package com.studymate.app.ui.summary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.studymate.app.data.model.Summary
import com.studymate.app.navigation.Screen
import com.studymate.app.ui.components.EmptyState
import com.studymate.app.ui.components.ErrorState
import com.studymate.app.ui.components.LoadingState
import com.studymate.app.ui.components.StudyMateButton
import com.studymate.app.ui.components.StudyMateCard
import com.studymate.app.ui.components.StudyMateOutlinedButton
import com.studymate.app.ui.components.resolveFileTypeVisual
import com.studymate.app.ui.materials.MaterialItem
import com.studymate.app.ui.theme.BorderSubtle
import com.studymate.app.ui.theme.DocBlue
import com.studymate.app.ui.theme.DocBlueContainer
import com.studymate.app.ui.theme.PdfRed
import com.studymate.app.ui.theme.PdfRedContainer
import com.studymate.app.ui.theme.StudyMateNavy
import com.studymate.app.ui.theme.SuccessGreen
import com.studymate.app.ui.theme.SuccessGreenContainer
import com.studymate.app.ui.theme.WarningOrange
import com.studymate.app.ui.theme.WarningOrangeContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    navController: NavController,
    materialId: String = "",
    viewModel: SummaryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userMaterials by viewModel.userMaterials.collectAsState()
    val selectedMaterial by viewModel.selectedMaterial.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    val context = LocalContext.current
    var showMaterialPicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(materialId) {
        viewModel.initialize(materialId)
    }

    if (showDeleteConfirmation && selectedMaterial != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Material?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to delete '${selectedMaterial?.title}'? This will remove the uploaded document from cloud storage and remove its generated study notes."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        viewModel.deleteCurrentMaterial()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = selectedMaterial?.title ?: "AI Study Notes",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = if (selectedMaterial != null) "${selectedMaterial?.subject} · Gemini Notes" else "Select a study material",
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
                actions = {
                    if (selectedMaterial != null) {
                        IconButton(onClick = { viewModel.openMaterial(context) }) {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = "View Material",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Delete Material",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    if (userMaterials.size > 1) {
                        IconButton(onClick = { showMaterialPicker = true }) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Switch Document", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    if (uiState is SummaryUiState.Success) {
                        IconButton(
                            onClick = { viewModel.regenerateSummary() },
                            enabled = !isGenerating
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Regenerate Summary", tint = MaterialTheme.colorScheme.primary)
                        }
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
        ) {
            // Horizontal Material Selector (Quick Bar)
            if (userMaterials.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(userMaterials) { mat ->
                        val isSelected = selectedMaterial?.id == mat.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectMaterial(mat) },
                            label = {
                                Text(
                                    text = mat.title,
                                    maxLines = 1,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                val visual = resolveFileTypeVisual(mat.type)
                                Icon(
                                    imageVector = visual.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) Color.White else visual.tint
                                )
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
            }

            // Main State Content Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (val state = uiState) {
                    is SummaryUiState.Loading -> {
                        LoadingState(
                            message = state.message,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is SummaryUiState.Empty -> {
                        EmptySummaryView(
                            material = state.material,
                            message = state.message,
                            onGenerate = { viewModel.generateSummary() },
                            onUpload = { navController.navigate(Screen.UploadMaterial.route) }
                        )
                    }

                    is SummaryUiState.Error -> {
                        ErrorSummaryView(
                            message = state.message,
                            isScannedPdf = state.isScannedPdf,
                            canRetry = state.canRetry,
                            onRetry = { viewModel.generateSummary() },
                            onSelectOther = { showMaterialPicker = true }
                        )
                    }

                    is SummaryUiState.Success -> {
                        SuccessSummaryView(
                            summary = state.summary,
                            material = state.material,
                            isGenerating = isGenerating,
                            onRegenerate = { viewModel.regenerateSummary() }
                        )
                    }

                    SummaryUiState.Idle -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Modal Bottom Sheet: Select Material
        if (showMaterialPicker) {
            ModalBottomSheet(
                onDismissRequest = { showMaterialPicker = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Select Document for AI Notes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose an uploaded PDF from your library",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(userMaterials) { mat ->
                            val isSelected = selectedMaterial?.id == mat.id
                            StudyMateCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    viewModel.selectMaterial(mat)
                                    showMaterialPicker = false
                                },
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                                borderColor = if (isSelected) MaterialTheme.colorScheme.primary else BorderSubtle,
                                contentPadding = 12.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(PdfRedContainer, shape = RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            tint = PdfRed,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = mat.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${mat.subject} · ${mat.fileSize}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptySummaryView(
    material: MaterialItem?,
    message: String,
    onGenerate: () -> Unit,
    onUpload: () -> Unit
) {
    if (material == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Study Materials Available",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Upload a course PDF to generate AI study notes, key formulas, and exam revision points.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            StudyMateButton(
                text = "Upload Study Material",
                leadingIcon = Icons.Default.UploadFile,
                onClick = onUpload
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            StudyMateCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 20.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = material.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${material.subject} · ${material.type} (${material.fileSize})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Generate structured study notes including a concise overview, core concepts, strict definitions, important formulas, and high-yield revision points.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    StudyMateButton(
                        text = "Generate AI Study Notes",
                        leadingIcon = Icons.Default.AutoAwesome,
                        onClick = onGenerate,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorSummaryView(
    message: String,
    isScannedPdf: Boolean,
    canRetry: Boolean,
    onRetry: () -> Unit,
    onSelectOther: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isScannedPdf) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(WarningOrangeContainer, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = WarningOrange,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Scanned PDF Detected",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            StudyMateButton(
                text = "Select Another PDF",
                onClick = onSelectOther
            )
        } else {
            ErrorState(
                message = message,
                onRetry = onRetry
            )
        }
    }
}

@Composable
private fun SuccessSummaryView(
    summary: Summary,
    material: MaterialItem,
    isGenerating: Boolean,
    onRegenerate: () -> Unit
) {
    val formattedDate = remember(summary.createdAt) {
        SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.US).format(Date(summary.createdAt))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp)
    ) {
        // Document Header Summary Banner
        item {
            StudyMateCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = StudyMateNavy,
                borderWidth = 0.dp,
                contentPadding = 16.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "AI-SYNTHESIZED NOTES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF93C5FD),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SuccessGreenContainer.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "Source-Verified",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = material.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${material.subject} · Created on $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    StudyMateOutlinedButton(
                        text = if (isGenerating) "Regenerating..." else "Regenerate Notes",
                        leadingIcon = Icons.Default.Refresh,
                        enabled = !isGenerating,
                        onClick = onRegenerate
                    )
                }
            }
        }

        // Section 1: Executive Summary
        item {
            ExpandableSummarySection(
                title = "Executive Summary",
                icon = Icons.Default.AutoAwesome,
                iconTint = MaterialTheme.colorScheme.primary,
                expandedByDefault = true
            ) {
                Text(
                    text = summary.overview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )
            }
        }

        // Section 2: Outline & Key Headings (if present)
        if (summary.headings.isNotEmpty()) {
            item {
                ExpandableSummarySection(
                    title = "Document Outline & Headings",
                    icon = Icons.Default.FormatListBulleted,
                    iconTint = DocBlue,
                    expandedByDefault = true
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        summary.headings.forEachIndexed { index, heading ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = heading,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Core Concepts
        if (summary.concepts.isNotEmpty()) {
            item {
                ExpandableSummarySection(
                    title = "Core Concepts",
                    icon = Icons.Default.Psychology,
                    iconTint = Color(0xFF7C3AED),
                    expandedByDefault = true
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        summary.concepts.forEach { concept ->
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 7.dp)
                                        .size(7.dp)
                                        .background(Color(0xFF7C3AED), shape = CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = concept,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Key Definitions
        if (summary.definitions.isNotEmpty()) {
            item {
                ExpandableSummarySection(
                    title = "Key Definitions",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    iconTint = Color(0xFF0F766E),
                    expandedByDefault = true
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        summary.definitions.forEach { definition ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = definition,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(12.dp),
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 5: Important Formulas & Equations
        item {
            ExpandableSummarySection(
                title = "Important Formulas & Equations",
                icon = Icons.Default.Calculate,
                iconTint = Color(0xFFEA580C),
                expandedByDefault = true
            ) {
                if (summary.formulas.isEmpty()) {
                    Text(
                        text = "No explicit mathematical or scientific formulas were present in the source material.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        summary.formulas.forEach { formula ->
                            Surface(
                                color = StudyMateNavy,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = formula,
                                    modifier = Modifier.padding(14.dp),
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 6: Quick Revision & High-Yield Points
        val revisionPoints = if (summary.revisionSection.isNotEmpty()) summary.revisionSection else summary.importantPoints
        if (revisionPoints.isNotEmpty()) {
            item {
                ExpandableSummarySection(
                    title = "Quick Revision & Exam Focus",
                    icon = Icons.Default.CheckCircle,
                    iconTint = SuccessGreen,
                    expandedByDefault = true
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        revisionPoints.forEach { point ->
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(18.dp).padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = point,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableSummarySection(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    expandedByDefault: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(expandedByDefault) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "expand")

    StudyMateCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(iconTint.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    HorizontalDivider(thickness = 1.dp, color = BorderSubtle)
                    Spacer(modifier = Modifier.height(14.dp))
                    content()
                }
            }
        }
    }
}
