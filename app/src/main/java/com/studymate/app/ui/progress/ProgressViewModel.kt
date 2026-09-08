package com.studymate.app.ui.progress

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TopicProgress(val name: String, val score: Int, val isWeak: Boolean)

data class ProgressUiState(
    val overallPercentage: Int = 78,
    val quizAverage: Int = 82,
    val questionsAttempted: Int = 145,
    val correctAnswers: Int = 118,
    val incorrectAnswers: Int = 27,
    val recentScores: List<Int> = listOf(80, 85, 70, 90),
    val topics: List<TopicProgress> = listOf(
        TopicProgress("Arrays", 90, false),
        TopicProgress("Linked List", 82, false),
        TopicProgress("Trees", 65, true),
        TopicProgress("Graphs", 58, true)
    )
)

class ProgressViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()
}
