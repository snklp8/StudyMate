package com.studymate.app.ui.teacher

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.studymate.app.navigation.TeacherBottomNav

@Composable
fun TeacherScaffold(
    navController: NavController,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = topBar,
        bottomBar = { TeacherBottomNav(navController = navController) }
    ) { paddingValues ->
        content(paddingValues)
    }
}
