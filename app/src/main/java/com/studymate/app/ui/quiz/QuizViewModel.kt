package com.studymate.app.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studymate.app.data.model.Difficulty
import com.studymate.app.data.model.Quiz as DomainQuiz
import com.studymate.app.data.repository.GeminiQuizRepository
import com.studymate.app.data.repository.SupabaseStorageRepository
import com.studymate.app.ui.materials.MaterialItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class QuizQuestion(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctOptionIndex: Int
)

data class Quiz(
    val id: String,
    val title: String,
    val questions: List<QuizQuestion>
)

enum class QuizState { SETUP, PLAYING, COMPLETED }

class QuizViewModel(
    private val quizRepository: GeminiQuizRepository = GeminiQuizRepository(),
    private val storageRepository: SupabaseStorageRepository = SupabaseStorageRepository()
) : ViewModel() {

    private val _quizState = MutableStateFlow(QuizState.SETUP)
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()

    private val _currentQuiz = MutableStateFlow<Quiz?>(null)
    val currentQuiz: StateFlow<Quiz?> = _currentQuiz.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _availableMaterials = MutableStateFlow<List<MaterialItem>>(emptyList())
    val availableMaterials: StateFlow<List<MaterialItem>> = _availableMaterials.asStateFlow()

    private val _selectedMaterial = MutableStateFlow<MaterialItem?>(null)
    val selectedMaterial: StateFlow<MaterialItem?> = _selectedMaterial.asStateFlow()

    // Map of questionId to selected option index
    val selectedAnswers = mutableMapOf<String, Int>()

    init {
        loadMaterials()
    }

    fun loadMaterials(preferredMaterialId: String? = null) {
        viewModelScope.launch {
            val list = storageRepository.getMaterials()
            val mapped = list.map { m ->
                MaterialItem(
                    id = m.id,
                    title = m.title,
                    subject = m.subject,
                    type = m.type.name,
                    uploadDate = "",
                    fileSize = "",
                    status = "Ready",
                    storagePath = m.fileUrl ?: ""
                )
            }
            _availableMaterials.value = mapped
            if (mapped.isNotEmpty()) {
                val matched = if (!preferredMaterialId.isNullOrBlank()) {
                    mapped.find { it.id == preferredMaterialId } ?: mapped.first()
                } else {
                    mapped.first()
                }
                _selectedMaterial.value = matched
            }
        }
    }

    fun selectMaterial(material: MaterialItem) {
        _selectedMaterial.value = material
    }

    fun generateQuiz(
        materialId: String? = null,
        difficulty: String,
        count: Int,
        onSuccess: (String) -> Unit = {}
    ) {
        val targetMaterial = _selectedMaterial.value
            ?: _availableMaterials.value.find { it.id == materialId }
            ?: _availableMaterials.value.firstOrNull()

        val actualMaterialId = targetMaterial?.id ?: materialId ?: "default"
        val docTitle = targetMaterial?.title ?: "Practice Quiz"

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = quizRepository.generateQuizQuestions(
                materialId = actualMaterialId,
                difficulty = difficulty,
                count = count
            )

            result.fold(
                onSuccess = { generatedQuestions ->
                    val quizId = UUID.randomUUID().toString()
                    _currentQuiz.value = Quiz(
                        id = quizId,
                        title = docTitle,
                        questions = generatedQuestions
                    )
                    _quizState.value = QuizState.PLAYING
                    _currentQuestionIndex.value = 0
                    selectedAnswers.clear()
                    _errorMessage.value = null
                    _isLoading.value = false
                    onSuccess(quizId)
                },
                onFailure = { error ->
                    _errorMessage.value = error.localizedMessage ?: "Failed to generate quiz with Gemini."
                    _isLoading.value = false
                }
            )
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun selectAnswer(questionId: String, optionIndex: Int) {
        selectedAnswers[questionId] = optionIndex
    }

    fun nextQuestion() {
        if (_currentQuestionIndex.value < (_currentQuiz.value?.questions?.size ?: 1) - 1) {
            _currentQuestionIndex.value++
        }
    }

    fun previousQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value--
        }
    }

    fun submitQuiz() {
        _quizState.value = QuizState.COMPLETED
        val quiz = _currentQuiz.value ?: return
        val total = quiz.questions.size
        var correct = 0
        quiz.questions.forEach { q ->
            if (selectedAnswers[q.id] == q.correctOptionIndex) correct++
        }
        val pct = if (total > 0) (correct.toFloat() / total * 100) else 0f

        viewModelScope.launch {
            val domainQuiz = DomainQuiz(
                id = quiz.id,
                materialId = _selectedMaterial.value?.id ?: "",
                materialTitle = quiz.title,
                score = correct,
                totalQuestions = total,
                percentage = pct,
                isCompleted = true
            )
            quizRepository.submitQuiz(domainQuiz)
        }
    }

    fun resetQuiz() {
        _quizState.value = QuizState.SETUP
        _currentQuiz.value = null
        selectedAnswers.clear()
        _currentQuestionIndex.value = 0
    }
}
