package com.studymate.app.data.repository

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.studymate.app.BuildConfig
import com.studymate.app.data.model.Difficulty
import com.studymate.app.data.model.Quiz
import com.studymate.app.data.util.PdfTextExtractor
import com.studymate.app.ui.quiz.QuizQuestion
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
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GeminiQuizRepository(
    private val storageRepository: SupabaseStorageRepository = SupabaseStorageRepository(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : QuizRepository {

    override suspend fun generateQuiz(
        materialId: String,
        difficulty: Difficulty,
        count: Int
    ): Result<Quiz> = withContext(Dispatchers.IO) {
        val diffStr = when (difficulty) {
            Difficulty.EASY -> "Easy"
            Difficulty.MEDIUM -> "Medium"
            Difficulty.HARD -> "Hard"
        }
        val questionsResult = generateQuizQuestions(materialId, diffStr, count)
        if (questionsResult.isFailure) {
            return@withContext Result.failure(questionsResult.exceptionOrNull()!!)
        }
        val uiQuestions = questionsResult.getOrThrow()
        val domainQuestions = uiQuestions.map { q ->
            com.studymate.app.data.model.QuizQuestion(
                questionId = q.id,
                question = q.text,
                options = q.options,
                correctAnswer = q.options.getOrElse(q.correctOptionIndex) { "" }
            )
        }
        val quiz = Quiz(
            id = UUID.randomUUID().toString(),
            studentId = firebaseAuth.currentUser?.uid ?: "",
            materialId = materialId,
            questions = domainQuestions,
            totalQuestions = domainQuestions.size
        )
        Result.success(quiz)
    }

    suspend fun generateQuizQuestions(
        materialId: String,
        difficulty: String,
        count: Int
    ): Result<List<QuizQuestion>> = withContext(Dispatchers.IO) {
        val currentUser = firebaseAuth.currentUser
            ?: return@withContext Result.failure(IllegalStateException("User is not authenticated."))
        val uid = currentUser.uid

        val material = storageRepository.getMaterial(materialId)
            ?: return@withContext Result.failure(IllegalArgumentException("Material not found."))

        val storagePath = material.fileUrl
        if (storagePath.isNullOrBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Material has no file."))
        }

        val downloadResult = storageRepository.downloadMaterialBytes(storagePath)
        if (downloadResult.isFailure) {
            return@withContext Result.failure(downloadResult.exceptionOrNull()!!)
        }
        val fileBytes = downloadResult.getOrThrow()

        val ctx = GeminiQuestionRepository.appContext ?: FirestoreSummaryRepository.appContext
            ?: return@withContext Result.failure(IllegalStateException("Application context not initialized."))

        val extractionResult = PdfTextExtractor.extractText(
            context = ctx,
            fileBytes = fileBytes,
            fileType = material.type.name
        )
        if (extractionResult.isFailure) {
            return@withContext Result.failure(extractionResult.exceptionOrNull()!!)
        }
        val extractedText = extractionResult.getOrThrow()

        val firebaseIdToken = suspendCancellableCoroutine<String?> { continuation ->
            currentUser.getIdToken(false).addOnCompleteListener { task ->
                if (task.isSuccessful) continuation.resume(task.result?.token) else continuation.resume(null)
            }
        } ?: return@withContext Result.failure(IllegalStateException("Token error."))

        val baseUrl = BuildConfig.SUPABASE_URL.trimEnd('/')
        val functionUrl = URL("$baseUrl/functions/v1/generate-quiz")

        try {
            val connection = (functionUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 60_000
                readTimeout = 120_000
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
                put("mode", "QUIZ")
                put("type", "MCQ")
                put("difficulty", difficulty)
                put("count", count)
            }

            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { it.write(requestPayload.toString()); it.flush() }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val errorStream = connection.errorStream ?: connection.inputStream
                val errorMsg = BufferedReader(InputStreamReader(errorStream, Charsets.UTF_8)).use { it.readText() }
                return@withContext Result.failure(Exception("Gemini Quiz error ($responseCode): $errorMsg"))
            }

            val responseBody = BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8)).use { it.readText() }
            val responseJson = JSONObject(responseBody)
            val questionsArray = responseJson.optJSONArray("questions") ?: JSONArray()

            val list = mutableListOf<QuizQuestion>()
            for (i in 0 until questionsArray.length()) {
                val qObj = questionsArray.getJSONObject(i)
                val opts = qObj.optJSONArray("options")?.toStringList() ?: listOf("Option A", "Option B", "Option C", "Option D")
                list.add(
                    QuizQuestion(
                        id = qObj.optString("id", "q_${i + 1}"),
                        text = qObj.optString("text", "Question statement"),
                        options = opts,
                        correctOptionIndex = qObj.optInt("correctOptionIndex", 0)
                    )
                )
            }

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitQuiz(quiz: Quiz): Result<Quiz> = withContext(Dispatchers.IO) {
        val currentUser = firebaseAuth.currentUser ?: return@withContext Result.failure(IllegalStateException("No user"))
        val uid = currentUser.uid

        val docData = hashMapOf(
            "id" to quiz.id,
            "studentId" to uid,
            "materialId" to quiz.materialId,
            "materialTitle" to quiz.materialTitle,
            "score" to quiz.score,
            "totalQuestions" to quiz.totalQuestions,
            "percentage" to quiz.percentage,
            "createdAt" to System.currentTimeMillis(),
            "isCompleted" to true
        )

        try {
            firestore.collection("quiz_results").document(quiz.id).set(docData).await()
            Result.success(quiz)
        } catch (e: Exception) {
            Result.success(quiz)
        }
    }

    override suspend fun getQuizHistory(): List<Quiz> = withContext(Dispatchers.IO) {
        val uid = firebaseAuth.currentUser?.uid ?: return@withContext emptyList()
        try {
            val snapshot = firestore.collection("quiz_results")
                .whereEqualTo("studentId", uid)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                Quiz(
                    id = doc.getString("id") ?: doc.id,
                    studentId = uid,
                    materialId = doc.getString("materialId") ?: "",
                    materialTitle = doc.getString("materialTitle") ?: "",
                    score = doc.getLong("score")?.toInt() ?: 0,
                    totalQuestions = doc.getLong("totalQuestions")?.toInt() ?: 0,
                    percentage = doc.getDouble("percentage")?.toFloat() ?: 0f,
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    isCompleted = true
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getQuiz(quizId: String): Quiz? = null

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
            if (task.isSuccessful) continuation.resume(task.result)
            else continuation.resumeWithException(task.exception ?: RuntimeException("Firebase error"))
        }
    }
}
