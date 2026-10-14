package com.studymate.app.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.studymate.app.data.model.Material
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

data class StudentHomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "Student",
    val recentMaterials: List<Material> = emptyList(),
    val continueMaterial: Material? = null,
    val quizAverage: String = "—",
    val materialsCount: Int = 0,
    val quizzesCount: Int = 0,
    val batchesCount: Int = 0,
    val weeklyProgressPercent: Float = 0f,
    val weeklyProgressText: String = "",
    val weeklyModulesText: String = "",
    val weakTopics: List<String> = emptyList()
)

class StudentHomeViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storageRepo: SupabaseStorageRepository = SupabaseStorageRepository(),
    private val batchRepo: BatchRepository = BatchRepository(),
    private val quizRepo: QuizRepository = GeminiQuizRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentHomeUiState())
    val uiState: StateFlow<StudentHomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun refresh() {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val user = auth.currentUser
                val uid = user?.uid

                // 1. Fetch user profile name
                val userName = withContext(Dispatchers.IO) {
                    if (uid != null) {
                        try {
                            val doc = firestore.collection("users").document(uid).get().await()
                            val name = doc.getString("name")?.trim()
                            if (!name.isNullOrBlank()) {
                                name
                            } else {
                                user.displayName ?: user.email?.substringBefore('@') ?: "Student"
                            }
                        } catch (_: Exception) {
                            user.displayName ?: user.email?.substringBefore('@') ?: "Student"
                        }
                    } else {
                        "Student"
                    }
                }

                // 2. Concurrently fetch materials, batches, and quiz history
                val materialsDeferred = async(Dispatchers.IO) {
                    try { storageRepo.getMaterials() } catch (_: Exception) { emptyList() }
                }
                val batchesDeferred = async(Dispatchers.IO) {
                    try { batchRepo.getStudentBatches() } catch (_: Exception) { emptyList() }
                }
                val quizzesDeferred = async(Dispatchers.IO) {
                    try { quizRepo.getQuizHistory() } catch (_: Exception) { emptyList() }
                }

                val materials = materialsDeferred.await()
                val batches = batchesDeferred.await()
                val quizzes = quizzesDeferred.await()

                // Calculate quiz average
                val quizAverage = if (quizzes.isNotEmpty()) {
                    val avg = quizzes.map { it.percentage }.average()
                    "${avg.roundToInt()}%"
                } else {
                    "—"
                }

                // Calculate weekly momentum based on quizzes & materials
                val completedQuizzes = quizzes.count { it.percentage >= 60f }
                val totalItems = maxOf(quizzes.size, materials.size)
                val progressPercent = if (totalItems > 0) {
                    (completedQuizzes.toFloat() / totalItems.toFloat()).coerceIn(0.1f, 1f)
                } else {
                    0f
                }
                val progressHeadline = when {
                    progressPercent >= 0.8f -> "Outstanding momentum! You're mastering your subjects."
                    progressPercent >= 0.5f -> "Great progress! Keep up the consistent study routine."
                    progressPercent > 0f -> "Off to a solid start. Tackle a new quiz or summary today."
                    else -> "Welcome to StudyMate! Join a batch or explore materials to begin."
                }
                val progressModules = if (totalItems > 0) {
                    "$completedQuizzes quizzes passed • ${quizzes.size} attempts total"
                } else {
                    "0 modules completed • Get started below"
                }

                // Continue learning: most recent study material
                val continueMaterial = materials.firstOrNull()

                // Focus topics: derived from lower scoring quizzes or material subjects
                val weakQuizzes = quizzes.filter { it.percentage < 70f }
                val derivedTopics = if (weakQuizzes.isNotEmpty()) {
                    weakQuizzes.map { it.materialTitle.ifBlank { "Practice Quiz" } }.distinct().take(4)
                } else if (materials.isNotEmpty()) {
                    materials.map { it.subject.ifBlank { "General" } }.distinct().take(4)
                } else {
                    emptyList()
                }

                _uiState.value = StudentHomeUiState(
                    isLoading = false,
                    userName = userName,
                    recentMaterials = materials.take(4),
                    continueMaterial = continueMaterial,
                    quizAverage = quizAverage,
                    materialsCount = materials.size,
                    quizzesCount = quizzes.size,
                    batchesCount = batches.size,
                    weeklyProgressPercent = progressPercent,
                    weeklyProgressText = progressHeadline,
                    weeklyModulesText = progressModules,
                    weakTopics = derivedTopics
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userName = auth.currentUser?.displayName ?: "Student"
                )
            }
        }
    }
}
