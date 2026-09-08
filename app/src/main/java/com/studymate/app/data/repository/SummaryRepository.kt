package com.studymate.app.data.repository

import com.studymate.app.data.model.Summary

interface SummaryRepository {
    suspend fun getSummary(materialId: String): Summary?
    suspend fun generateSummary(materialId: String): Result<Summary>
}
