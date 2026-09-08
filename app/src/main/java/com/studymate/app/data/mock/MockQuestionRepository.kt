package com.studymate.app.data.mock

import com.studymate.app.data.model.Difficulty
import com.studymate.app.data.model.Question
import com.studymate.app.data.model.QuestionType
import com.studymate.app.data.repository.QuestionRepository
import kotlinx.coroutines.delay

class MockQuestionRepository : QuestionRepository {
    override suspend fun generateQuestions(
        materialId: String,
        type: QuestionType,
        difficulty: Difficulty,
        count: Int
    ): Result<List<Question>> {
        delay(500)
        val questions = mutableListOf<Question>()
        for (i in 1..count) {
            val q = when (type) {
                QuestionType.MCQ -> Question(
                    id = "q_mcq_$i",
                    materialId = materialId,
                    type = type,
                    question = "What is the primary function of an Operating System?",
                    options = listOf("Resource Management", "Word Processing", "Web Browsing", "Compiling Code"),
                    correctAnswer = "Resource Management",
                    difficulty = difficulty
                )
                QuestionType.VIVA -> Question(
                    id = "q_viva_$i",
                    materialId = materialId,
                    type = type,
                    question = "Explain the difference between a process and a thread.",
                    correctAnswer = "A process is a program in execution, while a thread is a lightweight process.",
                    difficulty = difficulty
                )
                QuestionType.LONG_ANSWER -> Question(
                    id = "q_long_$i",
                    materialId = materialId,
                    type = type,
                    question = "Describe the four necessary conditions for a deadlock to occur.",
                    correctAnswer = "Mutual Exclusion, Hold and Wait, No Preemption, and Circular Wait.",
                    difficulty = difficulty
                )
            }
            questions.add(q)
        }
        return Result.success(questions)
    }
}
