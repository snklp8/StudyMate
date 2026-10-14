package com.studymate.app.data.repository

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.studymate.app.BuildConfig
import com.studymate.app.data.model.Summary
import com.studymate.app.data.util.PdfTextExtractor
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

/**
 * Production implementation of [SummaryRepository] that:
 * 1. Checks Firestore for existing cached study notes owned by the authenticated student.
 * 2. Downloads the source PDF securely from Supabase Storage.
 * 3. Safely extracts and bounds document text, catching scanned or empty documents.
 * 4. Calls the Supabase Edge Function `generate-summary` with the student's Firebase ID token.
 * 5. Persists generated structured notes in Firestore under `summaries/{materialId}` with strict ownership metadata.
 */
class FirestoreSummaryRepository(
    private val storageRepository: SupabaseStorageRepository = SupabaseStorageRepository(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : SummaryRepository {

    companion object {
        /**
         * Must be set once from Application.onCreate() or an Activity before calling generateSummary.
         * Used only for PDFBox text extraction initialisation.
         */
        @Volatile
        var appContext: Context? = null
    }

    override suspend fun getSummary(materialId: String): Summary? = withContext(Dispatchers.IO) {
        val currentUser = firebaseAuth.currentUser ?: return@withContext null
        val uid = currentUser.uid

        runCatching {
            val doc = firestore.collection("summaries").document(materialId).get().await()
            if (!doc.exists()) return@runCatching null

            val ownerId = doc.getString("ownerId")
            if (ownerId != uid) return@runCatching null

            doc.toSummaryModel(materialId, uid)
        }.getOrNull()
    }

    override suspend fun generateSummary(materialId: String): Result<Summary> = withContext(Dispatchers.IO) {
        val currentUser = firebaseAuth.currentUser
            ?: return@withContext Result.failure(IllegalStateException("User is not authenticated."))
        val uid = currentUser.uid

        // 1. Fetch material metadata from repository to verify ownership and path
        val material = storageRepository.getMaterial(materialId)
            ?: return@withContext Result.failure(IllegalArgumentException("Material not found or access denied."))

        val storagePath = material.fileUrl
        if (storagePath.isNullOrBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Material has no associated storage file."))
        }

        // 2. Download raw file bytes from Supabase Storage
        val downloadResult = storageRepository.downloadMaterialBytes(storagePath)
        if (downloadResult.isFailure) {
            val ex = downloadResult.exceptionOrNull()
            return@withContext Result.failure(
                Exception("Failed to download material from storage: ${ex?.localizedMessage ?: "Unknown storage error"}", ex)
            )
        }
        val fileBytes = downloadResult.getOrThrow()

        // 3. Extract text safely, handling scanned, empty, large, and corrupted PDFs
        val ctx = appContext ?: return@withContext Result.failure(
            IllegalStateException("Application context is not initialized. Ensure StudyMateApp is configured.")
        )
        val extractionResult = PdfTextExtractor.extractText(
            context = ctx,
            fileBytes = fileBytes,
            fileType = material.type.name
        )
        if (extractionResult.isFailure) {
            return@withContext Result.failure(extractionResult.exceptionOrNull()!!)
        }
        val extractedText = extractionResult.getOrThrow()

        // 4. Retrieve fresh Firebase ID token
        val firebaseIdToken = suspendCancellableCoroutine<String?> { continuation ->
            currentUser.getIdToken(false).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    continuation.resume(task.result?.token)
                } else {
                    continuation.resume(null)
                }
            }
        } ?: return@withContext Result.failure(IllegalStateException("Unable to retrieve Firebase ID token for authentication."))

        // 5. Call Supabase Edge Function: generate-summary
        val edgeFunctionResult = callGenerateSummaryEdgeFunction(
            idToken = firebaseIdToken,
            materialId = materialId,
            materialTitle = material.title,
            text = extractedText
        )

        if (edgeFunctionResult.isFailure) {
            val ex = edgeFunctionResult.exceptionOrNull()!!
            val friendlyError = if (ex is java.net.SocketTimeoutException) {
                Exception("The note synthesis timed out due to mobile network latency. Please tap Retry to resume.", ex)
            } else {
                ex
            }
            return@withContext Result.failure(friendlyError)
        }

        val generatedSummary = edgeFunctionResult.getOrThrow().copy(
            id = materialId,
            ownerId = uid,
            materialId = materialId,
            title = material.title,
            createdAt = System.currentTimeMillis()
        )

        // 6. Save successful summary to Firestore collection 'summaries'
        val docData = hashMapOf(
            "id" to materialId,
            "ownerId" to uid,
            "materialId" to materialId,
            "title" to material.title,
            "overview" to generatedSummary.overview,
            "headings" to generatedSummary.headings,
            "concepts" to generatedSummary.concepts,
            "definitions" to generatedSummary.definitions,
            "keywords" to generatedSummary.keywords,
            "formulas" to generatedSummary.formulas,
            "importantPoints" to generatedSummary.importantPoints,
            "revisionSection" to generatedSummary.revisionSection,
            "createdAt" to generatedSummary.createdAt
        )

        try {
            firestore.collection("summaries").document(materialId).set(docData).await()
        } catch (e: Exception) {
            // Even if offline or write error, return the generated notes so student can read immediately
            return@withContext Result.success(generatedSummary)
        }

        Result.success(generatedSummary)
    }

    /**
     * Executes an authenticated HTTP POST request to the Supabase Edge Function `generate-summary`.
     */
    private fun callGenerateSummaryEdgeFunction(
        idToken: String,
        materialId: String,
        materialTitle: String,
        text: String
    ): Result<Summary> = runCatching {
        val baseUrl = BuildConfig.SUPABASE_URL.trimEnd('/')
        if (baseUrl.isBlank()) {
            throw IllegalStateException("SUPABASE_URL is not configured in local.properties / BuildConfig.")
        }

        val functionUrl = URL("$baseUrl/functions/v1/generate-summary")
        val connection = (functionUrl.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 60_000
            readTimeout = 120_000
            doInput = true
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("Authorization", "Bearer $idToken")
            setRequestProperty("apikey", BuildConfig.SUPABASE_ANON_KEY)
        }

        val requestPayload = JSONObject().apply {
            put("materialId", materialId)
            put("materialTitle", materialTitle)
            put("text", text)
        }

        OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(requestPayload.toString())
            writer.flush()
        }

        val responseCode = connection.responseCode
        if (responseCode !in 200..299) {
            val errorResponse = runCatching {
                val stream = connection.errorStream ?: connection.inputStream
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
            }.getOrDefault("")

            val errorMsg = runCatching {
                JSONObject(errorResponse).optString("error", errorResponse)
            }.getOrDefault(errorResponse)

            throw when (responseCode) {
                401 -> SecurityException("Authentication failed: $errorMsg")
                403 -> SecurityException("Access forbidden: $errorMsg")
                429 -> IllegalStateException("Rate limit exceeded. Please wait a moment before trying again.")
                else -> Exception("Summary generation failed ($responseCode): $errorMsg")
            }
        }

        val responseBody = BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8)).use { it.readText() }
        val responseJson = JSONObject(responseBody)
        val summaryJson = responseJson.optJSONObject("summary") ?: responseJson

        Summary(
            id = materialId,
            materialId = materialId,
            overview = summaryJson.optString("overview", ""),
            headings = summaryJson.optJSONArray("headings")?.toStringList() ?: emptyList(),
            concepts = summaryJson.optJSONArray("concepts")?.toStringList() ?: emptyList(),
            definitions = summaryJson.optJSONArray("definitions")?.toStringList() ?: emptyList(),
            keywords = summaryJson.optJSONArray("keywords")?.toStringList() ?: emptyList(),
            formulas = (summaryJson.optJSONArray("importantFormulas")
                ?: summaryJson.optJSONArray("formulas"))?.toStringList() ?: emptyList(),
            importantPoints = summaryJson.optJSONArray("importantPoints")?.toStringList() ?: emptyList(),
            revisionSection = summaryJson.optJSONArray("revisionSection")?.toStringList() ?: emptyList()
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toSummaryModel(materialId: String, uid: String): Summary {
        @Suppress("UNCHECKED_CAST")
        return Summary(
            id = getString("id") ?: id,
            ownerId = getString("ownerId") ?: uid,
            materialId = getString("materialId") ?: materialId,
            title = getString("title") ?: "",
            overview = getString("overview") ?: "",
            headings = (get("headings") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
            concepts = (get("concepts") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
            definitions = (get("definitions") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
            keywords = (get("keywords") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
            formulas = (get("formulas") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
            importantPoints = (get("importantPoints") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
            revisionSection = (get("revisionSection") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
            createdAt = getLong("createdAt") ?: System.currentTimeMillis()
        )
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
                    task.exception ?: RuntimeException("Firebase operation completed without a result or exception.")
                )
            }
        }
    }
}
