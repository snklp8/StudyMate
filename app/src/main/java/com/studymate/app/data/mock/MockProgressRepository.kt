package com.studymate.app.data.mock

import com.studymate.app.data.model.RecentScore
import com.studymate.app.data.model.StudentProgress
import com.studymate.app.data.model.TopicProgress
import com.studymate.app.data.repository.ProgressRepository
import kotlinx.coroutines.delay

class MockProgressRepository : ProgressRepository {
    override suspend fun getProgress(): StudentProgress? {
        delay(500)
        return StudentProgress(
            studentId = "user123",
            overallAverage = 82.5f,
            quizAverage = 78.0f,
            totalQuizzes = 15,
            questionsAttempted = 150,
            correctAnswers = 117,
            incorrectAnswers = 33,
            topicProgress = listOf(
                TopicProgress("Operating Systems", 85f, false),
                TopicProgress("Data Structures", 92f, false),
                TopicProgress("DBMS", 65f, true)
            ),
            recentScores = listOf(
                RecentScore("OS Quiz 1", 90f),
                RecentScore("DBMS Quiz 2", 60f),
                RecentScore("DS Quiz 3", 85f)
            ),
            materialsCompleted = 8
        )
    }
}
