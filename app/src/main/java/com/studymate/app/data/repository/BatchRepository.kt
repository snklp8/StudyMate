package com.studymate.app.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.studymate.app.data.model.Batch
import com.studymate.app.data.model.BatchStudent
import com.studymate.app.data.model.Material
import com.studymate.app.data.model.MaterialType
import com.studymate.app.data.model.ProcessingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class BatchRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val notificationRepository: NotificationRepository = NotificationRepository()
) {
    private val secureRandom = SecureRandom()
    private val codeAlphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // 32 characters, no ambiguous 0/O, 1/I

    suspend fun generateUniqueBatchCode(): String = withContext(Dispatchers.IO) {
        for (attempt in 0 until 5) {
            val sb = StringBuilder(6)
            for (i in 0 until 6) {
                sb.append(codeAlphabet[secureRandom.nextInt(codeAlphabet.length)])
            }
            val code = sb.toString()
            val existing = firestore.collection("batches")
                .whereEqualTo("batchCode", code)
                .limit(1)
                .get()
                .await()
            if (existing.isEmpty) {
                return@withContext code
            }
        }
        // Fallback with timestamp suffix
        val base = (1000..9999).random().toString()
        "BAT$base"
    }

    suspend fun createBatch(
        name: String,
        course: String,
        semester: Int,
        academicYear: String,
        description: String
    ): Result<Batch> = withContext(Dispatchers.IO) {
        val user = firebaseAuth.currentUser
            ?: return@withContext Result.failure(IllegalStateException("Teacher is not authenticated."))
        val teacherId = user.uid
        val teacherName = user.displayName ?: user.email?.substringBefore('@') ?: "Instructor"

        try {
            val batchId = UUID.randomUUID().toString()
            val batchCode = generateUniqueBatchCode()

            val batch = Batch(
                id = batchId,
                teacherId = teacherId,
                teacherName = teacherName,
                name = name.trim(),
                course = course.trim(),
                semester = semester,
                academicYear = academicYear.trim(),
                batchCode = batchCode,
                description = description.trim(),
                studentCount = 0,
                studentIds = emptyList(),
                averageScore = 0f,
                createdAt = System.currentTimeMillis()
            )

            val map = hashMapOf(
                "id" to batch.id,
                "teacherId" to batch.teacherId,
                "teacherName" to batch.teacherName,
                "name" to batch.name,
                "course" to batch.course,
                "semester" to batch.semester,
                "academicYear" to batch.academicYear,
                "batchCode" to batch.batchCode,
                "description" to batch.description,
                "studentCount" to 0,
                "studentIds" to emptyList<String>(),
                "averageScore" to 0f,
                "createdAt" to batch.createdAt
            )

            firestore.collection("batches").document(batchId).set(map).await()
            Result.success(batch)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTeacherBatches(): List<Batch> = withContext(Dispatchers.IO) {
        val uid = firebaseAuth.currentUser?.uid ?: return@withContext emptyList()
        try {
            val snapshot = firestore.collection("batches")
                .whereEqualTo("teacherId", uid)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toBatch() }
                .sortedByDescending { it.createdAt }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getBatchById(batchId: String): Batch? = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection("batches").document(batchId).get().await()
            if (doc.exists()) doc.toBatch() else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getStudentBatches(): List<Batch> = withContext(Dispatchers.IO) {
        val uid = firebaseAuth.currentUser?.uid ?: return@withContext emptyList()
        try {
            val snapshot = firestore.collection("batches")
                .whereArrayContains("studentIds", uid)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toBatch() }
                .sortedByDescending { it.createdAt }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun joinBatch(code: String): Result<Batch> = withContext(Dispatchers.IO) {
        val user = firebaseAuth.currentUser
            ?: return@withContext Result.failure(IllegalStateException("User is not authenticated."))
        val studentId = user.uid
        val cleanCode = code.trim().uppercase()

        if (cleanCode.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid batch code."))
        }

        try {
            val snapshot = firestore.collection("batches")
                .whereEqualTo("batchCode", cleanCode)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                return@withContext Result.failure(IllegalArgumentException("Invalid batch code. No matching batch found."))
            }

            val doc = snapshot.documents.first()
            val batch = doc.toBatch() ?: return@withContext Result.failure(IllegalStateException("Could not read batch."))

            if (batch.studentIds.contains(studentId)) {
                return@withContext Result.failure(IllegalStateException("You are already enrolled in '${batch.name}'."))
            }

            // Fetch student profile details from Firestore
            val userDoc = runCatching { firestore.collection("users").document(studentId).get().await() }.getOrNull()
            val studentName = userDoc?.getString("name") ?: user.displayName ?: user.email?.substringBefore('@') ?: "Student"
            val studentEmail = user.email ?: userDoc?.getString("email") ?: ""

            // Atomically update batch studentIds and studentCount
            val batchRef = firestore.collection("batches").document(batch.id)
            val writeBatch = firestore.batch()
            writeBatch.update(batchRef, "studentIds", FieldValue.arrayUnion(studentId))
            writeBatch.update(batchRef, "studentCount", FieldValue.increment(1))

            // Save member record
            val memberRef = batchRef.collection("members").document(studentId)
            val memberData = hashMapOf(
                "studentId" to studentId,
                "batchId" to batch.id,
                "name" to studentName,
                "email" to studentEmail,
                "performance" to 0f,
                "joinedAt" to System.currentTimeMillis(),
                "lastActivity" to "Just now"
            )
            writeBatch.set(memberRef, memberData)
            writeBatch.commit().await()

            // Notify Teacher
            notificationRepository.createNotification(
                userId = batch.teacherId,
                title = "New Student Joined",
                message = "$studentName has enrolled in ${batch.name}.",
                type = "BATCH_JOIN",
                relatedId = batch.id
            )

            // Notify Student
            notificationRepository.createNotification(
                userId = studentId,
                title = "Enrolled Successfully",
                message = "You have joined ${batch.name} (${batch.course}).",
                type = "BATCH_JOIN",
                relatedId = batch.id
            )

            val updatedBatch = batch.copy(
                studentIds = batch.studentIds + studentId,
                studentCount = batch.studentCount + 1
            )
            Result.success(updatedBatch)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun leaveBatch(batchId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = firebaseAuth.currentUser
            ?: return@withContext Result.failure(IllegalStateException("Not authenticated."))
        val studentId = user.uid

        try {
            val batchRef = firestore.collection("batches").document(batchId)
            val writeBatch = firestore.batch()
            writeBatch.update(batchRef, "studentIds", FieldValue.arrayRemove(studentId))
            writeBatch.update(batchRef, "studentCount", FieldValue.increment(-1))
            val memberRef = batchRef.collection("members").document(studentId)
            writeBatch.delete(memberRef)
            writeBatch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBatchStudents(batchId: String): List<BatchStudent> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("batches").document(batchId)
                .collection("members")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                BatchStudent(
                    studentId = doc.getString("studentId") ?: doc.id,
                    batchId = batchId,
                    name = doc.getString("name") ?: "Student",
                    email = doc.getString("email") ?: "",
                    performance = doc.getDouble("performance")?.toFloat() ?: 0f,
                    joinedAt = doc.getLong("joinedAt") ?: System.currentTimeMillis(),
                    lastActivity = doc.getString("lastActivity") ?: "Active"
                )
            }.sortedByDescending { it.joinedAt }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getBatchMaterials(batchId: String): List<Material> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("materials")
                .whereEqualTo("batchId", batchId)
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
                val bId = doc.getString("batchId")
                val bName = doc.getString("batchName")

                Material(
                    id = id,
                    uploadedBy = doc.getString("ownerId") ?: "",
                    title = title,
                    type = runCatching { MaterialType.valueOf(typeStr.uppercase()) }.getOrDefault(MaterialType.PDF),
                    fileUrl = storagePath,
                    subject = subject,
                    size = size,
                    processingStatus = runCatching { ProcessingStatus.valueOf(statusStr) }.getOrDefault(ProcessingStatus.COMPLETED),
                    batchId = bId,
                    batchName = bName,
                    createdAt = createdAt
                )
            }.sortedByDescending { it.createdAt }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toBatch(): Batch? {
        if (!exists()) return null
        val id = getString("id") ?: id
        val name = getString("name") ?: return null
        val course = getString("course") ?: ""
        val semester = getLong("semester")?.toInt() ?: 1
        val academicYear = getString("academicYear") ?: ""
        val batchCode = getString("batchCode") ?: ""
        val teacherId = getString("teacherId") ?: ""
        val teacherName = getString("teacherName") ?: "Instructor"
        val description = getString("description") ?: ""
        val studentCount = getLong("studentCount")?.toInt() ?: 0
        val studentIds = (get("studentIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        val averageScore = getDouble("averageScore")?.toFloat() ?: 0f
        val createdAt = getLong("createdAt") ?: System.currentTimeMillis()

        return Batch(
            id = id,
            teacherId = teacherId,
            teacherName = teacherName,
            name = name,
            course = course,
            semester = semester,
            academicYear = academicYear,
            batchCode = batchCode,
            description = description,
            studentCount = studentCount,
            studentIds = studentIds,
            averageScore = averageScore,
            createdAt = createdAt
        )
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> continuation.resume(result) }
        addOnFailureListener { exception -> continuation.resumeWithException(exception) }
        addOnCanceledListener { continuation.cancel() }
    }
}
