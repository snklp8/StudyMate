package com.studymate.app.ui.prediction

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavController
import com.studymate.app.navigation.Screen
import com.studymate.app.ui.components.StudyMateCard
import com.studymate.app.ui.theme.BorderSubtle
import com.studymate.app.ui.theme.SuccessGreen
import com.studymate.app.ui.theme.SuccessGreenContainer
import com.studymate.app.ui.theme.WarningOrange
import com.studymate.app.ui.theme.WarningOrangeContainer
import com.studymate.app.ui.theme.WeakRed
import com.studymate.app.ui.theme.WeakRedContainer

data class AcademicRecommendation(
    val id: String,
    val subject: String,
    val topic: String,
    val priority: String, // "High", "Medium", "Low"
    val importanceLevel: Int, // 1 to 5
    val reason: String,
    val actionText: String = "Revise Topic"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictorScreen(
    navController: NavController
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "High", "Medium", "Low")

    val recommendations = listOf(
        AcademicRecommendation(
            id = "rec1",
            subject = "Database Management Systems",
            topic = "Normalization (1NF, 2NF, 3NF, BCNF)",
            priority = "High",
            importanceLevel = 5,
            reason = "Frequently assessed in upcoming midterms and currently below your average performance.",
            actionText = "Review Normalization Notes"
        ),
        AcademicRecommendation(
            id = "rec2",
            subject = "Data Structures",
            topic = "AVL Trees & Balance Rotations",
            priority = "High",
            importanceLevel = 5,
            reason = "High exam weightage; error rate identified in last week's practice quiz.",
            actionText = "Practice Rotations Quiz"
        ),
        AcademicRecommendation(
            id = "rec3",
            subject = "Operating Systems",
            topic = "Process Scheduling Algorithms (Round Robin & SJF)",
            priority = "Medium",
            importanceLevel = 4,
            reason = "Standard numerical problem area; recommended for revision before upcoming mock exam.",
            actionText = "View Scheduling Summary"
        ),
        AcademicRecommendation(
            id = "rec4",
            subject = "Computer Networks",
            topic = "TCP Sliding Window & Flow Control",
            priority = "Medium",
            importanceLevel = 4,
            reason = "Important protocol concept; you have covered theory but not completed practical exercises.",
            actionText = "Take Quick Quiz"
        ),
        AcademicRecommendation(
            id = "rec5",
            subject = "Algorithms",
            topic = "Dijkstra's Shortest Path Algorithm",
            priority = "Low",
            importanceLevel = 3,
            reason = "Good competency demonstrated; quick refresher suggested to retain optimal recall.",
            actionText = "Review Flashcards"
        )
    )

    val filteredList = if (selectedFilter == "All") recommendations else recommendations.filter { it.priority == selectedFilter }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Important Topics & Recommendations", fontWeight = FontWeight.Bold)
                        Text(
                            "Curated focus areas based on your study progress",
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
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                // Guidance Banner
                StudyMateCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    contentPadding = 16.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Personalized Priority Roadmap",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Topics tailored to strengthen upcoming assessment scores and close knowledge gaps.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Priority Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { filter ->
                        val isSelected = selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = if (filter == "All") "All Priorities" else "$filter Priority",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) MaterialTheme.colorScheme.primary else BorderSubtle
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            items(filteredList, key = { it.id }) { rec ->
                val (priorityColor, priorityBg) = when (rec.priority) {
                    "High" -> WeakRed to WeakRedContainer
                    "Medium" -> WarningOrange to WarningOrangeContainer
                    else -> SuccessGreen to SuccessGreenContainer
                }

                StudyMateCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(Screen.StudentMaterials.route) },
                    contentPadding = 16.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = rec.subject,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = priorityBg
                            ) {
                                Text(
                                    text = "${rec.priority} Priority",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = priorityColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = rec.topic,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Importance stars
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Importance:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            for (i in 1..5) {
                                Icon(
                                    imageVector = if (i <= rec.importanceLevel) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = if (i <= rec.importanceLevel) WarningOrange else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(thickness = 1.dp, color = BorderSubtle)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Reason:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = rec.reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = rec.actionText,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
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
