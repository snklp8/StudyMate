package com.studymate.app.ui.prediction

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ImportantTopic(
    val name: String,
    val stars: Int
)

data class PredictionState(
    val percentage: Int = 82,
    val status: String = "ON_TRACK",
    val description: String = "Based on your study patterns and quiz performance, you are on track to achieve an excellent grade.",
    val importantTopics: List<ImportantTopic> = listOf(
        ImportantTopic("Trees", 5),
        ImportantTopic("Graphs", 5),
        ImportantTopic("Binary Search", 4),
        ImportantTopic("Linked Lists", 3),
        ImportantTopic("Sorting", 3)
    )
)

class PredictorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PredictionState())
    val uiState: StateFlow<PredictionState> = _uiState.asStateFlow()
}
