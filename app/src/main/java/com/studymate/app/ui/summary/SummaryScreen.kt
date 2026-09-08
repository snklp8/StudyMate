package com.studymate.app.ui.summary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.studymate.app.ui.components.StudyMateCard
import com.studymate.app.ui.theme.BorderSubtle
import com.studymate.app.ui.theme.DocBlue
import com.studymate.app.ui.theme.DocBlueContainer
import com.studymate.app.ui.theme.StudyMateNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    navController: NavController,
    materialId: String,
    viewModel: SummaryViewModel = viewModel()
) {
    val summary by viewModel.summary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(materialId) {
        viewModel.loadSummary(materialId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(summary?.title ?: "Document Summary", fontWeight = FontWeight.Bold)
                        Text(
                            "AI-synthesized key takeaways & formulas",
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
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            summary?.let { data ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        ExpandableSummarySection(
                            title = "Executive Summary",
                            icon = Icons.Default.AutoAwesome,
                            iconTint = MaterialTheme.colorScheme.primary,
                            expandedByDefault = true
                        ) {
                            Text(
                                text = data.overallSummary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                            )
                        }
                    }

                    item {
                        ExpandableSummarySection(
                            title = "Core Concepts",
                            icon = Icons.Default.Psychology,
                            iconTint = DocBlue,
                            expandedByDefault = true
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                data.concepts.forEach { concept ->
                                    Row(verticalAlignment = Alignment.Top) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 7.dp)
                                                .size(6.dp)
                                                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(3.dp))
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = concept,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        ExpandableSummarySection(
                            title = "Key Subject Terms",
                            icon = Icons.Default.Bookmark,
                            iconTint = Color(0xFF7C3AED)
                        ) {
                            @OptIn(ExperimentalLayoutApi::class)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                data.keywords.forEach { keyword ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = "#$keyword",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        ExpandableSummarySection(
                            title = "Formulas & Equations",
                            icon = Icons.Default.Calculate,
                            iconTint = Color(0xFFEA580C)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                data.formulas.forEach { formula ->
                                    Surface(
                                        color = StudyMateNavy,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = formula,
                                            modifier = Modifier.padding(14.dp),
                                            fontFamily = FontFamily.Monospace,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF38BDF8),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        ExpandableSummarySection(
                            title = "Exam Focus Points",
                            icon = Icons.Default.CheckCircle,
                            iconTint = Color(0xFF16A34A)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                data.points.forEach { point ->
                                    Row(verticalAlignment = Alignment.Top) {
                                        Text(
                                            text = "•",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color(0xFF16A34A),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = point,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
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
    }
}

@Composable
fun ExpandableSummarySection(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    expandedByDefault: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(expandedByDefault) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "expand")

    StudyMateCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(iconTint.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    HorizontalDivider(thickness = 1.dp, color = BorderSubtle)
                    Spacer(modifier = Modifier.height(14.dp))
                    content()
                }
            }
        }
    }
}

