package com.studymate.app.data.repository

import com.studymate.app.data.model.Batch
import com.studymate.app.data.model.BatchStudent

interface BatchRepository {
    suspend fun getBatches(): List<Batch>
    suspend fun getBatch(id: String): Batch?
    suspend fun createBatch(batch: Batch): Result<Batch>
    suspend fun getBatchStudents(batchId: String): List<BatchStudent>
}
