package com.studymate.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.studymate.app.ui.auth.ForgotPasswordScreen
import com.studymate.app.ui.auth.LoginScreen
import com.studymate.app.ui.auth.RegisterScreen
import com.studymate.app.ui.auth.SplashScreen
import com.studymate.app.ui.materials.MaterialDetailScreen
import com.studymate.app.ui.materials.MaterialsListScreen
import com.studymate.app.ui.materials.UploadMaterialScreen
import com.studymate.app.ui.notifications.NotificationsScreen
import com.studymate.app.ui.prediction.PredictorScreen
import com.studymate.app.ui.profile.StudentProfileScreen
import com.studymate.app.ui.profile.TeacherProfileScreen
import com.studymate.app.ui.progress.ProgressScreen
import com.studymate.app.ui.questions.QuestionGeneratorScreen
import com.studymate.app.ui.questions.QuestionResultsScreen
import com.studymate.app.ui.quiz.QuizPlayScreen
import com.studymate.app.ui.quiz.QuizResultScreen
import com.studymate.app.ui.quiz.QuizSetupScreen
import com.studymate.app.ui.settings.SettingsScreen
import com.studymate.app.ui.student.StudentHomeScreen
import com.studymate.app.ui.summary.SummaryScreen
import com.studymate.app.ui.teacher.TeacherDashboardScreen
import com.studymate.app.ui.teacher.analytics.BatchPerformanceScreen
import com.studymate.app.ui.teacher.analytics.StudentPerformanceScreen
import com.studymate.app.ui.teacher.batches.AddStudentsScreen
import com.studymate.app.ui.teacher.batches.BatchDetailScreen
import com.studymate.app.ui.teacher.batches.BatchListScreen
import com.studymate.app.ui.teacher.batches.CreateBatchScreen
import com.studymate.app.ui.teacher.materials.TeacherMaterialsScreen
import com.studymate.app.ui.teacher.tests.CreateTestScreen
import com.studymate.app.ui.teacher.tests.TestListScreen
import com.studymate.app.ui.teacher.tests.TestResultsScreen

@Composable
fun StudyMateNavGraph(navController: NavHostController, startDestination: String = Screen.Splash.route) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth & Global Utility
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController)
        }
        composable(Screen.Notifications.route) {
            NotificationsScreen(navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }

        // Student Screens
        composable(Screen.StudentHome.route) { StudentHomeScreen(navController) }
        composable(Screen.StudentMaterials.route) { MaterialsListScreen(navController) }
        composable(Screen.UploadMaterial.route) { UploadMaterialScreen(navController) }
        composable(Screen.MaterialDetail.route) { backStackEntry ->
            val materialId = backStackEntry.arguments?.getString("materialId") ?: ""
            MaterialDetailScreen(navController = navController, materialId = materialId)
        }
        composable(Screen.Summary.route) { backStackEntry ->
            val materialId = backStackEntry.arguments?.getString("materialId") ?: ""
            SummaryScreen(navController = navController, materialId = materialId)
        }
        composable(Screen.QuestionGenerator.route) { backStackEntry ->
            val materialId = backStackEntry.arguments?.getString("materialId") ?: ""
            QuestionGeneratorScreen(navController = navController, materialId = materialId)
        }
        composable(Screen.GeneratedQuestions.route) { backStackEntry ->
            val materialId = backStackEntry.arguments?.getString("materialId") ?: ""
            QuestionResultsScreen(navController = navController, materialId = materialId)
        }
        composable(Screen.QuizSetup.route) { QuizSetupScreen(navController) }
        composable(Screen.QuizPlay.route) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId") ?: ""
            QuizPlayScreen(navController = navController, quizId = quizId)
        }
        composable(Screen.QuizResult.route) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId") ?: ""
            QuizResultScreen(navController = navController, quizId = quizId)
        }
        composable(Screen.Progress.route) { ProgressScreen(navController) }
        composable(Screen.Predictor.route) { PredictorScreen(navController) }
        composable(Screen.StudentProfile.route) { StudentProfileScreen(navController) }

        // Teacher Screens
        composable(Screen.TeacherDashboard.route) { TeacherDashboardScreen(navController) }
        composable(Screen.BatchList.route) { BatchListScreen(navController) }
        composable(Screen.BatchDetail.route) { backStackEntry ->
            val batchId = backStackEntry.arguments?.getString("batchId") ?: ""
            BatchDetailScreen(batchId = batchId, navController = navController)
        }
        composable(Screen.CreateBatch.route) { CreateBatchScreen(navController) }
        composable(Screen.AddStudents.route) { backStackEntry ->
            val batchId = backStackEntry.arguments?.getString("batchId") ?: ""
            AddStudentsScreen(navController = navController, batchId = batchId)
        }
        composable(Screen.TeacherMaterials.route) { TeacherMaterialsScreen(navController) }
        composable(Screen.TestList.route) { TestListScreen(navController) }
        composable(Screen.CreateTest.route) { CreateTestScreen(navController) }
        composable(Screen.TestResults.route) { backStackEntry ->
            val testId = backStackEntry.arguments?.getString("testId") ?: ""
            TestResultsScreen(testId = testId, navController = navController)
        }
        composable(Screen.StudentPerformance.route) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            StudentPerformanceScreen(navController = navController, studentId = studentId)
        }
        composable(Screen.BatchPerformance.route) { backStackEntry ->
            val batchId = backStackEntry.arguments?.getString("batchId") ?: "default"
            BatchPerformanceScreen(navController = navController, batchId = batchId)
        }
        composable(Screen.TeacherProfile.route) { TeacherProfileScreen(navController) }
    }
}
