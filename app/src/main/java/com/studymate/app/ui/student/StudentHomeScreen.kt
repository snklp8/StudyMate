package com.studymate.app.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.studymate.app.navigation.Screen
import com.studymate.app.ui.components.MaterialCardItem
import com.studymate.app.ui.components.SectionHeader
import com.studymate.app.ui.components.StatCard
import com.studymate.app.ui.components.StudyMateCard
import com.studymate.app.ui.components.TopicChip
import com.studymate.app.ui.theme.DocBlue
import com.studymate.app.ui.theme.DocBlueContainer
import com.studymate.app.ui.theme.PdfRed
import com.studymate.app.ui.theme.PdfRedContainer
import com.studymate.app.ui.theme.PptOrange
import com.studymate.app.ui.theme.PptOrangeContainer
import com.studymate.app.ui.theme.StudyMateNavy
import com.studymate.app.ui.theme.SuccessGreen
import com.studymate.app.ui.theme.SuccessGreenContainer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentHomeScreen(
    navController: NavController,
    viewModel: StudentHomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    StudentScaffold(navController = navController) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // 1. Header Bar: Profile info & Avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Good morning, ${uiState.userName}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Let's smash today's milestones",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.IconButton(
                        onClick = { navController.navigate(Screen.Notifications.route) }
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    // Avatar Monogram
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { navController.navigate(Screen.StudentProfile.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.userName.take(1).ifBlank { "A" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Weekly Momentum Banner
            StudyMateCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = StudyMateNavy,
                borderWidth = 0.dp,
                contentPadding = 18.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "THIS WEEK'S TARGET",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF93C5FD),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "68% Complete",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "You're making steady progress across core subjects.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE2E8F0),
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { 0.68f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = Color(0xFF38BDF8),
                    trackColor = Color.White.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "12 modules completed • 4 remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                    Button(
                        onClick = { navController.navigate(Screen.Progress.route) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = StudyMateNavy
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "View Analytics",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Continue Learning Active Card
            SectionHeader(
                title = "Continue Learning",
                subtitle = "Pick up where you left off"
            )
            Spacer(modifier = Modifier.height(8.dp))
            StudyMateCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(Screen.MaterialDetail.createRoute("2")) },
                contentPadding = 16.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(PptOrangeContainer, shape = RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = PptOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Operating Systems PPT",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Computer Science • Slide 24 of 48",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Resume",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { 0.72f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 4. Quick Actions (2x2 Modular Grid)
            SectionHeader(title = "Quick Actions")
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionItem(
                    title = "My Materials",
                    subtitle = "${uiState.materialsCount} files available",
                    icon = Icons.Default.Folder,
                    tint = DocBlue,
                    bg = DocBlueContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.StudentMaterials.route) }
                )
                QuickActionItem(
                    title = "Take Quiz",
                    subtitle = "Practice & assessment",
                    icon = Icons.Default.Quiz,
                    tint = SuccessGreen,
                    bg = SuccessGreenContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.QuizSetup.route) }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionItem(
                    title = "Key Summaries",
                    subtitle = "AI formula & review",
                    icon = Icons.Default.AutoAwesome,
                    tint = Color(0xFF7C3AED),
                    bg = Color(0xFFF5F3FF),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.Summary.createRoute("1")) }
                )
                QuickActionItem(
                    title = "Exam Predictor",
                    subtitle = "High-priority topics",
                    icon = Icons.Default.Insights,
                    tint = PptOrange,
                    bg = PptOrangeContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.Predictor.route) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Recent Study Materials
            SectionHeader(
                title = "Recent Materials",
                actionText = "View All",
                onActionClick = { navController.navigate(Screen.StudentMaterials.route) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.recentMaterials.forEach { material ->
                    MaterialCardItem(
                        title = material.title,
                        subject = material.subject,
                        type = material.type,
                        metadata = "2 hrs ago",
                        status = "Ready",
                        onClick = { navController.navigate(Screen.MaterialDetail.createRoute(material.id)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 6. Academic Performance Overview
            SectionHeader(
                title = "Academic Performance",
                actionText = "Details",
                onActionClick = { navController.navigate(Screen.Progress.route) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Quiz Avg",
                    value = uiState.quizAverage,
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    iconTint = MaterialTheme.colorScheme.primary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Materials",
                    value = uiState.materialsCount.toString(),
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    iconTint = DocBlue
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Quizzes",
                    value = uiState.quizzesCount.toString(),
                    icon = Icons.Default.Quiz,
                    iconTint = SuccessGreen
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 7. Topics Requiring Focus
            SectionHeader(
                title = "Focus Topics",
                subtitle = "Based on recent diagnostic quizzes"
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.weakTopics.forEach { topic ->
                    TopicChip(
                        topic = topic,
                        isWeak = true,
                        onClick = { navController.navigate(Screen.QuizSetup.route) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 8. Exam Readiness Predictor Summary
            SectionHeader(
                title = "Exam Readiness",
                actionText = "Predictor",
                onActionClick = { navController.navigate(Screen.Predictor.route) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            StudyMateCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(Screen.Predictor.route) },
                contentPadding = 16.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(SuccessGreenContainer, shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${uiState.predictedPerformance.toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Predicted Exam Score",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SuccessGreenContainer
                            ) {
                                Text(
                                    text = "On Track",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Historical test trend suggests strong performance.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun QuickActionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    bg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    StudyMateCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = 14.dp
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(bg, shape = RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

