package com.studymate.app.data.repository

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.studymate.app.BuildConfig
import com.studymate.app.data.model.Difficulty
import com.studymate.app.data.model.Question
import com.studymate.app.data.model.QuestionType
import com.studymate.app.data.util.PdfTextExtractor
import com.studymate.app.ui.questions.GeneratedQuestionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GeminiQuestionRepository(
    private val storageRepository: SupabaseStorageRepository = SupabaseStorageRepository(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : QuestionRepository {

    companion object {
        private const val TAG = "GeminiQuestionRepo"

        @Volatile
        var appContext: Context? = null
    }

    override suspend fun generateQuestions(
        materialId: String,
        type: QuestionType,
        difficulty: Difficulty,
        count: Int
    ): Result<List<Question>> = withContext(Dispatchers.IO) {
        val typeStr = when (type) {
            QuestionType.MCQ -> "MCQ"
            QuestionType.VIVA -> "Viva"
            QuestionType.LONG_ANSWER -> "Long Answer"
        }
        val diffStr = when (difficulty) {
            Difficulty.EASY -> "Easy"
            Difficulty.MEDIUM -> "Medium"
            Difficulty.HARD -> "Hard"
        }

        val itemsResult = generateQuestionItems(materialId, typeStr, diffStr, count)
        if (itemsResult.isFailure) {
            return@withContext Result.failure(itemsResult.exceptionOrNull()!!)
        }

        val items = itemsResult.getOrThrow()
        val questions = items.map { item ->
            Question(
                id = item.id,
                materialId = materialId,
                type = type,
                question = item.text,
                options = item.options,
                correctAnswer = item.options?.firstOrNull() ?: "",
                difficulty = difficulty
            )
        }
        Result.success(questions)
    }

    suspend fun generateQuestionItems(
        materialId: String,
        type: String,
        difficulty: String,
        count: Int
    ): Result<List<GeneratedQuestionItem>> = withContext(Dispatchers.IO) {
        Log.d(TAG, "generateQuestionItems: materialId=$materialId type=$type difficulty=$difficulty count=$count")

        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "FAIL: User not authenticated")
            return@withContext Result.failure(IllegalStateException("User is not authenticated."))
        }
        val uid = currentUser.uid
        Log.d(TAG, "User authenticated: uid=${uid.take(8)}...")

        val material = storageRepository.getMaterial(materialId)
        if (material == null) {
            Log.e(TAG, "FAIL: Material not found for id=$materialId")
            return@withContext Result.failure(IllegalArgumentException("Material not found or access denied."))
        }
        Log.d(TAG, "Material found: title=${material.title} type=${material.type} path=${material.fileUrl}")

        val storagePath = material.fileUrl
        if (storagePath.isNullOrBlank()) {
            Log.e(TAG, "FAIL: Material has no file URL")
            return@withContext Result.failure(IllegalArgumentException("Material has no associated storage file."))
        }

        Log.d(TAG, "Downloading bytes from: $storagePath")
        val downloadResult = storageRepository.downloadMaterialBytes(storagePath)
        if (downloadResult.isFailure) {
            val ex = downloadResult.exceptionOrNull()
            Log.e(TAG, "FAIL: Download failed: ${ex?.message}", ex)
            return@withContext Result.failure(
                Exception("Failed to download material: ${ex?.localizedMessage ?: "Unknown storage error"}", ex)
            )
        }
        val fileBytes = downloadResult.getOrThrow()
        Log.d(TAG, "Downloaded ${fileBytes.size} bytes")

        val ctx = appContext ?: FirestoreSummaryRepository.appContext
        if (ctx == null) {
            Log.e(TAG, "FAIL: App context not initialized")
            return@withContext Result.failure(IllegalStateException("Context not initialized."))
        }
        Log.d(TAG, "Extracting text from ${material.type.name} file...")
        val extractionResult = PdfTextExtractor.extractText(
            context = ctx,
            fileBytes = fileBytes,
            fileType = material.type.name
        )
        if (extractionResult.isFailure) {
            val ex = extractionResult.exceptionOrNull()
            Log.e(TAG, "FAIL: Text extraction failed: ${ex?.message}")
            return@withContext Result.failure(ex!!)
        }
        val extractedText = extractionResult.getOrThrow()
        Log.d(TAG, "Extracted text: ${extractedText.length} chars, preview: '${extractedText.take(100)}'")

        // Force-refresh token to avoid stale token issues
        Log.d(TAG, "Getting Firebase ID token (force refresh)...")
        val firebaseIdToken = suspendCancellableCoroutine<String?> { continuation ->
            currentUser.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result?.token
                    Log.d(TAG, "Token obtained: ${token?.take(20)}...")
                    continuation.resume(token)
                } else {
                    Log.e(TAG, "Token fetch failed: ${task.exception?.message}")
                    continuation.resume(null)
                }
            }
        }
        if (firebaseIdToken == null) {
            Log.e(TAG, "FAIL: Firebase token is null")
            return@withContext Result.failure(IllegalStateException("Unable to retrieve Firebase token."))
        }

        val baseUrl = BuildConfig.SUPABASE_URL.trimEnd('/')
        if (baseUrl.isBlank()) {
            Log.e(TAG, "FAIL: SUPABASE_URL is blank")
            return@withContext Result.failure(IllegalStateException("SUPABASE_URL is not configured."))
        }
        Log.d(TAG, "Calling edge function: $baseUrl/functions/v1/generate-quiz")

        try {
            val functionUrl = URL("$baseUrl/functions/v1/generate-quiz")
            val connection = (functionUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 60_000
                readTimeout = 180_000
                doInput = true
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Authorization", "Bearer $firebaseIdToken")
                setRequestProperty("apikey", BuildConfig.SUPABASE_ANON_KEY)
            }

            val requestPayload = JSONObject().apply {
                put("materialId", materialId)
                put("materialTitle", material.title)
                put("text", extractedText)
                put("mode", "QUESTIONS")
                put("type", type)
                put("difficulty", difficulty)
                put("count", count)
            }
            Log.d(TAG, "Payload size: ${requestPayload.toString().length} chars")

            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(requestPayload.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            Log.d(TAG, "HTTP response code: $responseCode")
            if (responseCode !in 200..299) {
                val errorResponse = runCatching {
                    val stream = connection.errorStream ?: connection.inputStream
                    BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
                }.getOrDefault("")
                val errorMsg = runCatching { JSONObject(errorResponse).optString("error", errorResponse) }.getOrDefault(errorResponse)
                Log.e(TAG, "FAIL: HTTP $responseCode — $errorMsg")
                return@withContext Result.failure(Exception("AI generation error ($responseCode): $errorMsg"))
            }

            val responseBody = BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8)).use { it.readText() }
            Log.d(TAG, "Response body (first 200 chars): ${responseBody.take(200)}")
            val responseJson = JSONObject(responseBody)
            val questionsArray = responseJson.optJSONArray("questions") ?: JSONArray()
            Log.d(TAG, "Questions in response: ${questionsArray.length()}")

            val list = mutableListOf<GeneratedQuestionItem>()
            for (i in 0 until questionsArray.length()) {
                val qObj = questionsArray.getJSONObject(i)
                list.add(
                    GeneratedQuestionItem(
                        id = qObj.optString("id", "q_${i + 1}"),
                        number = i + 1,
                        type = type,
                        difficulty = difficulty,
                        text = qObj.optString("text", ""),
                        options = qObj.optJSONArray("options")?.toStringList(),
                        keyConcepts = qObj.optJSONArray("keyConcepts")?.toStringList(),
                        expectedPoints = qObj.optJSONArray("expectedPoints")?.toStringList(),
                        isBookmarked = false
                    )
                )
            }

            // Cache in Firestore under questions collection
            runCatching {
                val batch = firestore.batch()
                list.forEach { item ->
                    val docRef = firestore.collection("generated_questions").document("${materialId}_${item.id}")
                    val map = hashMapOf(
                        "materialId" to materialId,
                        "ownerId" to uid,
                        "type" to item.type,
                        "difficulty" to item.difficulty,
                        "text" to item.text,
                        "options" to item.options,
                        "keyConcepts" to item.keyConcepts,
                        "expectedPoints" to item.expectedPoints,
                        "createdAt" to System.currentTimeMillis()
                    )
                    batch.set(docRef, map)
                }
                batch.commit().await()
            }

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun JSONArray.toStringList(): List<String> {
        val list = ArrayList<String>(length())
        for (i in 0 until length()) {
            val item = optString(i, null)
            if (!item.isNullOrBlank()) list.add(item)
        }
        return list
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                continuation.resumeWithException(
                    task.exception ?: RuntimeException("Firebase operation completed with error.")
                )
            }
        }
    }
}
