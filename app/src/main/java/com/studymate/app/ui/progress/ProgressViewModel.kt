package com.studymate.app.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studymate.app.data.repository.GeminiQuizRepository
import com.studymate.app.data.repository.QuizRepository
import com.studymate.app.data.repository.SupabaseStorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class TopicProgress(
    val name: String,
    val score: Int,
    val isWeak: Boolean,
    val materialId: String = ""
)

data class ProgressUiState(
    val isLoading: Boolean = true,
    val quizAverage: Int = 0,
    val questionsAttempted: Int = 0,
    val correctAnswers: Int = 0,
    val incorrectAnswers: Int = 0,
    val topics: List<TopicProgress> = emptyList()
)

class ProgressViewModel(
    private val storageRepo: SupabaseStorageRepository = SupabaseStorageRepository(),
    private val quizRepo: QuizRepository = GeminiQuizRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        loadProgressData()
    }

    fun refresh() {
        loadProgressData()
    }

    private fun loadProgressData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val materialsDeferred = async(Dispatchers.IO) {
                    try { storageRepo.getMaterials() } catch (_: Exception) { emptyList() }
                }
                val quizzesDeferred = async(Dispatchers.IO) {
                    try { quizRepo.getQuizHistory() } catch (_: Exception) { emptyList() }
                }

                val materials = materialsDeferred.await()
                val quizzes = quizzesDeferred.await()

                val questionsAttempted = quizzes.sumOf { it.totalQuestions }
                val correctAnswers = quizzes.sumOf { it.score }
                val incorrectAnswers = maxOf(0, questionsAttempted - correctAnswers)
                val quizAverage = if (quizzes.isNotEmpty()) {
                    quizzes.map { it.percentage }.average().roundToInt()
                } else {
                    0
                }

                // Recent 4 uploaded materials as topics
                val recentMaterials = materials.take(4)
                val topics = recentMaterials.map { mat ->
                    val matchingQuizzes = quizzes.filter { q ->
                        (q.materialId.isNotBlank() && q.materialId == mat.id) ||
                        (q.materialTitle.isNotBlank() && q.materialTitle.equals(mat.title, ignoreCase = true))
                    }
                    val score = if (matchingQuizzes.isNotEmpty()) {
                        matchingQuizzes.map { it.percentage }.average().roundToInt()
                    } else if (quizzes.isNotEmpty()) {
                        // Fallback to average score if this specific material has no standalone quiz yet
                        quizAverage
                    } else {
                        0
                    }
                    TopicProgress(
                        name = mat.title.ifBlank { mat.subject.ifBlank { "Study Material" } },
                        score = score,
                        isWeak = score in 1..65,
                        materialId = mat.id
                    )
                }

                _uiState.value = ProgressUiState(
                    isLoading = false,
                    quizAverage = quizAverage,
                    questionsAttempted = questionsAttempted,
                    correctAnswers = correctAnswers,
                    incorrectAnswers = incorrectAnswers,
                    topics = topics
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
