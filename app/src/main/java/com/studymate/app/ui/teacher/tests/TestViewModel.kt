package com.studymate.app.ui.teacher.tests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studymate.app.data.model.Test
import com.studymate.app.data.model.TestResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class TestsUiState {
    object Loading : TestsUiState()
    data class Success(val tests: List<Test>) : TestsUiState()
    object Empty : TestsUiState()
    data class Error(val message: String) : TestsUiState()
}

class TestViewModel : ViewModel() {
    private val _testsState = MutableStateFlow<TestsUiState>(TestsUiState.Loading)
    val testsState: StateFlow<TestsUiState> = _testsState.asStateFlow()

    private val _testResults = MutableStateFlow<List<TestResult>>(emptyList())
    val testResults: StateFlow<List<TestResult>> = _testResults.asStateFlow()

    private val mockTests = mutableListOf(
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
            title = "OS Quiz 1",
            subject = "Operating Systems",
            batchName = "B.Tech CSE - Sem 5",
            totalQuestions = 20,
            duration = 30
        ),
        Test(
            id = "3",
            title = "DBMS Test",
            subject = "Database Systems",
            batchName = "B.Tech CSE - Sem 5",
            totalQuestions = 40,
            duration = 60
        ),
        Test(
            id = "4",
            title = "Network Protocols",
            subject = "Computer Networks",
            batchName = "B.Tech CSE - Sem 7",
            totalQuestions = 30,
            duration = 45
        ),
        Test(
            id = "5",
            title = "Python Basics",
            subject = "Programming",
            batchName = "MCA - Sem 1",
            totalQuestions = 25,
            duration = 40
        )
    )

    init {
        loadTests()
    }

    fun loadTests() {
        viewModelScope.launch {
            _testsState.value = TestsUiState.Loading
            delay(500)
            _testsState.value = TestsUiState.Success(mockTests)
        }
    }

    fun loadTestResults(testId: String) {
        _testResults.value = List(15) {
            val score = 20 + (Math.random() * 30).toInt()
            val total = 50
            TestResult(
                testId = testId,
                studentId = "std_$it",
                studentName = "Student ${it + 1}",
                score = score,
                totalQuestions = total,
                percentage = (score.toFloat() / total * 100),
                submittedAt = System.currentTimeMillis() - (it * 3600000L)
            )
        }
    }

    fun createTest(name: String, subject: String, batchId: String, questions: Int, duration: Int, start: String, end: String) {
        val newTest = Test(
            id = UUID.randomUUID().toString(),
            title = name,
            subject = subject,
            batchId = batchId,
            batchName = "Batch $batchId",
            totalQuestions = questions,
            duration = duration
        )
        mockTests.add(newTest)
        loadTests()
    }
}
