package com.studymate.app.data.model

data class StudentProgress(
    val studentId: String = "",
    val overallAverage: Float = 0f,
    val quizAverage: Float = 0f,
    val totalQuizzes: Int = 0,
    val questionsAttempted: Int = 0,
    val correctAnswers: Int = 0,
    val incorrectAnswers: Int = 0,
    val topicProgress: List<TopicProgress> = emptyList(),
    val recentScores: List<RecentScore> = emptyList(),
    val materialsCompleted: Int = 0
)

data class TopicProgress(
    val topic: String = "",
    val percentage: Float = 0f,
    val isWeak: Boolean = false
)

data class RecentScore(
    val quizName: String = "",
    val percentage: Float = 0f
)
