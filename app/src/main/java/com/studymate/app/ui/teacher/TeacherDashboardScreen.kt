package com.studymate.app.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.studymate.app.data.model.Batch
import com.studymate.app.navigation.Screen
import com.studymate.app.ui.components.SectionHeader
import com.studymate.app.ui.components.StatCard
import com.studymate.app.ui.components.StudyMateCard
import com.studymate.app.ui.theme.BorderSubtle
import com.studymate.app.ui.theme.DocBlue
import com.studymate.app.ui.theme.DocBlueContainer
import com.studymate.app.ui.theme.PdfRed
import com.studymate.app.ui.theme.PdfRedContainer
import com.studymate.app.ui.theme.PptOrange
import com.studymate.app.ui.theme.PptOrangeContainer
import com.studymate.app.ui.theme.StudyMateIndigo
import com.studymate.app.ui.theme.SuccessGreen
import com.studymate.app.ui.theme.SuccessGreenContainer
import com.studymate.app.ui.theme.WarningOrange
import com.studymate.app.ui.theme.WarningOrangeContainer

data class RecentActivityItem(
    val title: String,
    val description: String,
    val time: String,
    val icon: ImageVector,
    val tint: Color,
    val bg: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    navController: NavController,
    viewModel: TeacherDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val recentActivities = listOf(
        RecentActivityItem("New student enrolled", "Rohan Sharma joined CSE-A", "10m ago", Icons.Default.PersonAdd, SuccessGreen, SuccessGreenContainer),
        RecentActivityItem("Material uploaded", "Binary Search Trees Handout assigned to CSE-A", "1h ago", Icons.AutoMirrored.Filled.MenuBook, DocBlue, DocBlueContainer),
        RecentActivityItem("Test completed", "Algorithms Quiz 2 completed by 38/42 students", "3h ago", Icons.AutoMirrored.Filled.Assignment, PptOrange, PptOrangeContainer),
        RecentActivityItem("Recent result posted", "Average score for DBMS Unit Test reached 79%", "Yesterday", Icons.AutoMirrored.Filled.TrendingUp, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
    )

    TeacherScaffold(
        navController = navController,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("StudyMate", fontWeight = FontWeight.Bold)
                        Text(
                            "Faculty & Assessment Console",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    IconButton(onClick = { navController.navigate(Screen.TeacherProfile.route) }) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "K",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Greeting & Faculty Info
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Good morning, Dr. Kumar 👋",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Dept of Computer Science & Engineering",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 2. Overview Statistics (4 metrics: Batches, Students, Active Tests, Materials)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                modifier = Modifier.weight(1f),
                                label = "Total Batches",
                                value = uiState.batchCount.toString(),
                                icon = Icons.Default.Groups,
                                tint = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                            StatCard(
                                modifier = Modifier.weight(1f),
                                label = "Total Students",
                                value = uiState.totalStudents.toString(),
                                icon = Icons.Default.People,
                                tint = DocBlue,
                                containerColor = DocBlueContainer
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                modifier = Modifier.weight(1f),
                                label = "Active Tests",
                                value = uiState.testCount.toString(),
                                icon = Icons.AutoMirrored.Filled.Assignment,
                                tint = PptOrange,
                                containerColor = PptOrangeContainer
                            )
                            StatCard(
                                modifier = Modifier.weight(1f),
                                label = "Materials Uploaded",
                                value = uiState.materialCount.toString(),
                                icon = Icons.AutoMirrored.Filled.MenuBook,
                                tint = SuccessGreen,
                                containerColor = SuccessGreenContainer
                            )
                        }
                    }
                }

                // 3. Quick Actions Grid: Create Batch, Upload Material, Create Test, View Results
                item {
                    SectionHeader(title = "Quick Actions")
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TeacherActionCard(
                            title = "Create Batch",
                            icon = Icons.Default.Add,
                            tint = MaterialTheme.colorScheme.primary,
                            bg = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(Screen.CreateBatch.route) }
                        )
                        TeacherActionCard(
                            title = "Upload Material",
                            icon = Icons.Default.CloudUpload,
                            tint = DocBlue,
                            bg = DocBlueContainer,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(Screen.TeacherMaterials.route) }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TeacherActionCard(
                            title = "Create Test",
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            tint = PptOrange,
                            bg = PptOrangeContainer,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(Screen.CreateTest.route) }
                        )
                        TeacherActionCard(
                            title = "View Results",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            tint = SuccessGreen,
                            bg = SuccessGreenContainer,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(Screen.TestList.route) }
                        )
                    }
                }

                // 4. Batch Performance (CSE-A 78% average, CSE-B 71% average)
                item {
                    SectionHeader(
                        title = "Batch Performance Overview",
                        actionText = "Full Analytics",
                        onActionClick = { navController.navigate(Screen.BatchPerformance.createRoute("all")) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            Triple("CSE-A", "Data Structures & Algorithms • 42 students", 78),
                            Triple("CSE-B", "Operating Systems • 38 students", 71),
                            Triple("CSE-C", "Database Management • 45 students", 83)
                        ).forEach { (batchName, subtitle, avg) ->
                            StudyMateCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate(Screen.BatchDetail.createRoute("batch1")) },
                                contentPadding = 14.dp
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = batchName,
                                                style = MaterialTheme.typography.titleMedium,
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

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (avg >= 75) SuccessGreenContainer else WarningOrangeContainer
                                        ) {
                                            Text(
                                                text = "$avg% average",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (avg >= 75) SuccessGreen else WarningOrange,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    LinearProgressIndicator(
                                        progress = { avg / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (avg >= 75) SuccessGreen else MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        strokeCap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Recent Activity Feed
                item {
                    SectionHeader(title = "Recent Activity")
                    Spacer(modifier = Modifier.height(6.dp))

                    StudyMateCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 12.dp
                    ) {
                        Column {
                            recentActivities.forEachIndexed { index, act ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(act.bg, shape = RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = act.icon,
                                            contentDescription = null,
                                            tint = act.tint,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = act.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = act.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = act.time,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }

                                if (index < recentActivities.size - 1) {
                                    HorizontalDivider(thickness = 1.dp, color = BorderSubtle, modifier = Modifier.padding(vertical = 4.dp))
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
}

@Composable
private fun TeacherActionCard(
    title: String,
    icon: ImageVector,
    tint: Color,
    bg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    StudyMateCard(
        modifier = modifier.clickable { onClick() },
        contentPadding = 14.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(bg, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
