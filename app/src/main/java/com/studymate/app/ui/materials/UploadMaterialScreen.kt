package com.studymate.app.ui.materials

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.studymate.app.data.model.MaterialType
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
import com.studymate.app.ui.theme.StudyMateNavy
import com.studymate.app.ui.theme.YouTubeRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

data class SelectedFileInfo(
    val name: String,
    val type: String,
    val size: String,
    val bytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SelectedFileInfo
        return name == other.name && type == other.type && size == other.size
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + size.hashCode()
        return result
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UploadMaterialScreen(
    navController: NavController,
    viewModel: MaterialsViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val uploadState by viewModel.uploadState.collectAsState()

    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("PDF") }
    val types = listOf("PDF", "PPT", "PPTX", "DOCX")

    var selectedFile by remember { mutableStateOf<SelectedFileInfo?>(null) }

    // Android Document Picker
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                var fileName = "document"
                var fileSize = 0L

                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) fileName = cursor.getString(nameIndex) ?: fileName
                        if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                    }
                }

                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null && bytes.isNotEmpty()) {
                    val sizeStr = when {
                        bytes.size < 1024 -> "${bytes.size} B"
                        bytes.size < 1024 * 1024 -> "${bytes.size / 1024} KB"
                        else -> String.format(Locale.US, "%.1f MB", bytes.size / (1024.0 * 1024.0))
                    }

                    val ext = fileName.substringAfterLast('.', "").uppercase()
                    val detectedType = when (ext) {
                        "PDF" -> "PDF"
                        "PPT" -> "PPT"
                        "PPTX" -> "PPTX"
                        "DOC", "DOCX" -> "DOCX"
                        else -> selectedType
                    }

                    selectedType = detectedType
                    selectedFile = SelectedFileInfo(
                        name = fileName,
                        type = detectedType,
                        size = sizeStr,
                        bytes = bytes
                    )

                    if (title.isBlank()) {
                        title = fileName.substringBeforeLast('.')
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Could not read content from selected file.")
                    }
                }
            } catch (e: Exception) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Error opening file: ${e.localizedMessage ?: "Unknown error"}")
                }
            }
        }
    }

    // Handle UploadState transitions
    LaunchedEffect(uploadState) {
        when (uploadState) {
            is UploadUiState.Success -> {
                snackbarHostState.showSnackbar((uploadState as UploadUiState.Success).message)
                viewModel.resetUploadState()
                delay(600)
                navController.popBackStack()
            }
            is UploadUiState.Error -> {
                snackbarHostState.showSnackbar((uploadState as UploadUiState.Error).message)
                viewModel.resetUploadState()
            }
            else -> Unit
        }
    }

    val isProcessing = uploadState is UploadUiState.Uploading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Upload Study Material", fontWeight = FontWeight.Bold)
                        Text(
                            "Add course notes, slides or documents",
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

            Text(
                text = "ATTACHED DOCUMENT",
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
                                val mimeTypes = arrayOf(
                                    "application/pdf",
                                    "application/vnd.ms-powerpoint",
                                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                                    "application/msword",
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                )
                                documentPickerLauncher.launch(mimeTypes)
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
                                text = "Supports PDF, PowerPoint (PPT, PPTX), Word (DOCX)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

            Spacer(modifier = Modifier.height(30.dp))

            // Upload action button
            StudyMateButton(
                text = if (isProcessing) "Uploading..." else "Upload & Process Material",
                enabled = !isProcessing,
                onClick = {
                    if (title.isBlank()) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Please enter a material title.")
                        }
                        return@StudyMateButton
                    }
                    if (subject.isBlank()) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Please enter a course / subject.")
                        }
                        return@StudyMateButton
                    }

                    if (selectedFile == null) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Please select a document to upload.")
                        }
                        return@StudyMateButton
                    }

                    val materialType = when (selectedType.uppercase()) {
                        "PDF" -> MaterialType.PDF
                        "PPT" -> MaterialType.PPT
                        "PPTX" -> MaterialType.PPTX
                        "DOC", "DOCX" -> MaterialType.DOCX
                        else -> MaterialType.PDF
                    }

                    viewModel.uploadMaterial(
                        title = title.trim(),
                        subject = subject.trim(),
                        fileType = materialType,
                        fileName = selectedFile!!.name,
                        fileBytes = selectedFile!!.bytes
                    )
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
                        text = "Uploading to Private Storage…",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Securing file in Supabase Storage and recording metadata in Firestore.",
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
