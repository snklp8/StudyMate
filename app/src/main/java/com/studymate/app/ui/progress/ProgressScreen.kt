package com.studymate.app.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.studymate.app.ui.components.EmptyState
import com.studymate.app.ui.components.SectionHeader
import com.studymate.app.ui.components.StatCard
import com.studymate.app.ui.components.StudyMateCard
import com.studymate.app.ui.student.StudentScaffold
import com.studymate.app.ui.theme.DocBlue
import com.studymate.app.ui.theme.SuccessGreen
import com.studymate.app.ui.theme.SuccessGreenContainer
import com.studymate.app.ui.theme.WeakRed
import com.studymate.app.ui.theme.WeakRedContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    navController: NavController,
    viewModel: ProgressViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    StudentScaffold(
        navController = navController,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Academic Progress", fontWeight = FontWeight.Bold)
                        Text(
                            "Quiz accuracy & topic analytics",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@StudentScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Stat Metrics Grid
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Quiz Avg",
                        value = if (state.questionsAttempted > 0) "${state.quizAverage}%" else "—",
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

            // Topic Performance Breakdown (from uploaded materials)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader(
                    title = "Recent Material Topics",
                    subtitle = "Competency based on recent uploaded materials"
                )
            }

            if (state.topics.isEmpty()) {
                item {
                    EmptyState(
                        title = "No Materials Uploaded",
                        subtitle = "Upload study materials or join a batch to see topic analytics."
                    )
                }
            } else {
                items(state.topics) { topic ->
                    StudyMateCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = if (topic.materialId.isNotBlank()) {
                            { navController.navigate(Screen.MaterialDetail.createRoute(topic.materialId)) }
                        } else null,
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
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )

                            val (statusText, statusColor, statusBg) = if (topic.isWeak) {
                                Triple("Needs Focus", WeakRed, WeakRedContainer)
                            } else if (topic.score >= 80) {
                                Triple("Mastered", SuccessGreen, SuccessGreenContainer)
                            } else if (topic.score > 0) {
                                Triple("Good", DocBlue, MaterialTheme.colorScheme.primaryContainer)
                            } else {
                                Triple("Pending Quiz", MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
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
                                if (topic.score > 0) {
                                    Spacer(modifier = Modifier.padding(start = 8.dp))
                                    Text(
                                        text = "${topic.score}%",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }
                            }
                        }

                        if (topic.score > 0) {
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
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
