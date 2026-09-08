package com.studymate.app.data.repository

import com.studymate.app.data.model.Difficulty
import com.studymate.app.data.model.Quiz

interface QuizRepository {
    suspend fun generateQuiz(materialId: String, difficulty: Difficulty, count: Int): Result<Quiz>
    suspend fun submitQuiz(quiz: Quiz): Result<Quiz>
    suspend fun getQuizHistory(): List<Quiz>
    suspend fun getQuiz(quizId: String): Quiz?
}
