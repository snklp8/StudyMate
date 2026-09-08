package com.studymate.app.data.mock

import com.studymate.app.data.model.Test
import com.studymate.app.data.model.TestResult
import com.studymate.app.data.repository.TestRepository
import kotlinx.coroutines.delay

class MockTestRepository : TestRepository {
    private val tests = mutableListOf(
        Test(
            id = "t1",
            teacherId = "teacher1",
            batchId = "b1",
            batchName = "CS-A 2024",
            title = "OS Midterm",
            subject = "Operating Systems",
            totalQuestions = 30,
            duration = 60,
            startDate = System.currentTimeMillis() - 86400000 * 2, // 2 days ago
            endDate = System.currentTimeMillis() - 86400000
        ),
        Test(
            id = "t2",
            teacherId = "teacher1",
            batchId = "b1",
            batchName = "CS-A 2024",
            title = "DBMS Quiz 1",
            subject = "Database Management Systems",
            totalQuestions = 15,
            duration = 30,
            startDate = System.currentTimeMillis() + 86400000, // Tomorrow
            endDate = System.currentTimeMillis() + 86400000 * 2
        )
    )

    private val results = listOf(
        TestResult("t1", "user123", "Alex Smith", 25, 30, 83.3f, System.currentTimeMillis() - 90000000),
        TestResult("t1", "user124", "Maria Garcia", 28, 30, 93.3f, System.currentTimeMillis() - 91000000),
        TestResult("t1", "user125", "James Johnson", 18, 30, 60.0f, System.currentTimeMillis() - 88000000)
    )

    override suspend fun getTests(): List<Test> {
        delay(500)
        return tests.toList()
    }

    override suspend fun createTest(test: Test): Result<Test> {
        delay(500)
        val newTest = test.copy(id = "t${System.currentTimeMillis()}")
        tests.add(newTest)
        return Result.success(newTest)
    }

    override suspend fun getTestResults(testId: String): List<TestResult> {
        delay(500)
        return results.filter { it.testId == testId }
    }
}
