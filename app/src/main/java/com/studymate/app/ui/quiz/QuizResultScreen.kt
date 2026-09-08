package com.studymate.app.ui.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Dangerous
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.studymate.app.navigation.Screen
import com.studymate.app.ui.components.StatCard
import com.studymate.app.ui.components.StudyMateButton
import com.studymate.app.ui.components.StudyMateCard
import com.studymate.app.ui.components.StudyMateOutlinedButton
import com.studymate.app.ui.theme.BorderSubtle
import com.studymate.app.ui.theme.SuccessGreen
import com.studymate.app.ui.theme.SuccessGreenContainer
import com.studymate.app.ui.theme.WeakRed
import com.studymate.app.ui.theme.WeakRedContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultScreen(
    navController: NavController,
    quizId: String,
    viewModel: QuizViewModel = viewModel()
) {
    val quiz by viewModel.currentQuiz.collectAsState()
    var showReviewAnswers by remember { mutableStateOf(false) }

    if (quiz == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No assessment data found.")
        }
        return
    }

    val totalQuestions = quiz!!.questions.size
    val selectedAnswers = viewModel.selectedAnswers
    var correctCount = 0
    var incorrectCount = 0
    var skippedCount = 0

    quiz!!.questions.forEach { q ->
        val selected = selectedAnswers[q.id]
        if (selected == null) skippedCount++
        else if (selected == q.correctOptionIndex) correctCount++
        else incorrectCount++
    }

    val attemptedCount = totalQuestions - skippedCount
    val percentage = if (totalQuestions > 0) (correctCount.toFloat() / totalQuestions * 100).toInt() else 0

    val (performanceLabel, performanceColor, performanceBg) = when {
        percentage >= 80 -> Triple("Mastery Level Achieved", SuccessGreen, SuccessGreenContainer)
        percentage >= 60 -> Triple("Good Competency", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
        else -> Triple("Review Recommended", WeakRed, WeakRedContainer)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Quiz Results", fontWeight = FontWeight.Bold)
                        Text(
                            "Performance analysis & topic breakdown",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Hero Score Card
                StudyMateCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 22.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = performanceBg
                        ) {
                            Text(
                                text = performanceLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = performanceColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = performanceColor
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Score: $correctCount / $totalQuestions ($attemptedCount attempted)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Stat Metrics Row: 3 cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Correct",
                        value = "$correctCount",
                        icon = Icons.Outlined.CheckCircle,
                        tint = SuccessGreen,
                        containerColor = SuccessGreenContainer
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Incorrect",
                        value = "$incorrectCount",
                        icon = Icons.Outlined.Dangerous,
                        tint = WeakRed,
                        containerColor = WeakRedContainer
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Time Taken",
                        value = "3m 42s",
                        icon = Icons.Outlined.Timer,
                        tint = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }

            // Strengths & Weaknesses (Exact Figma Progress design)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Strengths & Weaknesses",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Strengths Card
                        StudyMateCard(
                            modifier = Modifier.weight(1f),
                            contentPadding = 12.dp
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Strengths",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("Binary Trees", "BST Invariants", "Search Bounds").forEach { tag ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = SuccessGreenContainer,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = tag,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = SuccessGreen,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Weaknesses Card
                        StudyMateCard(
                            modifier = Modifier.weight(1f),
                            contentPadding = 12.dp
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = null,
                                        tint = WeakRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Weaknesses",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = WeakRed
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("AVL Rotations", "Recursion Depth", "Deletion Cases").forEach { tag ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = WeakRedContainer,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = tag,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = WeakRed,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recommended Review Area Card
            item {
                StudyMateCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    contentPadding = 14.dp
                ) {
                    Column {
                        Text(
                            text = "Recommended Review Area",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Focus on AVL Tree Double Rotations (LR & RL). Review the step-by-step summary in CS 201 before your next attempt.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Actions: Review Answers, Try Again, Back to Progress
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    StudyMateOutlinedButton(
                        text = if (showReviewAnswers) "Hide Answer Breakdown" else "Review Answers",
                        onClick = { showReviewAnswers = !showReviewAnswers },
                        leadingIcon = Icons.Default.Visibility,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StudyMateOutlinedButton(
                            text = "Try Again",
                            onClick = {
                                viewModel.resetQuiz()
                                navController.navigate(Screen.QuizSetup.route) {
                                    popUpTo(Screen.StudentHome.route) { inclusive = false }
                                }
                            },
                            leadingIcon = Icons.Default.Refresh,
                            modifier = Modifier.weight(1f)
                        )

                        StudyMateButton(
                            text = "Back to Progress",
                            onClick = {
                                navController.navigate(Screen.Progress.route) {
                                    popUpTo(Screen.StudentHome.route) { saveState = true }
                                }
                            },
                            leadingIcon = Icons.AutoMirrored.Filled.TrendingUp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Review Answers List (Animated Visibility)
            if (showReviewAnswers) {
                item {
                    Text(
                        text = "DETAILED QUESTION REVIEW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                itemsIndexed(quiz!!.questions) { index, q ->
                    val selected = selectedAnswers[q.id]
                    val isCorrect = selected != null && selected == q.correctOptionIndex
                    val isSkipped = selected == null

                    StudyMateCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 14.dp
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Question ${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                val (statusText, statusColor, statusBg) = when {
                                    isCorrect -> Triple("Correct", SuccessGreen, SuccessGreenContainer)
                                    isSkipped -> Triple("Skipped", MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
                                    else -> Triple("Incorrect", WeakRed, WeakRedContainer)
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = statusBg
                                ) {
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = q.text,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(thickness = 1.dp, color = BorderSubtle)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Correct Answer
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Correct: ",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                                Text(
                                    text = q.options.getOrElse(q.correctOptionIndex) { "" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (!isCorrect && !isSkipped && selected != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Your choice: ",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = WeakRed
                                    )
                                    Text(
                                        text = q.options.getOrElse(selected) { "" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
