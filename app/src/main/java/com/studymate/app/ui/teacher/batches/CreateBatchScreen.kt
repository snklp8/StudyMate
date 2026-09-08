package com.studymate.app.ui.teacher.batches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.studymate.app.ui.components.StudyMateButton
import com.studymate.app.ui.components.StudyMateCard
import com.studymate.app.ui.components.StudyMateOutlinedButton
import com.studymate.app.ui.components.StudyMateTextField
import com.studymate.app.ui.theme.BorderSubtle
import com.studymate.app.ui.theme.SuccessGreen
import com.studymate.app.ui.theme.SuccessGreenContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBatchScreen(
    navController: NavController,
    viewModel: BatchViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var name by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("5") }
    var description by remember { mutableStateOf("") }
    var generatedCode by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (generatedCode == null) "Create New Batch" else "Batch Established", fontWeight = FontWeight.Bold)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (generatedCode == null) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "COHORT INFORMATION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                StudyMateTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Batch Name",
                    placeholder = "e.g., CSE-A 2026"
                )

                StudyMateTextField(
                    value = course,
                    onValueChange = { course = it },
                    label = "Course / Department",
                    placeholder = "e.g., Data Structures & Algorithms"
                )

                // Semester Selector
                Column {
                    Text(
                        text = "Semester",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..8).forEach { sem ->
                            val isSelected = semester == sem.toString()
                            FilterChip(
                                selected = isSelected,
                                onClick = { semester = sem.toString() },
                                label = { Text("S$sem", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
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
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                StudyMateTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description",
                    placeholder = "e.g., Core batch for CS undergraduate student curriculum",
                    singleLine = false,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(14.dp))

                StudyMateButton(
                    text = "Create Batch",
                    onClick = {
                        val code = viewModel.createBatch(name, course, semester.toIntOrNull() ?: 1, "2026-2027")
                        generatedCode = code
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank() && course.isNotBlank()
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))

                StudyMateCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentPadding = 24.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessGreenContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Batch Created Successfully",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$course · Semester $semester",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "INVITATION / ENROLLMENT CODE",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = generatedCode!!,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Students can use this code to join this batch directly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                StudyMateButton(
                    text = "Done",
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
