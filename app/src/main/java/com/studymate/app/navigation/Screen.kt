package com.studymate.app.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Notifications : Screen("notifications")
    object Settings : Screen("settings")

    // Student
    object StudentHome : Screen("student_home")
    object StudentMaterials : Screen("student_materials")
    object UploadMaterial : Screen("upload_material")
    object MaterialDetail : Screen("material_detail/{materialId}") {
        fun createRoute(materialId: String) = "material_detail/$materialId"
    }
    object Summary : Screen("summary/{materialId}") {
        fun createRoute(materialId: String) = "summary/$materialId"
    }
    object QuestionGenerator : Screen("question_generator/{materialId}") {
        fun createRoute(materialId: String) = "question_generator/$materialId"
    }
    object GeneratedQuestions : Screen("generated_questions/{materialId}") {
        fun createRoute(materialId: String) = "generated_questions/$materialId"
    }
    object QuizSetup : Screen("quiz_setup")
    object QuizPlay : Screen("quiz_play/{quizId}") {
        fun createRoute(quizId: String) = "quiz_play/$quizId"
    }
    object QuizResult : Screen("quiz_result/{quizId}") {
        fun createRoute(quizId: String) = "quiz_result/$quizId"
    }
    object Progress : Screen("progress")
    object Predictor : Screen("predictor")
    object StudentProfile : Screen("student_profile")

    // Teacher
    object TeacherDashboard : Screen("teacher_dashboard")
    object BatchList : Screen("batch_list")
    object BatchDetail : Screen("batch_detail/{batchId}") {
        fun createRoute(batchId: String) = "batch_detail/$batchId"
    }
    object CreateBatch : Screen("create_batch")
    object AddStudents : Screen("add_students/{batchId}") {
        fun createRoute(batchId: String) = "add_students/$batchId"
    }
    object TeacherMaterials : Screen("teacher_materials")
    object TestList : Screen("test_list")
    object CreateTest : Screen("create_test")
    object TestResults : Screen("test_results/{testId}") {
        fun createRoute(testId: String) = "test_results/$testId"
    }
    object StudentPerformance : Screen("student_performance/{studentId}") {
        fun createRoute(studentId: String) = "student_performance/$studentId"
    }
    object BatchPerformance : Screen("batch_performance/{batchId}") {
        fun createRoute(batchId: String = "default") = "batch_performance/$batchId"
    }
    object TeacherProfile : Screen("teacher_profile")
}
