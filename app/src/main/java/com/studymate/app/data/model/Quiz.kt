package com.studymate.app.data.model

data class Quiz(
    val id: String = "",
    val studentId: String = "",
    val materialId: String = "",
    val materialTitle: String = "",
    val questions: List<QuizQuestion> = emptyList(),
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val percentage: Float = 0f,
    val duration: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)

data class QuizQuestion(
    val questionId: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val selectedAnswer: String? = null,
    val isCorrect: Boolean? = null
)
