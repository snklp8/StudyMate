package com.studymate.app.data.model

data class Test(
    val id: String = "",
    val teacherId: String = "",
    val batchId: String = "",
    val batchName: String = "",
    val title: String = "",
    val subject: String = "",
    val totalQuestions: Int = 0,
    val duration: Int = 0, // minutes
    val startDate: Long = 0,
    val endDate: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class TestResult(
    val testId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val percentage: Float = 0f,
    val submittedAt: Long = 0
)
