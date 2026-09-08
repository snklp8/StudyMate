package com.studymate.app.ui.teacher.batches

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.studymate.app.ui.components.StudyMateButton
import com.studymate.app.ui.components.StudyMateCard
import com.studymate.app.ui.theme.BorderSubtle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class CandidateStudent(
    val id: String,
    val name: String,
    val email: String,
    val rollNo: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentsScreen(
    navController: NavController,
    batchId: String
) {
    var searchQuery by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateListOf<String>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isAdding by remember { mutableStateOf(false) }

    val candidateStudents = remember {
        listOf(
            CandidateStudent("s1", "Aarav Sharma", "aarav.s@university.edu", "CS-2024-001"),
            CandidateStudent("s2", "Diya Patel", "diya.p@university.edu", "CS-2024-014"),
            CandidateStudent("s3", "Ishaan Verma", "ishaan.v@university.edu", "CS-2024-022"),
            CandidateStudent("s4", "Ananya Iyer", "ananya.i@university.edu", "CS-2024-035"),
            CandidateStudent("s5", "Kabir Mehta", "kabir.m@university.edu", "CS-2024-041"),
            CandidateStudent("s6", "Riya Sen", "riya.s@university.edu", "CS-2024-052"),
            CandidateStudent("s7", "Varun Nair", "varun.n@university.edu", "CS-2024-063"),
            CandidateStudent("s8", "Meera Joshi", "meera.j@university.edu", "CS-2024-074")
        )
    }

    val filteredList = if (searchQuery.isBlank()) {
        candidateStudents
    } else {
        candidateStudents.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.email.contains(searchQuery, ignoreCase = true) ||
            it.rollNo.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Add Students", fontWeight = FontWeight.Bold)
                        Text(
                            "Select students to enroll in batch",
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
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.navigationBarsPadding()) {
                    HorizontalDivider(thickness = 1.dp, color = BorderSubtle)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${selectedIds.size} Selected",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Ready to enroll",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        StudyMateButton(
                            text = "Add Students",
                            onClick = {
                                coroutineScope.launch {
                                    isAdding = true
                                    delay(600)
                                    isAdding = false
                                    snackbarHostState.showSnackbar("Added ${selectedIds.size} students to batch!")
                                    delay(400)
                                    navController.popBackStack()
                                }
                            },
                            enabled = selectedIds.isNotEmpty(),
                            isLoading = isAdding,
                            leadingIcon = Icons.Default.PersonAdd
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by student name or roll no…", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = BorderSubtle
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.id }) { student ->
                    val isChecked = selectedIds.contains(student.id)

                    StudyMateCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) selectedIds.remove(student.id) else selectedIds.add(student.id)
                            },
                        borderColor = if (isChecked) MaterialTheme.colorScheme.primary else BorderSubtle,
                        containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                        contentPadding = 12.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = student.name.take(1),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${student.rollNo} • ${student.email}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) selectedIds.add(student.id) else selectedIds.remove(student.id)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = BorderSubtle
                                )
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}
