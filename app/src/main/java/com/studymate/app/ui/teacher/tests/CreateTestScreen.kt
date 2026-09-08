package com.studymate.app.ui.teacher.tests

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTestScreen(
    navController: NavController,
    viewModel: TestViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var selectedBatch by remember { mutableStateOf("CSE-A (Data Structures)") }
    var totalMarks by remember { mutableStateOf("50") }
    var duration by remember { mutableStateOf("45") }
    var scheduleDateTime by remember { mutableStateOf("Tomorrow, 10:00 AM") }

    val testQuestions = remember {
        mutableStateListOf(
            "Explain binary search algorithm and its O(log n) time complexity bound.",
            "Compare AVL tree double rotations with single rotations.",
            "Find the inorder successor of a node in a binary search tree."
        )
    }

    var newQuestionText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isPublishing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Create Assessment", fontWeight = FontWeight.Bold)
                        Text(
                            "Configure and publish test for cohort",
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "TEST CONFIGURATION",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            StudyMateTextField(
                value = title,
                onValueChange = { title = it },
                label = "Test Title",
                placeholder = "e.g., Midterm Assessment 1: Trees & Graphs"
            )

            StudyMateTextField(
                value = subject,
                onValueChange = { subject = it },
                label = "Subject / Topic Area",
                placeholder = "e.g., CS 201 · Data Structures"
            )

            // Batch selection
            Column {
                Text(
                    text = "Assign to Cohort / Batch",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("CSE-A (Data Structures)", "CSE-B (Algorithms)", "CSE-C (OS)").forEach { b ->
                        val isSelected = selectedBatch == b
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedBatch = b },
                            label = { Text(b.substringBefore(" "), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
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

            // Specs Row: Total Marks, Duration (mins), Schedule
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    StudyMateTextField(
                        value = totalMarks,
                        onValueChange = { totalMarks = it },
                        label = "Total Marks",
                        placeholder = "50"
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    StudyMateTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = "Duration (mins)",
                        placeholder = "45"
                    )
                }
            }

            StudyMateTextField(
                value = scheduleDateTime,
                onValueChange = { scheduleDateTime = it },
                label = "Date & Time",
                placeholder = "e.g., Oct 24, 10:00 AM"
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Questions Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUESTIONS (${testQuestions.size})",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                testQuestions.forEachIndexed { index, q ->
                    StudyMateCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 12.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "Q${index + 1}.",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = q,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { testQuestions.removeAt(index) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // Add Question Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        StudyMateTextField(
                            value = newQuestionText,
                            onValueChange = { newQuestionText = it },
                            label = "Add Question",
                            placeholder = "Enter question text…"
                        )
                    }
                    StudyMateButton(
                        text = "Add",
                        onClick = {
                            if (newQuestionText.isNotBlank()) {
                                testQuestions.add(newQuestionText)
                                newQuestionText = ""
                            }
                        },
                        modifier = Modifier.padding(top = 22.dp),
                        leadingIcon = Icons.Default.Add
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions: Save Draft & Publish Test
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StudyMateOutlinedButton(
                    text = "Save Draft",
                    onClick = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Test draft saved successfully")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Default.Save
                )

                StudyMateButton(
                    text = "Publish Test",
                    onClick = {
                        coroutineScope.launch {
                            isPublishing = true
                            delay(900)
                            isPublishing = false
                            snackbarHostState.showSnackbar("Test published to $selectedBatch!")
                            delay(400)
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Default.Publish,
                    isLoading = isPublishing,
                    enabled = title.isNotBlank() && testQuestions.isNotEmpty()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
