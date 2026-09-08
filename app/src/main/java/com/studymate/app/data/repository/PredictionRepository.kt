package com.studymate.app.data.repository

import com.studymate.app.data.model.Prediction

interface PredictionRepository {
    suspend fun getPrediction(): Prediction?
}
