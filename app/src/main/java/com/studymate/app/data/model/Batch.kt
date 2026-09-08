package com.studymate.app.data.model

data class Batch(
    val id: String = "",
    val teacherId: String = "",
    val name: String = "",
    val course: String = "",
    val semester: Int = 1,
    val academicYear: String = "",
    val batchCode: String = "",
    val studentCount: Int = 0,
    val averageScore: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
)

data class BatchStudent(
    val studentId: String = "",
    val name: String = "",
    val email: String = "",
    val performance: Float = 0f,
    val lastActivity: String = ""
)
