package com.studymate.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.studymate.app.data.model.UserRole
import com.studymate.app.navigation.Screen
import com.studymate.app.ui.components.StudyMateLogoBadge
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        val minDelayJob = launch { delay(1600) }
        authViewModel.checkCurrentSession { user ->
            launch {
                minDelayJob.join()
                val targetRoute = when {
                    user == null -> Screen.Login.route
                    user.role == UserRole.TEACHER -> Screen.TeacherDashboard.route
                    else -> Screen.StudentHome.route
                }
                navController.navigate(targetRoute) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        StudyMateLogoBadge(
            iconSize = 76.dp,
            showTagline = true
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CAMPUS LEARNING & ASSESSMENT",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
