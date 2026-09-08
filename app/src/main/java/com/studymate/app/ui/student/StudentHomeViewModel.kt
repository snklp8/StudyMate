package com.studymate.app.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studymate.app.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudentHomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val recentMaterials: List<MockMaterial> = emptyList(),
    val quizAverage: String = "",
    val materialsCount: Int = 0,
    val quizzesCount: Int = 0,
    val weakTopics: List<String> = emptyList(),
    val predictedPerformance: Float = 0f
)

data class MockMaterial(
    val id: String,
    val title: String,
    val type: String, // PDF, PPT, YOUTUBE
    val subject: String
)

class StudentHomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(StudentHomeUiState())
    val uiState: StateFlow<StudentHomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            delay(500)
            _uiState.value = StudentHomeUiState(
                isLoading = false,
                userName = "Sankalp",
                recentMaterials = listOf(
                    MockMaterial("1", "Data Structures PDF", "PDF", "Computer Science"),
                    MockMaterial("2", "Operating Systems PPT", "PPT", "Computer Science"),
                    MockMaterial("3", "DBMS YouTube", "YOUTUBE", "Computer Science")
                ),
                quizAverage = "78%",
                materialsCount = 12,
                quizzesCount = 8,
                weakTopics = listOf("Graphs", "Recursion", "Trees"),
                predictedPerformance = 82f
            )
        }
    }
}
