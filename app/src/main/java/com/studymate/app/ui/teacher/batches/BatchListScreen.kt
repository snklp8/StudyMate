package com.studymate.app.ui.teacher.batches

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.studymate.app.navigation.Screen
import com.studymate.app.ui.components.EmptyState
import com.studymate.app.ui.components.StudyMateCard
import com.studymate.app.ui.teacher.TeacherScaffold
import com.studymate.app.ui.theme.BorderSubtle
import com.studymate.app.ui.theme.SuccessGreen
import com.studymate.app.ui.theme.SuccessGreenContainer
import com.studymate.app.ui.theme.WarningOrange
import com.studymate.app.ui.theme.WarningOrangeContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchListScreen(
    navController: NavController,
    viewModel: BatchViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.batchesState.collectAsState()

    TeacherScaffold(
        navController = navController,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Academic Batches", fontWeight = FontWeight.Bold)
                        Text(
                            "Manage cohorts, students, and performance",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is BatchesUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is BatchesUiState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            title = "No Batches Created",
                            message = "Tap 'New Batch' below to establish your first cohort."
                        )
                    }
                }
                is BatchesUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error loading batches.", color = MaterialTheme.colorScheme.error)
                    }
                }
                is BatchesUiState.Success -> {
                    val batches = (uiState as BatchesUiState.Success).batches
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(6.dp)) }

                        items(batches) { batch ->
                            val avg = batch.averageScore
                            val (avgColor, avgBg) = if (avg >= 75) {
                                SuccessGreen to SuccessGreenContainer
                            } else {
                                WarningOrange to WarningOrangeContainer
                            }

                            StudyMateCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate(Screen.BatchDetail.createRoute(batch.id)) },
                                contentPadding = 16.dp
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = batch.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = batch.course,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = avgBg
                                        ) {
                                            Text(
                                                text = "$avg% avg",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = avgColor,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(thickness = 1.dp, color = BorderSubtle)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${batch.studentCount} Students Enrolled",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = "2 Active Tests",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    LinearProgressIndicator(
                                        progress = { avg / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (avg >= 75) SuccessGreen else MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        strokeCap = StrokeCap.Round
                                    )
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(90.dp)) }
                    }
                }
            }

            // FAB
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.CreateBatch.route) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Create Batch", fontWeight = FontWeight.SemiBold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(18.dp)
            )
        }
    }
}
