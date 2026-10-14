package com.studymate.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.studymate.app.data.repository.BatchRepository
import com.studymate.app.data.repository.GeminiQuizRepository
import com.studymate.app.data.repository.QuizRepository
import com.studymate.app.data.repository.SupabaseStorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

data class StudentProfileUiState(
    val isLoading: Boolean = true,
    val name: String = "Student",
    val email: String = "",
    val initials: String = "S",
    val materialsCount: Int = 0,
    val quizzesCount: Int = 0,
    val batchesCount: Int = 0,
    val averageScore: String = "—"
)

class StudentProfileViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storageRepo: SupabaseStorageRepository = SupabaseStorageRepository(),
    private val batchRepo: BatchRepository = BatchRepository(),
    private val quizRepo: QuizRepository = GeminiQuizRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentProfileUiState())
    val uiState: StateFlow<StudentProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun refresh() {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = auth.currentUser
            val uid = user?.uid
            val userEmail = user?.email ?: ""

            val userName = withContext(Dispatchers.IO) {
                if (uid != null) {
                    try {
                        val doc = firestore.collection("users").document(uid).get().await()
                        doc.getString("name")?.trim().takeIf { !it.isNullOrBlank() }
                            ?: user.displayName
                            ?: userEmail.substringBefore('@')
                    } catch (_: Exception) {
                        user.displayName ?: userEmail.substringBefore('@')
                    }
                } else {
                    "Student"
                }
            } ?: "Student"

            val initials = userName.split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .map { it.first().uppercaseChar() }
                .joinToString("")
                .ifBlank { "S" }

            val materialsDeferred = async(Dispatchers.IO) {
                try { storageRepo.getMaterials().size } catch (_: Exception) { 0 }
            }
            val batchesDeferred = async(Dispatchers.IO) {
                try { batchRepo.getStudentBatches().size } catch (_: Exception) { 0 }
            }
            val quizzesDeferred = async(Dispatchers.IO) {
                try { quizRepo.getQuizHistory() } catch (_: Exception) { emptyList() }
            }

            val materialsCount = materialsDeferred.await()
            val batchesCount = batchesDeferred.await()
            val quizzes = quizzesDeferred.await()
            val quizzesCount = quizzes.size
            val averageScore = if (quizzes.isNotEmpty()) {
                "${quizzes.map { it.percentage }.average().roundToInt()}%"
            } else {
                "—"
            }

            _uiState.value = StudentProfileUiState(
                isLoading = false,
                name = userName,
                email = userEmail,
                initials = initials,
                materialsCount = materialsCount,
                quizzesCount = quizzesCount,
                batchesCount = batchesCount,
                averageScore = averageScore
            )
        }
    }
}
