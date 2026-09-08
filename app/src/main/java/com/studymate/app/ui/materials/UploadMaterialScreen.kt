package com.studymate.app.ui.materials

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.studymate.app.ui.components.StudyMateButton
import com.studymate.app.ui.components.StudyMateCard
import com.studymate.app.ui.components.StudyMateTextField
import com.studymate.app.ui.theme.BorderSubtle
import com.studymate.app.ui.theme.DocBlue
import com.studymate.app.ui.theme.DocBlueContainer
import com.studymate.app.ui.theme.PdfRed
import com.studymate.app.ui.theme.PdfRedContainer
import com.studymate.app.ui.theme.PptOrange
import com.studymate.app.ui.theme.PptOrangeContainer
import com.studymate.app.ui.theme.StudyMateIndigo
import com.studymate.app.ui.theme.StudyMateNavy
import com.studymate.app.ui.theme.SuccessGreen
import com.studymate.app.ui.theme.SuccessGreenContainer
import com.studymate.app.ui.theme.YouTubeRed
import com.studymate.app.ui.theme.YouTubeRedContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SelectedFileInfo(
    val name: String,
    val type: String,
    val size: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UploadMaterialScreen(
    navController: NavController
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("PDF") }
    val types = listOf("PDF", "PPT", "PPTX", "DOCX", "YouTube")

    var youtubeUrl by remember { mutableStateOf("") }
    var youtubeAdded by remember { mutableStateOf(false) }

    var selectedFile by remember {
        mutableStateOf<SelectedFileInfo?>(
            SelectedFileInfo("Binary_Trees_And_BST_Basics.pdf", "PDF", "2.8 MB")
        )
    }

    var isProcessing by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Upload Study Material", fontWeight = FontWeight.Bold)
                        Text(
                            "Add course notes, slides or video references",
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Document Details Section
            Text(
                text = "DOCUMENT DETAILS",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            StudyMateTextField(
                value = title,
                onValueChange = { title = it },
                label = "Material Title",
                placeholder = "e.g., Binary Trees & BST Basics"
            )

            Spacer(modifier = Modifier.height(14.dp))

            StudyMateTextField(
                value = subject,
                onValueChange = { subject = it },
                label = "Course / Subject",
                placeholder = "e.g., CS 201 · Data Structures"
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Format Selector
            Text(
                text = "MATERIAL TYPE",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                types.forEach { type ->
                    val isSelected = selectedType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedType = type
                            if (type != "YouTube" && selectedFile == null) {
                                selectedFile = SelectedFileInfo(
                                    name = "Lecture_Notes_${type.lowercase()}.${type.lowercase()}",
                                    type = type,
                                    size = "3.2 MB"
                                )
                            }
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val icon = when {
                                    type.contains("PDF") -> Icons.Default.PictureAsPdf
                                    type.contains("PPT") -> Icons.Default.Slideshow
                                    type.contains("YouTube") -> Icons.Default.PlayCircle
                                    else -> Icons.Default.Description
                                }
                                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(type, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
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

            Spacer(modifier = Modifier.height(22.dp))

            // Source Content Area
            if (selectedType == "YouTube") {
                Text(
                    text = "YOUTUBE STUDY LINK",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        StudyMateTextField(
                            value = youtubeUrl,
                            onValueChange = {
                                youtubeUrl = it
                                youtubeAdded = false
                            },
                            label = "Video URL",
                            placeholder = "https://www.youtube.com/watch?v=...",
                            leadingIcon = {
                                Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        )
                    }

                    StudyMateButton(
                        text = if (youtubeAdded) "Added" else "Add",
                        onClick = {
                            if (youtubeUrl.isNotBlank()) {
                                youtubeAdded = true
                                if (title.isBlank()) title = "Tree Traversals: Inorder, Preorder, Postorder"
                                if (subject.isBlank()) subject = "CS 201 · Data Structures"
                            }
                        },
                        modifier = Modifier.padding(top = 22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Video Preview Card
                AnimatedVisibility(visible = youtubeAdded || youtubeUrl.isNotBlank()) {
                    StudyMateCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentPadding = 14.dp
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(StudyMateNavy),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color.White.copy(alpha = 0.2f), shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.Black.copy(alpha = 0.75f)
                                ) {
                                    Text(
                                        text = "24:18",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = if (title.isNotBlank()) title else "Tree Traversals: Inorder, Preorder, Postorder Walkthrough",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "YouTube Video Reference · CS Academic Hub",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "ATTACHED FILE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (selectedFile != null) {
                    val file = selectedFile!!
                    val (icon, tint, bg) = when (file.type) {
                        "PDF" -> Triple(Icons.Default.PictureAsPdf, PdfRed, PdfRedContainer)
                        "PPT", "PPTX" -> Triple(Icons.Default.Slideshow, PptOrange, PptOrangeContainer)
                        else -> Triple(Icons.Default.Description, DocBlue, DocBlueContainer)
                    }

                    StudyMateCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentPadding = 14.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(bg, shape = RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = tint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = file.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${file.type} Document · ${file.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(onClick = { selectedFile = null }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove file",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    StudyMateCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedFile = SelectedFileInfo(
                                    name = "Advanced_Data_Structures_Module2.$selectedType",
                                    type = selectedType,
                                    size = "4.1 MB"
                                )
                            },
                        containerColor = MaterialTheme.colorScheme.surface,
                        borderColor = BorderSubtle,
                        contentPadding = 24.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Tap to select document",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Supports PDF, PPT, PPTX, DOCX up to 50 MB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Upload action button
            StudyMateButton(
                text = "Upload & Process Material",
                onClick = {
                    coroutineScope.launch {
                        isProcessing = true
                        delay(2200)
                        isProcessing = false
                        snackbarHostState.showSnackbar("Material successfully prepared and added to your library!")
                        delay(400)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Processing State Modal Bottom Sheet
        if (isProcessing) {
            ModalBottomSheet(
                onDismissRequest = { /* locked during processing */ },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(54.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Preparing your material…",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Extracting key concepts, formulas, and assessment topics for your study blocks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
