package com.studymate.app.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.studymate.app.data.model.Material
import com.studymate.app.data.model.MaterialType
import com.studymate.app.data.model.ProcessingStatus
import com.studymate.app.data.remote.SupabaseClientProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.seconds

/**
 * Implements [MaterialRepository] against the private Supabase Storage bucket
 * `study-materials` with metadata persistence in Cloud Firestore.
 *
 * Security & Isolation:
 * - Every file is stored under: `materials/<firebase-uid>/<unique-filename>`
 * - Firebase UID is obtained securely from [FirebaseAuth.currentUser], never trusted from callers.
 * - All uploads, listings, signed URLs, and deletions are isolated strictly to the current user's UID.
 * - Atomic metadata write: If the Firestore metadata write fails, the uploaded Storage file
 *   is automatically deleted to prevent orphaned files.
 */
class SupabaseStorageRepository(
    private val supabase: SupabaseClient = SupabaseClientProvider.client,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : MaterialRepository {

    private val bucket = "study-materials"

    // -------------------------------------------------------------------------
    // MaterialRepository — listing (scoped to current user's UID)
    // -------------------------------------------------------------------------

    override suspend fun getMaterials(): List<Material> = withContext(Dispatchers.IO) {
        val currentUser = firebaseAuth.currentUser ?: return@withContext emptyList()
        val uid = currentUser.uid

        try {
            val snapshot = firestore.collection("materials")
                .whereEqualTo("ownerId", uid)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val title = doc.getString("title") ?: return@mapNotNull null
                val typeStr = doc.getString("type") ?: doc.getString("fileType") ?: "PDF"
                val storagePath = doc.getString("storagePath") ?: ""
                val subject = doc.getString("subject") ?: ""
                val size = doc.getLong("size")
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                val statusStr = doc.getString("processingStatus") ?: "COMPLETED"

                Material(
                    id = id,
                    uploadedBy = uid,
                    title = title,
                    type = typeStr.toMaterialType(),
                    fileUrl = storagePath,
                    subject = subject,
                    size = size,
                    processingStatus = runCatching { ProcessingStatus.valueOf(statusStr) }.getOrDefault(ProcessingStatus.COMPLETED),
                    createdAt = createdAt
                )
            }.sortedByDescending { it.createdAt }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getMaterial(id: String): Material? = withContext(Dispatchers.IO) {
        val currentUser = firebaseAuth.currentUser ?: return@withContext null
        val uid = currentUser.uid

        runCatching {
            val doc = firestore.collection("materials").document(id).get().await()
            if (!doc.exists()) return@runCatching null

            val ownerId = doc.getString("ownerId")
            if (ownerId != uid) return@runCatching null

            val title = doc.getString("title") ?: ""
            val typeStr = doc.getString("type") ?: doc.getString("fileType") ?: "PDF"
            val storagePath = doc.getString("storagePath") ?: ""
            val subject = doc.getString("subject") ?: ""
            val size = doc.getLong("size")
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            val statusStr = doc.getString("processingStatus") ?: "COMPLETED"

            Material(
                id = doc.id,
                uploadedBy = uid,
                title = title,
                type = typeStr.toMaterialType(),
                fileUrl = storagePath,
                subject = subject,
                size = size,
                processingStatus = runCatching { ProcessingStatus.valueOf(statusStr) }.getOrDefault(ProcessingStatus.COMPLETED),
                createdAt = createdAt
            )
        }.getOrNull()
    }

    // -------------------------------------------------------------------------
    // MaterialRepository — upload & delete
    // -------------------------------------------------------------------------

    override suspend fun uploadMaterial(material: Material): Result<Material> {
        return Result.failure(
            UnsupportedOperationException(
                "Use uploadFile(title, subject, fileType, fileName, fileBytes) to provide binary data."
            )
        )
    }

    /**
     * Uploads a file to the private Supabase Storage bucket and creates a corresponding
     * metadata record in Firestore.
     *
     * Path structure: `materials/<firebase-uid>/<timestamp>_<cleanFileName>`
     *
     * If the Firestore metadata creation fails, the uploaded Storage object is deleted
     * to prevent orphaned storage objects.
     */
    suspend fun uploadFile(
        title: String,
        subject: String,
        fileType: MaterialType,
        fileName: String,
        fileBytes: ByteArray
    ): Result<Material> = withContext(Dispatchers.IO) {
        val currentUser = firebaseAuth.currentUser
            ?: return@withContext Result.failure(IllegalStateException("User is not authenticated."))
        val uid = currentUser.uid

        // Sanitize filename to avoid path traversal
        val sanitized = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val uniqueFileName = "${System.currentTimeMillis()}_$sanitized"
        val storagePath = "materials/$uid/$uniqueFileName"

        // Step 1: Upload binary content to private Supabase bucket
        val uploadResult = runCatching {
            supabase.storage.from(bucket).upload(storagePath, fileBytes) {
                upsert = false
            }
        }

        if (uploadResult.isFailure) {
            val error = uploadResult.exceptionOrNull() ?: Exception("Failed to upload file to storage.")
            return@withContext Result.failure(error)
        }

        // Step 2: Write metadata to Firestore
        val materialId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val docData = hashMapOf(
            "id" to materialId,
            "ownerId" to uid,
            "title" to title,
            "subject" to subject,
            "type" to fileType.name,
            "fileType" to fileType.name,
            "storagePath" to storagePath,
            "size" to fileBytes.size.toLong(),
            "createdAt" to now,
            "processingStatus" to ProcessingStatus.COMPLETED.name
        )

        try {
            firestore.collection("materials").document(materialId).set(docData).await()
        } catch (firestoreError: Exception) {
            // Rollback: delete the uploaded file from Supabase Storage so no orphaned file remains
            runCatching {
                supabase.storage.from(bucket).delete(storagePath)
            }
            return@withContext Result.failure(
                Exception("Failed to save metadata to Firestore. Storage upload was rolled back: ${firestoreError.message}", firestoreError)
            )
        }

        Result.success(
            Material(
                id = materialId,
                uploadedBy = uid,
                title = title,
                type = fileType,
                fileUrl = storagePath,
                subject = subject,
                size = fileBytes.size.toLong(),
                processingStatus = ProcessingStatus.COMPLETED,
                createdAt = now
            )
        )
    }

    override suspend fun deleteMaterial(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        val currentUser = firebaseAuth.currentUser
            ?: return@withContext Result.failure(IllegalStateException("User is not authenticated."))
        val uid = currentUser.uid

        runCatching {
            val doc = firestore.collection("materials").document(id).get().await()
            if (!doc.exists()) {
                return@runCatching
            }

            val ownerId = doc.getString("ownerId")
            if (ownerId != uid) {
                throw SecurityException("Unauthorized: You do not own this material.")
            }

            val storagePath = doc.getString("storagePath")

            // Delete Firestore document
            firestore.collection("materials").document(id).delete().await()

            // Delete object from Supabase Storage
            if (!storagePath.isNullOrBlank()) {
                supabase.storage.from(bucket).delete(storagePath)
            }
        }
    }

    // -------------------------------------------------------------------------
    // Time-limited Signed URL Generation
    // -------------------------------------------------------------------------

    /**
     * Generates a signed download URL for the private object at [storagePath].
     * Enforces user isolation: [storagePath] MUST be inside `materials/<uid>/`.
     */
    suspend fun createSignedUrl(
        storagePath: String,
        expiresInSeconds: Long = 3600L
    ): Result<String> = withContext(Dispatchers.IO) {
        val currentUser = firebaseAuth.currentUser
            ?: return@withContext Result.failure(IllegalStateException("User is not authenticated."))
        val uid = currentUser.uid

        if (!storagePath.startsWith("materials/$uid/")) {
            return@withContext Result.failure(SecurityException("Unauthorized access to storage path."))
        }

        runCatching {
            supabase.storage.from(bucket).createSignedUrl(
                path = storagePath,
                expiresIn = expiresInSeconds.seconds
            )
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun String.toMaterialType(): MaterialType =
        when (uppercase()) {
            "PDF"  -> MaterialType.PDF
            "PPT"  -> MaterialType.PPT
            "PPTX" -> MaterialType.PPTX
            "DOC"  -> MaterialType.DOC
            "DOCX" -> MaterialType.DOCX
            "TXT"  -> MaterialType.TXT
            "YOUTUBE" -> MaterialType.YOUTUBE
            else -> when (substringAfterLast('.').lowercase()) {
                "pdf"  -> MaterialType.PDF
                "ppt"  -> MaterialType.PPT
                "pptx" -> MaterialType.PPTX
                "doc"  -> MaterialType.DOC
                "docx" -> MaterialType.DOCX
                "txt"  -> MaterialType.TXT
                else   -> MaterialType.PDF
            }
        }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                continuation.resumeWithException(
                    task.exception ?: RuntimeException("Firebase operation completed without a result or exception.")
                )
            }
        }
    }
}
