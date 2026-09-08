package com.studymate.app.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

class QuizViewModel : ViewModel() {
    private val _quizState = MutableStateFlow(QuizState.SETUP)
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()

    private val _currentQuiz = MutableStateFlow<Quiz?>(null)
    val currentQuiz: StateFlow<Quiz?> = _currentQuiz.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    // Map of questionId to selected option index
    val selectedAnswers = mutableMapOf<String, Int>()

    fun generateQuiz(materialId: String, difficulty: String, count: Int) {
        viewModelScope.launch {
            // Mock generating quiz
            delay(500)
            val questions = List(count) { i ->
                QuizQuestion(
                    id = "q$i",
                    text = "Sample mock question ${i + 1} for $materialId?",
                    options = listOf("Option A", "Option B", "Option C", "Option D"),
                    correctOptionIndex = (0..3).random()
                )
            }
            _currentQuiz.value = Quiz("quiz1", "Mock Quiz", questions)
            _quizState.value = QuizState.PLAYING
            _currentQuestionIndex.value = 0
            selectedAnswers.clear()
        }
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
    }
    
    fun resetQuiz() {
        _quizState.value = QuizState.SETUP
        _currentQuiz.value = null
        selectedAnswers.clear()
    }
}
