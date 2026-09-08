package com.studymate.app.data.repository

import com.studymate.app.data.model.StudentProgress

interface ProgressRepository {
    suspend fun getProgress(): StudentProgress?
}
