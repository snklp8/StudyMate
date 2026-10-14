package com.studymate.app

import android.app.Application
import com.studymate.app.data.repository.FirestoreSummaryRepository

class StudyMateApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Provide application context to FirestoreSummaryRepository so PDFBox
        // can initialize its resource loader without a Context constructor parameter.
        FirestoreSummaryRepository.appContext = applicationContext
    }
}
