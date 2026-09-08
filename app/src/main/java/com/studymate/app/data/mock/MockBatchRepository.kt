package com.studymate.app.data.mock

import com.studymate.app.data.model.Batch
import com.studymate.app.data.model.BatchStudent
import com.studymate.app.data.repository.BatchRepository
import kotlinx.coroutines.delay

class MockBatchRepository : BatchRepository {
    private val batches = mutableListOf(
        Batch(
            id = "b1",
            teacherId = "teacher1",
            name = "CS-A 2024",
            course = "B.Tech Computer Science",
            semester = 5,
            academicYear = "2023-2024",
            batchCode = "CSA24",
            studentCount = 60,
            averageScore = 78.5f
        ),
        Batch(
            id = "b2",
            teacherId = "teacher1",
            name = "CS-B 2024",
            course = "B.Tech Computer Science",
            semester = 5,
            academicYear = "2023-2024",
            batchCode = "CSB24",
            studentCount = 58,
            averageScore = 74.2f
        )
    )

    private val students = listOf(
        BatchStudent("user123", "Alex Smith", "alex@university.edu", 82.5f, "2 hours ago"),
        BatchStudent("user124", "Maria Garcia", "maria@university.edu", 91.0f, "5 mins ago"),
        BatchStudent("user125", "James Johnson", "james@university.edu", 65.4f, "1 day ago")
    )

    override suspend fun getBatches(): List<Batch> {
        delay(500)
        return batches.toList()
    }

    override suspend fun getBatch(id: String): Batch? {
        delay(300)
        return batches.find { it.id == id }
    }

    override suspend fun createBatch(batch: Batch): Result<Batch> {
        delay(500)
        val newBatch = batch.copy(id = "b${System.currentTimeMillis()}")
        batches.add(newBatch)
        return Result.success(newBatch)
    }

    override suspend fun getBatchStudents(batchId: String): List<BatchStudent> {
        delay(500)
        return students
    }
}
