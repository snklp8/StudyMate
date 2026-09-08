package com.studymate.app.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studymate.app.data.model.Batch
import com.studymate.app.data.model.Test
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TeacherDashboardUiState(
    val isLoading: Boolean = false,
    val totalStudents: Int = 0,
    val batchCount: Int = 0,
    val materialCount: Int = 0,
    val testCount: Int = 0,
    val batches: List<Batch> = emptyList(),
    val recentTests: List<Test> = emptyList()
)

class TeacherDashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TeacherDashboardUiState(isLoading = true))
    val uiState: StateFlow<TeacherDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            delay(500)

            val mockBatches = listOf(
                Batch(
                    id = "1",
                    name = "B.Tech CSE - Sem 3",
                    course = "Computer Science",
                    semester = 3,
                    academicYear = "2026-2027",
                    batchCode = "CSE3-A2K26",
                    studentCount = 42,
                    averageScore = 76f
                ),
                Batch(
                    id = "2",
                    name = "B.Tech CSE - Sem 5",
                    course = "Computer Science",
                    semester = 5,
                    academicYear = "2026-2027",
                    batchCode = "CSE5-B2K26",
                    studentCount = 38,
                    averageScore = 81f
                )
            )

            val mockTests = listOf(
                Test(
                    id = "1",
                    title = "Data Structures Midterm",
                    subject = "Data Structures",
                    batchName = "B.Tech CSE - Sem 3",
                    totalQuestions = 50,
                    duration = 60
                ),
                Test(
                    id = "2",
                    title = "OS Test 1",
                    subject = "Operating Systems",
                    batchName = "B.Tech CSE - Sem 5",
                    totalQuestions = 30,
                    duration = 45
                )
            )

            _uiState.value = TeacherDashboardUiState(
                isLoading = false,
                totalStudents = 80,
                batchCount = 5,
                materialCount = 12,
                testCount = 8,
                batches = mockBatches,
                recentTests = mockTests
            )
        }
    }
}
