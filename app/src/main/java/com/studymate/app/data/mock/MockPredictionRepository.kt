package com.studymate.app.data.mock

import com.studymate.app.data.model.ImportantTopic
import com.studymate.app.data.model.PerformanceCategory
import com.studymate.app.data.model.Prediction
import com.studymate.app.data.repository.PredictionRepository
import kotlinx.coroutines.delay

class MockPredictionRepository : PredictionRepository {
    override suspend fun getPrediction(): Prediction? {
        delay(500)
        return Prediction(
            id = "pred1",
            studentId = "user123",
            predictedPercentage = 81.5f,
            category = PerformanceCategory.ON_TRACK,
            importantTopics = listOf(
                ImportantTopic("Normalization (DBMS)", 5),
                ImportantTopic("Deadlock Avoidance (OS)", 4),
                ImportantTopic("Graph Traversal (DS)", 4)
            ),
            modelVersion = "v1.2.0"
        )
    }
}
