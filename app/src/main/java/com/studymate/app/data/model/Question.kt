package com.studymate.app.data.model

enum class QuestionType { LONG_ANSWER, VIVA, MCQ }
enum class Difficulty { EASY, MEDIUM, HARD }

data class Question(
    val id: String = "",
    val materialId: String = "",
    val type: QuestionType = QuestionType.MCQ,
    val question: String = "",
    val options: List<String>? = null,
    val correctAnswer: String = "",
    val difficulty: Difficulty = Difficulty.MEDIUM
)
