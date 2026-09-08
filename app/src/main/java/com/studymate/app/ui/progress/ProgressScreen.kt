package com.studymate.app.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.studymate.app.navigation.Screen
import com.studymate.app.ui.components.SectionHeader
import com.studymate.app.ui.components.StatCard
import com.studymate.app.ui.components.StudyMateCard
import com.studymate.app.ui.components.TopicChip
import com.studymate.app.ui.student.StudentScaffold
import com.studymate.app.ui.theme.DocBlue
import com.studymate.app.ui.theme.SuccessGreen
import com.studymate.app.ui.theme.SuccessGreenContainer
import com.studymate.app.ui.theme.WarningOrange
import com.studymate.app.ui.theme.WarningOrangeContainer
import com.studymate.app.ui.theme.WeakRed
import com.studymate.app.ui.theme.WeakRedContainer

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProgressScreen(
    navController: NavController,
    viewModel: ProgressViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    StudentScaffold(
        navController = navController,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Academic Progress", fontWeight = FontWeight.Bold)
                        Text(
                            "Mastery, accuracy & subject analytics",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Overall Mastery Hero Card
                StudyMateCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 22.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "OVERALL MASTERY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${state.overallPercentage}%",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessGreenContainer
                        ) {
                            Text(
                                text = "On Track",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { state.overallPercentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${state.correctAnswers} of ${state.questionsAttempted} questions mastered across 4 key topics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Stat Metrics Grid
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Quiz Avg",
                        value = "${state.quizAverage}%",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        iconTint = MaterialTheme.colorScheme.primary
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Questions",
                        value = state.questionsAttempted.toString(),
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        iconTint = DocBlue
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Correct",
                        value = state.correctAnswers.toString(),
                        valueColor = SuccessGreen,
                        icon = Icons.Default.CheckCircle,
                        iconTint = SuccessGreen
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Incorrect",
                        value = state.incorrectAnswers.toString(),
                        valueColor = WeakRed,
                        icon = Icons.Default.Cancel,
                        iconTint = WeakRed
                    )
                }
            }

            // Topic Performance Breakdown
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader(
                    title = "Topic Performance",
                    subtitle = "Competency score per syllabus unit"
                )
            }

            items(state.topics) { topic ->
                StudyMateCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 14.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = topic.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        val (statusText, statusColor, statusBg) = if (topic.isWeak) {
                            Triple("Needs Focus", WeakRed, WeakRedContainer)
                        } else if (topic.score >= 85) {
                            Triple("Mastered", SuccessGreen, SuccessGreenContainer)
                        } else {
                            Triple("Good", DocBlue, MaterialTheme.colorScheme.primaryContainer)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = statusBg
                            ) {
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.padding(start = 8.dp))
                            Text(
                                text = "${topic.score}%",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { topic.score / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = if (topic.isWeak) WeakRed else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            // Weak Topics Quick Action
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader(
                    title = "Areas for Improvement",
                    subtitle = "Tap a topic to launch target practice"
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.topics.filter { it.isWeak }.forEach { topic ->
                        TopicChip(
                            topic = topic.name,
                            isWeak = true,
                            onClick = { navController.navigate(Screen.QuizSetup.route) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

