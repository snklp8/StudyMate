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
    // MaterialRepository — listing (scoped to user's UID and enrolled batches)
    // -------------------------------------------------------------------------

    override suspend fun getMaterials(): List<Material> = withContext(Dispatchers.IO) {
        val currentUser = firebaseAuth.currentUser ?: return@withContext emptyList()
        val uid = currentUser.uid

        try {
            // 1. Get materials owned by user
            val ownedDocs = firestore.collection("materials")
                .whereEqualTo("ownerId", uid)
                .get()
                .await()
                .documents

            // 2. Get materials from batches the user is enrolled in
            val enrolledBatches = firestore.collection("batches")
                .whereArrayContains("studentIds", uid)
                .get()
                .await()
                .documents

            val batchMaterialDocs = mutableListOf<com.google.firebase.firestore.DocumentSnapshot>()
            for (batchDoc in enrolledBatches) {
                val bId = batchDoc.id
                val docs = firestore.collection("materials")
                    .whereEqualTo("batchId", bId)
                    .get()
                    .await()
                    .documents
                batchMaterialDocs.addAll(docs)
            }

            // Combine and deduplicate by document id
            val allDocs = (ownedDocs + batchMaterialDocs).distinctBy { it.id }

            allDocs.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val title = doc.getString("title") ?: return@mapNotNull null
                val typeStr = doc.getString("type") ?: doc.getString("fileType") ?: "PDF"
                val storagePath = doc.getString("storagePath") ?: ""
                val subject = doc.getString("subject") ?: ""
                val size = doc.getLong("size")
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                val statusStr = doc.getString("processingStatus") ?: "COMPLETED"
                val batchId = doc.getString("batchId")
                val batchName = doc.getString("batchName")

                Material(
                    id = id,
                    uploadedBy = doc.getString("ownerId") ?: uid,
                    title = title,
                    type = typeStr.toMaterialType(),
                    fileUrl = storagePath,
                    subject = subject,
                    size = size,
                    processingStatus = runCatching { ProcessingStatus.valueOf(statusStr) }.getOrDefault(ProcessingStatus.COMPLETED),
                    createdAt = createdAt,
                    batchId = batchId,
                    batchName = batchName
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
            val batchId = doc.getString("batchId")
            val isOwner = ownerId == uid

            val isAuthorized = if (isOwner) {
                true
            } else if (!batchId.isNullOrBlank()) {
                val batchDoc = firestore.collection("batches").document(batchId).get().await()
                @Suppress("UNCHECKED_CAST")
                val studentIds = batchDoc.get("studentIds") as? List<String> ?: emptyList()
                val teacherId = batchDoc.getString("teacherId")
                teacherId == uid || studentIds.contains(uid)
            } else {
                false
            }

            if (!isAuthorized) return@runCatching null

            val title = doc.getString("title") ?: ""
            val typeStr = doc.getString("type") ?: doc.getString("fileType") ?: "PDF"
            val storagePath = doc.getString("storagePath") ?: ""
            val subject = doc.getString("subject") ?: ""
            val size = doc.getLong("size")
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            val statusStr = doc.getString("processingStatus") ?: "COMPLETED"
            val batchName = doc.getString("batchName")

            Material(
                id = doc.id,
                uploadedBy = ownerId ?: uid,
                title = title,
                type = typeStr.toMaterialType(),
                fileUrl = storagePath,
                subject = subject,
                size = size,
                processingStatus = runCatching { ProcessingStatus.valueOf(statusStr) }.getOrDefault(ProcessingStatus.COMPLETED),
                createdAt = createdAt,
                batchId = batchId,
                batchName = batchName
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
        fileBytes: ByteArray,
        batchId: String? = null,
        batchName: String? = null
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
        val docData = hashMapOf<String, Any?>(
            "id" to materialId,
            "ownerId" to uid,
            "title" to title,
            "subject" to subject,
            "type" to fileType.name,
            "fileType" to fileType.name,
            "storagePath" to storagePath,
            "size" to fileBytes.size.toLong(),
            "createdAt" to now,
            "processingStatus" to ProcessingStatus.COMPLETED.name,
            "batchId" to (batchId ?: ""),
            "batchName" to (batchName ?: "")
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

        // Step 3: If assigned to a batch, notify students
        if (!batchId.isNullOrBlank()) {
            runCatching {
                val notifRepo = NotificationRepository(firestore, firebaseAuth)
                notifRepo.notifyBatchStudents(
                    batchId = batchId,
                    title = "New Batch Material Uploaded",
                    message = "New material \"$title\" uploaded for ${batchName ?: "your batch"}.",
                    relatedId = materialId
                )
            }
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
                createdAt = now,
                batchId = batchId,
                batchName = batchName
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
    // Time-limited Signed URL Generation & Download
    // -------------------------------------------------------------------------

    private suspend fun isAuthorizedForPath(uid: String, storagePath: String): Boolean {
        if (storagePath.startsWith("materials/$uid/")) return true
        return runCatching {
            val matQuery = firestore.collection("materials")
                .whereEqualTo("storagePath", storagePath)
                .limit(1)
                .get()
                .await()
            val doc = matQuery.documents.firstOrNull() ?: return@runCatching false
            val batchId = doc.getString("batchId") ?: return@runCatching false
            if (batchId.isBlank()) return@runCatching false
            val batchDoc = firestore.collection("batches").document(batchId).get().await()
            @Suppress("UNCHECKED_CAST")
            val studentIds = batchDoc.get("studentIds") as? List<String> ?: emptyList()
            val teacherId = batchDoc.getString("teacherId")
            teacherId == uid || studentIds.contains(uid)
        }.getOrDefault(false)
    }

    /**
     * Generates a signed download URL for the private object at [storagePath].
     * Enforces user isolation: [storagePath] MUST be owned by user or from an enrolled batch.
     */
    suspend fun createSignedUrl(
        storagePath: String,
        expiresInSeconds: Long = 3600L
    ): Result<String> = withContext(Dispatchers.IO) {
        val currentUser = firebaseAuth.currentUser
            ?: return@withContext Result.failure(IllegalStateException("User is not authenticated."))
        val uid = currentUser.uid

        if (!isAuthorizedForPath(uid, storagePath)) {
            return@withContext Result.failure(SecurityException("Unauthorized access to storage path."))
        }

        runCatching {
            supabase.storage.from(bucket).createSignedUrl(
                path = storagePath,
                expiresIn = expiresInSeconds.seconds
            )
        }
    }

    /**
     * Downloads binary content for a stored material.
     * Enforces user isolation or batch membership strictly.
     */
    suspend fun downloadMaterialBytes(storagePath: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        val currentUser = firebaseAuth.currentUser
            ?: return@withContext Result.failure(IllegalStateException("User is not authenticated."))
        val uid = currentUser.uid

        if (!isAuthorizedForPath(uid, storagePath)) {
            return@withContext Result.failure(SecurityException("Unauthorized access to storage path."))
        }

        // Try downloading directly via authenticated client
        val directResult = runCatching {
            supabase.storage.from(bucket).downloadAuthenticated(storagePath)
        }

        if (directResult.isSuccess && directResult.getOrNull()?.isNotEmpty() == true) {
            return@withContext directResult
        }

        // Fallback: Generate time-limited signed URL and download via stream
        runCatching {
            val signedUrl = createSignedUrl(storagePath, expiresInSeconds = 300L).getOrThrow()
            java.net.URL(signedUrl).openStream().use { it.readBytes() }
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
            else -> when (substringAfterLast('.').lowercase()) {
                "pdf"  -> MaterialType.PDF
                "ppt"  -> MaterialType.PPT
                "pptx" -> MaterialType.PPTX
                "doc"  -> MaterialType.DOC
                "docx" -> MaterialType.DOCX
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
