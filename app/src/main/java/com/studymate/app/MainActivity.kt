package com.studymate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.studymate.app.navigation.StudyMateNavGraph
import com.studymate.app.ui.theme.StudyMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyMateTheme {
                val navController = rememberNavController()
                StudyMateNavGraph(navController = navController)
            }
        }
    }
}
