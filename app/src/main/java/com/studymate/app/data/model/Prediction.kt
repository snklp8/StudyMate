package com.studymate.app.data.model

enum class PerformanceCategory { ON_TRACK, NEEDS_ATTENTION, AT_RISK }

data class Prediction(
    val id: String = "",
    val studentId: String = "",
    val predictedPercentage: Float = 0f,
    val category: PerformanceCategory = PerformanceCategory.ON_TRACK,
    val importantTopics: List<ImportantTopic> = emptyList(),
    val modelVersion: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class ImportantTopic(
    val topic: String = "",
    val importance: Int = 3 // 1-5 stars
)
