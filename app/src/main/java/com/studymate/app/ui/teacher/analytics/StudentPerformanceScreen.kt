package com.studymate.app.ui.teacher.analytics

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.studymate.app.ui.components.StatCard
import com.studymate.app.ui.components.StudyMateCard
import com.studymate.app.ui.theme.BorderSubtle
import com.studymate.app.ui.theme.DocBlue
import com.studymate.app.ui.theme.DocBlueContainer
import com.studymate.app.ui.theme.PdfRed
import com.studymate.app.ui.theme.PptOrange
import com.studymate.app.ui.theme.PptOrangeContainer
import com.studymate.app.ui.theme.StudyMateIndigo
import com.studymate.app.ui.theme.SuccessGreen
import com.studymate.app.ui.theme.SuccessGreenContainer
import com.studymate.app.ui.theme.WarningOrange
import com.studymate.app.ui.theme.WarningOrangeContainer
import com.studymate.app.ui.theme.WeakRed
import com.studymate.app.ui.theme.WeakRedContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentPerformanceScreen(
    navController: NavController,
    studentId: String
) {
    val weeklyHours = listOf(14f, 22f, 30f, 18f, 36f, 28f)
    val maxWeekly = 40f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Student Analytics", fontWeight = FontWeight.Bold)
                        Text(
                            "Individual Academic Evaluation",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Student Identity Card
                StudyMateCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 18.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(StudyMateIndigo),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AM",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Alex Mercer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "alex.mercer@university.edu • Roll: CS-2024-001",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Cohort: CSE-A · B.Tech Semester 5",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessGreenContainer
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "82%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                                Text(
                                    text = "Overall",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SuccessGreen
                                )
                            }
                        }
                    }
                }
            }

            // Stat Metrics: Quiz scores, Test scores, Study hours
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Quiz Score",
                        value = "84%",
                        icon = Icons.Default.Quiz,
                        tint = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Test Score",
                        value = "78%",
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        tint = SuccessGreen,
                        containerColor = SuccessGreenContainer
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Study Time",
                        value = "42h",
                        icon = Icons.Outlined.Timer,
                        tint = PptOrange,
                        containerColor = PptOrangeContainer
                    )
                }
            }

            // Progress Over Time Bar Chart (Matching Figma Progress screen)
            item {
                Column {
                    Text(
                        text = "Weekly Study Activity (hrs)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    StudyMateCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 18.dp
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                weeklyHours.forEachIndexed { index, hours ->
                                    val barHeightPct = hours / maxWeekly
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(18.dp)
                                                .height((110 * barHeightPct).dp)
                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "W${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Topic Performance (Matching Figma Progress screen)
            item {
                Column {
                    Text(
                        text = "Topic Performance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    StudyMateCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 16.dp
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf(
                                Triple("Data Structures", 85, MaterialTheme.colorScheme.primary),
                                Triple("Algorithms", 72, WarningOrange),
                                Triple("Operating Systems", 64, PdfRed),
                                Triple("Computer Networks", 91, DocBlue)
                            ).forEach { (topic, pct, color) ->
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = topic,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "$pct%",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = color
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { pct / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = color,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        strokeCap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Strengths & Weaknesses (Exact Figma Progress design)
            item {
                Column {
                    Text(
                        text = "Strengths & Weaknesses",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Strengths Card
                        StudyMateCard(
                            modifier = Modifier.weight(1f),
                            contentPadding = 14.dp
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
                                    listOf("Trees", "Sorting", "IP Stack").forEach { tag ->
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
                            contentPadding = 14.dp
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
                                    listOf("Graphs", "Recursion", "Memory Mgmt").forEach { tag ->
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

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
