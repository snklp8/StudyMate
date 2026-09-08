package com.studymate.app.data.mock

import com.studymate.app.data.model.Difficulty
import com.studymate.app.data.model.Quiz
import com.studymate.app.data.model.QuizQuestion
import com.studymate.app.data.repository.QuizRepository
import kotlinx.coroutines.delay

class MockQuizRepository : QuizRepository {
    private val quizHistory = mutableListOf<Quiz>()

    override suspend fun generateQuiz(
        materialId: String,
        difficulty: Difficulty,
        count: Int
    ): Result<Quiz> {
        delay(500)
        val questions = List(count) { i ->
            QuizQuestion(
                questionId = "qq_$i",
                question = "Sample MCQ Question ${i + 1} for material $materialId?",
                options = listOf("Option A", "Option B", "Option C", "Option D"),
                correctAnswer = "Option A"
            )
        }
        val quiz = Quiz(
            id = "quiz_${System.currentTimeMillis()}",
            studentId = "user123",
            materialId = materialId,
            materialTitle = "Sample Material Title",
            questions = questions,
            totalQuestions = count
        )
        return Result.success(quiz)
    }

    override suspend fun submitQuiz(quiz: Quiz): Result<Quiz> {
        delay(500)
        val correctCount = quiz.questions.count { it.selectedAnswer == it.correctAnswer }
        val updatedQuiz = quiz.copy(
            score = correctCount,
            percentage = (correctCount.toFloat() / quiz.totalQuestions) * 100f,
            isCompleted = true
        )
        quizHistory.add(updatedQuiz)
        return Result.success(updatedQuiz)
    }

    override suspend fun getQuizHistory(): List<Quiz> {
        delay(500)
        return quizHistory.toList()
    }

    override suspend fun getQuiz(quizId: String): Quiz? {
        delay(300)
        return quizHistory.find { it.id == quizId }
    }
}
