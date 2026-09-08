package com.studymate.app.data.repository

import com.studymate.app.data.model.Test
import com.studymate.app.data.model.TestResult

interface TestRepository {
    suspend fun getTests(): List<Test>
    suspend fun createTest(test: Test): Result<Test>
    suspend fun getTestResults(testId: String): List<TestResult>
}
