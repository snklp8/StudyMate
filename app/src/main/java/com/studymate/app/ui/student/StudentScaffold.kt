package com.studymate.app.ui.student

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.studymate.app.navigation.StudentBottomNav

@Composable
fun StudentScaffold(
    navController: NavController,
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = topBar,
        bottomBar = { StudentBottomNav(navController = navController) },
        floatingActionButton = floatingActionButton
    ) { paddingValues ->
        content(paddingValues)
    }
}

