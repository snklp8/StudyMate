package com.studymate.app.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.studymate.app.data.model.AppNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class NotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    suspend fun getUserNotifications(): List<AppNotification> = withContext(Dispatchers.IO) {
        val uid = firebaseAuth.currentUser?.uid ?: return@withContext emptyList()
        try {
            val snapshot = firestore.collection("notifications")
                .whereEqualTo("userId", uid)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                AppNotification(
                    id = doc.getString("id") ?: doc.id,
                    userId = doc.getString("userId") ?: "",
                    title = doc.getString("title") ?: "",
                    message = doc.getString("message") ?: "",
                    type = doc.getString("type") ?: "GENERAL",
                    relatedId = doc.getString("relatedId") ?: "",
                    isRead = doc.getBoolean("isRead") ?: false,
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            }.sortedByDescending { it.createdAt }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun createNotification(
        userId: String,
        title: String,
        message: String,
        type: String,
        relatedId: String = ""
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val id = UUID.randomUUID().toString()
            val data = hashMapOf(
                "id" to id,
                "userId" to userId,
                "title" to title,
                "message" to message,
                "type" to type,
                "relatedId" to relatedId,
                "isRead" to false,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("notifications").document(id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsRead(notificationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("notifications").document(notificationId)
                .update("isRead", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAllAsRead(): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = firebaseAuth.currentUser?.uid ?: return@withContext Result.failure(IllegalStateException("Not authenticated"))
        try {
            val snapshot = firestore.collection("notifications")
                .whereEqualTo("userId", uid)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun notifyBatchStudents(
        batchId: String,
        title: String,
        message: String,
        relatedId: String = ""
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val batchDoc = firestore.collection("batches").document(batchId).get().await()
            val studentIds = (batchDoc.get("studentIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            if (studentIds.isEmpty()) return@withContext Result.success(Unit)

            val writeBatch = firestore.batch()
            studentIds.forEach { studentId ->
                val notifId = UUID.randomUUID().toString()
                val docRef = firestore.collection("notifications").document(notifId)
                writeBatch.set(
                    docRef,
                    hashMapOf(
                        "id" to notifId,
                        "userId" to studentId,
                        "title" to title,
                        "message" to message,
                        "type" to "BATCH_MATERIAL",
                        "relatedId" to relatedId,
                        "isRead" to false,
                        "createdAt" to System.currentTimeMillis()
                    )
                )
            }
            writeBatch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> continuation.resume(result) }
        addOnFailureListener { exception -> continuation.resumeWithException(exception) }
        addOnCanceledListener { continuation.cancel() }
    }
}
