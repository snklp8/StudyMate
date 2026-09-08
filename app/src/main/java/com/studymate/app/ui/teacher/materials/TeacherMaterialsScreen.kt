package com.studymate.app.ui.teacher.materials

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
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
import com.studymate.app.navigation.Screen
import com.studymate.app.ui.components.EmptyState
import com.studymate.app.ui.components.StudyMateCard
import com.studymate.app.ui.theme.BorderSubtle
import com.studymate.app.ui.theme.DocBlue
import com.studymate.app.ui.theme.DocBlueContainer
import com.studymate.app.ui.theme.PdfRed
import com.studymate.app.ui.theme.PdfRedContainer
import com.studymate.app.ui.theme.PptOrange
import com.studymate.app.ui.theme.PptOrangeContainer
import com.studymate.app.ui.theme.SuccessGreen
import com.studymate.app.ui.theme.SuccessGreenContainer
import kotlinx.coroutines.launch

data class TeacherMaterialItem(
    val id: String,
    val title: String,
    val type: String, // "PDF", "PPT", "DOCX", "Video"
    val subject: String,
    val batchAssigned: String,
    val date: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherMaterialsScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "PDF", "PPT", "Video", "DOCX")
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var materialsList by remember {
        mutableStateOf(
            listOf(
                TeacherMaterialItem("m1", "Binary Trees & BST Basics", "PDF", "CS 201 · Data Structures", "CSE-A", "Yesterday"),
                TeacherMaterialItem("m2", "Divide & Conquer Strategies", "PPT", "CS 302 · Algorithms", "CSE-B", "Oct 12"),
                TeacherMaterialItem("m3", "Dynamic Programming Lectures", "Video", "CS 302 · Algorithms", "CSE-A", "Oct 10"),
                TeacherMaterialItem("m4", "Operating Systems Notes", "DOCX", "EE 110 · Computer OS", "CSE-C", "Oct 08"),
                TeacherMaterialItem("m5", "TCP/IP Protocol Stack", "PDF", "EE 342 · Networks", "CSE-A", "Oct 02"),
                TeacherMaterialItem("m6", "Dijkstra's Algorithm Walkthrough", "Video", "CS 302 · Algorithms", "CSE-B", "Sep 28")
            )
        )
    }

    val filteredList = materialsList.filter { item ->
        (selectedFilter == "All" || item.type.equals(selectedFilter, ignoreCase = true)) &&
        (searchQuery.isBlank() || item.title.contains(searchQuery, ignoreCase = true) || item.subject.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Course Materials", fontWeight = FontWeight.Bold)
                        Text(
                            "Manage and assign lecture resources",
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.UploadMaterial.route) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Upload Material", fontWeight = FontWeight.SemiBold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
            )
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

            // Search bar matching Figma Materials screen
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search course materials, slides, or batches…", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
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

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
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

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        title = "No Materials Found",
                        message = "No course resources match the selected criteria.",
                        icon = Icons.AutoMirrored.Filled.MenuBook
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        var menuExpanded by remember { mutableStateOf(false) }
                        val (icon, tint, bg) = when (item.type) {
                            "PDF" -> Triple(Icons.Default.PictureAsPdf, PdfRed, PdfRedContainer)
                            "PPT" -> Triple(Icons.Default.Slideshow, PptOrange, PptOrangeContainer)
                            "Video" -> Triple(Icons.Default.PlayCircle, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                            else -> Triple(Icons.Default.Description, DocBlue, DocBlueContainer)
                        }

                        StudyMateCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate(Screen.Summary.createRoute(item.id)) },
                            contentPadding = 12.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(bg, shape = RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = tint,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.subject,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = " · ${item.date}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Groups,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Batch ${item.batchAssigned}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Box {
                                    IconButton(onClick = { menuExpanded = true }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Options",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("View Material") },
                                            onClick = {
                                                menuExpanded = false
                                                navController.navigate(Screen.Summary.createRoute(item.id))
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Assign to Different Batch") },
                                            onClick = {
                                                menuExpanded = false
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Assigned to CSE-B")
                                                }
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Remove Material", color = MaterialTheme.colorScheme.error) },
                                            onClick = {
                                                menuExpanded = false
                                                materialsList = materialsList.filter { it.id != item.id }
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Material removed")
                                                }
                                            }
                                        )
                                    }
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
