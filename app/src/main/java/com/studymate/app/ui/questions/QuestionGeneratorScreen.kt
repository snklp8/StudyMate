package com.studymate.app.ui.questions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.FactCheck
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.studymate.app.navigation.Screen
import com.studymate.app.ui.components.StudyMateButton
import com.studymate.app.ui.components.StudyMateCard
import com.studymate.app.ui.theme.BorderSubtle
import com.studymate.app.ui.theme.SuccessGreen
import com.studymate.app.ui.theme.SuccessGreenContainer
import com.studymate.app.ui.theme.WarningOrange
import com.studymate.app.ui.theme.WarningOrangeContainer
import com.studymate.app.ui.theme.WeakRed
import com.studymate.app.ui.theme.WeakRedContainer

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuestionGeneratorScreen(
    navController: NavController,
    materialId: String,
    viewModel: QuestionsViewModel = viewModel()
) {
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()
    val questionCount by viewModel.questionCount.collectAsState()
    val selectedTopic by viewModel.selectedTopic.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    val supportedTypes = listOf(
        Triple("MCQ", "Multiple Choice", Icons.Outlined.FactCheck),
        Triple("Viva", "Oral Defense / Viva", Icons.Default.RecordVoiceOver),
        Triple("Long Answer", "Explanatory & Proofs", Icons.Outlined.Assignment)
    )

    val difficulties = listOf(
        Triple("Easy", "Foundational recall", SuccessGreen to SuccessGreenContainer),
        Triple("Medium", "Exam-standard problems", WarningOrange to WarningOrangeContainer),
        Triple("Hard", "Complex theory & bounds", WeakRed to WeakRedContainer)
    )

    val countOptions = listOf(5, 10, 15, 20)

    val topics = listOf(
        "Binary Search Trees & Operations",
        "Tree Traversals (Inorder, Preorder, Postorder)",
        "AVL Trees & Rebalancing Rotations",
        "Red-Black Tree Properties",
        "Priority Queues & Binary Heaps"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Question Generator", fontWeight = FontWeight.Bold)
                        Text(
                            "Generate customized revision prompts",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Topic Selection
            Text(
                text = "TARGET TOPIC",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            StudyMateCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 14.dp
            ) {
                Column {
                    Text(
                        text = "Selected Subject: CS 201 · Data Structures",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = selectedTopic,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(thickness = 1.dp, color = BorderSubtle)
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        topics.forEach { topic ->
                            val isSelected = selectedTopic == topic
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else BorderSubtle),
                                modifier = Modifier.clickable { viewModel.selectedTopic.value = topic }
                            ) {
                                Text(
                                    text = topic,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Question Type (ONLY MCQ, Viva, Long Answer)
            Text(
                text = "QUESTION TYPE",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                supportedTypes.forEach { (typeKey, title, icon) ->
                    val isSelected = selectedType == typeKey
                    StudyMateCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectedType.value = typeKey },
                        borderColor = if (isSelected) MaterialTheme.colorScheme.primary else BorderSubtle,
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                        contentPadding = 14.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = typeKey,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Difficulty
            Text(
                text = "DIFFICULTY LEVEL",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                difficulties.forEach { (diff, _, colors) ->
                    val isSelected = selectedDifficulty == diff
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable { viewModel.selectedDifficulty.value = diff },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) colors.second else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isSelected) colors.first else BorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = diff,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                                color = if (isSelected) colors.first else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Number of Questions
            Text(
                text = "NUMBER OF QUESTIONS",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                countOptions.forEach { count ->
                    val isSelected = questionCount == count
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.questionCount.value = count },
                        label = {
                            Text(
                                text = "$count Qs",
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
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 5. Generate Button
            StudyMateButton(
                text = "Generate Questions",
                onClick = {
                    viewModel.generateQuestions {
                        navController.navigate(Screen.GeneratedQuestions.createRoute(materialId))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.AutoAwesome,
                isLoading = isGenerating
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
