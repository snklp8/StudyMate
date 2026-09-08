package com.studymate.app.ui.teacher.batches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studymate.app.data.model.Batch
import com.studymate.app.data.model.BatchStudent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class BatchesUiState {
    object Loading : BatchesUiState()
    data class Success(val batches: List<Batch>) : BatchesUiState()
    object Empty : BatchesUiState()
    data class Error(val message: String) : BatchesUiState()
}

class BatchViewModel : ViewModel() {
    private val _batchesState = MutableStateFlow<BatchesUiState>(BatchesUiState.Loading)
    val batchesState: StateFlow<BatchesUiState> = _batchesState.asStateFlow()

    private val _selectedBatch = MutableStateFlow<Batch?>(null)
    val selectedBatch: StateFlow<Batch?> = _selectedBatch.asStateFlow()

    private val _batchStudents = MutableStateFlow<List<BatchStudent>>(emptyList())
    val batchStudents: StateFlow<List<BatchStudent>> = _batchStudents.asStateFlow()

    private val mockBatches = mutableListOf(
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
        ),
        Batch(
            id = "3",
            name = "B.Tech IT - Sem 3",
            course = "Information Technology",
            semester = 3,
            academicYear = "2026-2027",
            batchCode = "IT3-A2K26",
            studentCount = 45,
            averageScore = 72f
        ),
        Batch(
            id = "4",
            name = "B.Tech ECE - Sem 7",
            course = "Electronics",
            semester = 7,
            academicYear = "2026-2027",
            batchCode = "ECE7-C2K26",
            studentCount = 30,
            averageScore = 85f
        ),
        Batch(
            id = "5",
            name = "MCA - Sem 1",
            course = "Master of Computer Applications",
            semester = 1,
            academicYear = "2026-2027",
            batchCode = "MCA1-2K26",
            studentCount = 60,
            averageScore = 68f
        )
    )

    init {
        loadBatches()
    }

    fun loadBatches() {
        viewModelScope.launch {
            _batchesState.value = BatchesUiState.Loading
            delay(500)
            _batchesState.value = BatchesUiState.Success(mockBatches)
        }
    }

    fun loadBatch(id: String) {
        _selectedBatch.value = mockBatches.find { it.id == id }
    }

    fun loadBatchStudents(batchId: String) {
        _batchStudents.value = List(10) {
            BatchStudent(
                studentId = "std_$it",
                name = "Student ${it + 1}",
                email = "student${it + 1}@university.edu",
                performance = (60 + (it * 3.5f)),
                lastActivity = "2 days ago"
            )
        }
    }

    fun createBatch(name: String, course: String, semester: Int, year: String): String {
        val code = "${course.take(3).uppercase()}$semester-${year.takeLast(2)}"
        val newBatch = Batch(
            id = UUID.randomUUID().toString(),
            name = name,
            course = course,
            semester = semester,
            studentCount = 0,
            averageScore = 0f,
            batchCode = code,
            academicYear = year
        )
        mockBatches.add(newBatch)
        loadBatches()
        return code
    }
}
