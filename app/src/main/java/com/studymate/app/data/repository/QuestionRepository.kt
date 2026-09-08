package com.studymate.app.data.repository

import com.studymate.app.data.model.Difficulty
import com.studymate.app.data.model.Question
import com.studymate.app.data.model.QuestionType

interface QuestionRepository {
    suspend fun generateQuestions(
        materialId: String,
        type: QuestionType,
        difficulty: Difficulty,
        count: Int
    ): Result<List<Question>>
}
