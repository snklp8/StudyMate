package com.studymate.app

import android.app.Application
import com.studymate.app.data.repository.FirestoreSummaryRepository

class StudyMateApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Provide application context to repositories so PDFBox and file extractors
        // can initialize without a Context constructor parameter.
        FirestoreSummaryRepository.appContext = applicationContext
        com.studymate.app.data.repository.GeminiQuestionRepository.appContext = applicationContext
    }
}
