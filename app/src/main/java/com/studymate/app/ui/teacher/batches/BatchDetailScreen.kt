package com.studymate.app.ui.teacher.batches

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.studymate.app.ui.components.StudyMateButton
import com.studymate.app.ui.components.StudyMateCard
import com.studymate.app.ui.components.StudyMateOutlinedButton
import com.studymate.app.ui.theme.BorderSubtle
import com.studymate.app.ui.theme.DocBlue
import com.studymate.app.ui.theme.DocBlueContainer
import com.studymate.app.ui.theme.PdfRed
import com.studymate.app.ui.theme.PdfRedContainer
import com.studymate.app.ui.theme.PptOrange
import com.studymate.app.ui.theme.PptOrangeContainer
import com.studymate.app.ui.theme.SuccessGreen
import com.studymate.app.ui.theme.SuccessGreenContainer
import com.studymate.app.ui.theme.WarningOrange
import com.studymate.app.ui.theme.WarningOrangeContainer
import com.studymate.app.ui.theme.WeakRed
import com.studymate.app.ui.theme.WeakRedContainer

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BatchDetailScreen(
    batchId: String,
    navController: NavController,
    viewModel: BatchViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val batch by viewModel.selectedBatch.collectAsState()
    val students by viewModel.batchStudents.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Students", "Materials", "Tests", "Performance")

    LaunchedEffect(batchId) {
        viewModel.loadBatch(batchId)
        viewModel.loadBatchStudents(batchId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(batch?.name ?: "Batch Details", fontWeight = FontWeight.Bold)
                        Text(
                            batch?.course ?: "Cohort Management",
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
    ) { paddingValues ->
        if (batch == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        val b = batch!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Batch Overview Header Card
            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                Spacer(modifier = Modifier.height(6.dp))
                StudyMateCard(
                    modifier = Modifier.fillMaxWidth(),
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
                                    text = b.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${b.course} • Semester ${b.semester}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Code: ${b.batchCode}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(thickness = 1.dp, color = BorderSubtle)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${b.studentCount} Active Students",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Avg Performance: ${b.averageScore}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (b.averageScore >= 75) SuccessGreen else WarningOrange
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { b.averageScore / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (b.averageScore >= 75) SuccessGreen else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 4 Tabs: Students, Materials, Tests, Performance
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = { HorizontalDivider(thickness = 1.dp, color = BorderSubtle) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> { // Students Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ENROLLED STUDENTS (${students.size})",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                StudyMateButton(
                                    text = "Add Students",
                                    onClick = { navController.navigate(Screen.AddStudents.createRoute(b.id)) },
                                    leadingIcon = Icons.Default.PersonAdd
                                )
                            }
                        }

                        items(students) { student ->
                            StudyMateCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate(Screen.StudentPerformance.createRoute(student.studentId)) },
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
                                            text = student.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${student.performance}%",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (student.performance >= 75) SuccessGreen else WarningOrange
                                        )
                                        Text(
                                            text = student.lastActivity,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
                1 -> { // Materials Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ASSIGNED COURSE MATERIALS",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                StudyMateButton(
                                    text = "Upload",
                                    onClick = { navController.navigate(Screen.TeacherMaterials.route) },
                                    leadingIcon = Icons.Default.Add
                                )
                            }
                        }

                        val batchMaterials = listOf(
                            Triple("Binary Trees & BST Basics", "PDF • 2.8 MB • Uploaded Yesterday", Icons.Default.PictureAsPdf to (PdfRed to PdfRedContainer)),
                            Triple("Divide & Conquer Strategies", "PPT • 4.5 MB • Oct 12", Icons.Default.Slideshow to (PptOrange to PptOrangeContainer)),
                            Triple("Tree Traversals Walkthrough", "YouTube Video • 24 mins • Oct 10", Icons.Default.PlayCircle to (DocBlue to DocBlueContainer))
                        )

                        items(batchMaterials) { (title, sub, meta) ->
                            StudyMateCard(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = 12.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(meta.second.second, shape = RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = meta.first,
                                            contentDescription = null,
                                            tint = meta.second.first,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
                2 -> { // Tests Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "BATCH ASSESSMENTS",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                StudyMateButton(
                                    text = "Create Test",
                                    onClick = { navController.navigate(Screen.CreateTest.route) },
                                    leadingIcon = Icons.Default.Add
                                )
                            }
                        }

                        val tests = listOf(
                            Triple("Midterm Mock 1: Data Structures", "42 Attempted • Avg 78%", "Completed"),
                            Triple("Algorithms Quiz 2: Trees", "38 Attempted • Avg 82%", "Completed"),
                            Triple("Binary Search Practice Quiz", "40 Attempted • Avg 74%", "Active")
                        )

                        items(tests) { (title, stats, status) ->
                            StudyMateCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate(Screen.TestResults.createRoute("test1")) },
                                contentPadding = 12.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(stats, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (status == "Active") SuccessGreenContainer else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = status,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (status == "Active") SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
                3 -> { // Performance Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            StudyMateCard(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = 14.dp
                            ) {
                                Column {
                                    Text("Cohort Performance Metrics", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("BATCH AVERAGE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${b.averageScore}%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("COMPLETION RATE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("92%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = SuccessGreen)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text("Topic Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            StudyMateCard(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = 14.dp
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    listOf(
                                        Pair("Binary Trees & BST", 84),
                                        Pair("Array Sorting & Divide/Conquer", 78),
                                        Pair("AVL Trees & Rotations", 62),
                                        Pair("Graph Traversals (BFS/DFS)", 58)
                                    ).forEach { (topic, pct) ->
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(topic, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                                Text("$pct%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (pct >= 75) SuccessGreen else WarningOrange)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            LinearProgressIndicator(
                                                progress = { pct / 100f },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(5.dp)
                                                    .clip(RoundedCornerShape(3.dp)),
                                                color = if (pct >= 75) SuccessGreen else MaterialTheme.colorScheme.primary,
                                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                                strokeCap = StrokeCap.Round
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            StudyMateButton(
                                text = "View Full Analytics & Trends",
                                onClick = { navController.navigate(Screen.BatchPerformance.createRoute(b.id)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}
