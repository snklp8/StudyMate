package com.studymate.app.ui.teacher.tests

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.studymate.app.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestListScreen(
    navController: NavController,
    viewModel: TestViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.testsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tests") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.CreateTest.route) }) {
                Icon(Icons.Default.Add, contentDescription = "Create Test")
            }
        }
    ) { paddingValues ->
        when (uiState) {
            is TestsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is TestsUiState.Empty -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tests found.")
                }
            }
            is TestsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error loading tests.", color = MaterialTheme.colorScheme.error)
                }
            }
            is TestsUiState.Success -> {
                val tests = (uiState as TestsUiState.Success).tests
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(tests) { test ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate(Screen.TestResults.createRoute(test.id)) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = test.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "${test.subject} • ${test.batchName}", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "${test.totalQuestions} Questions • ${test.duration} mins", style = MaterialTheme.typography.bodySmall)
                                    Text(text = test.createdAt.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}
