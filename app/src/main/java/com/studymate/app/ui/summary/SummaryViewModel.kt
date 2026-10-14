package com.studymate.app.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studymate.app.data.model.Material
import com.studymate.app.data.model.MaterialType
import com.studymate.app.data.model.Summary
import com.studymate.app.data.repository.FirestoreSummaryRepository
import com.studymate.app.data.repository.SummaryRepository
import com.studymate.app.data.repository.SupabaseStorageRepository
import com.studymate.app.data.util.EmptyDocumentException
import com.studymate.app.data.util.ScannedPdfException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.studymate.app.ui.materials.MaterialItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class SummaryUiState {
    object Idle : SummaryUiState()
    data class Loading(val message: String = "Synthesizing study notes with Gemini AI...") : SummaryUiState()
    data class Success(val summary: Summary, val material: MaterialItem) : SummaryUiState()
    data class Empty(val message: String, val material: MaterialItem? = null) : SummaryUiState()
    data class Error(
        val message: String,
        val isScannedPdf: Boolean = false,
        val canRetry: Boolean = true
    ) : SummaryUiState()
}

class SummaryViewModel(
    private val summaryRepository: SummaryRepository = FirestoreSummaryRepository(),
    private val storageRepository: SupabaseStorageRepository = SupabaseStorageRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<SummaryUiState>(SummaryUiState.Idle)
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    private val _userMaterials = MutableStateFlow<List<MaterialItem>>(emptyList())
    val userMaterials: StateFlow<List<MaterialItem>> = _userMaterials.asStateFlow()

    private val _selectedMaterial = MutableStateFlow<MaterialItem?>(null)
    val selectedMaterial: StateFlow<MaterialItem?> = _selectedMaterial.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private var hasInitialized = false

    /**
     * Initializes the Summary screen by loading only the authenticated user's uploaded materials,
     * resolving the requested [targetMaterialId], and loading any cached summary.
     */
    fun initialize(targetMaterialId: String?) {
        if (hasInitialized && _selectedMaterial.value?.id == targetMaterialId) return
        hasInitialized = true

        viewModelScope.launch {
            _uiState.value = SummaryUiState.Loading("Loading your study materials...")
            try {
                // Load only the logged-in user's uploaded materials that support summaries (PDF, PPT, PPTX, DOC, DOCX)
                val rawMaterials = storageRepository.getMaterials()
                val items = rawMaterials
                    .filter { isSummaryEligible(it.type, it.title) }
                    .map { it.toMaterialItem() }
                _userMaterials.value = items

                if (items.isEmpty()) {
                    _selectedMaterial.value = null
                    _uiState.value = SummaryUiState.Empty("No summary-eligible documents found. Please upload a PDF, Word, or PowerPoint file.")
                    return@launch
                }

                // If an ineligible material was specifically requested, inform the user
                val targetedRaw = rawMaterials.find { it.id == targetMaterialId }
                if (targetedRaw != null && !isSummaryEligible(targetedRaw.type, targetedRaw.title)) {
                    _selectedMaterial.value = null
                    _uiState.value = SummaryUiState.Empty("AI Summaries are not available for ${targetedRaw.type.name.lowercase()} files. Please select a PDF, Word, or PowerPoint document.")
                    return@launch
                }

                // Match requested material or default to first PDF or first document
                val resolvedMaterial = if (!targetMaterialId.isNullOrBlank() && targetMaterialId != "select" && targetMaterialId != "1") {
                    items.find { it.id == targetMaterialId } ?: items.firstOrNull { it.type.equals("PDF", ignoreCase = true) } ?: items.first()
                } else {
                    items.firstOrNull { it.type.equals("PDF", ignoreCase = true) } ?: items.first()
                }

                _selectedMaterial.value = resolvedMaterial
                loadSummaryForMaterial(resolvedMaterial)
            } catch (e: Exception) {
                _uiState.value = SummaryUiState.Error(
                    message = e.localizedMessage ?: "Failed to load study materials."
                )
            }
        }
    }

    private fun isSummaryEligible(type: MaterialType, title: String): Boolean {
        // All remaining MaterialTypes (PDF, PPT, PPTX, DOC, DOCX) are eligible
        return true
    }

    private fun isSummaryEligible(typeStr: String, title: String): Boolean {
        val upper = typeStr.uppercase()
        // Only eligible if it is a document type we still support
        return upper in listOf("PDF", "PPT", "PPTX", "DOC", "DOCX")
    }

    /**
     * Selects a different material from the user's uploaded files.
     */
    fun selectMaterial(material: MaterialItem) {
        if (_selectedMaterial.value?.id == material.id) return
        _selectedMaterial.value = material
        loadSummaryForMaterial(material)
    }

    /**
     * Checks Firestore for existing generated study notes. If found, displays them.
     * Otherwise, shows an empty prompt offering generation.
     */
    private fun loadSummaryForMaterial(material: MaterialItem) {
        viewModelScope.launch {
            _uiState.value = SummaryUiState.Loading("Checking existing study notes...")
            try {
                val existingSummary = summaryRepository.getSummary(material.id)
                if (existingSummary != null && existingSummary.overview.isNotBlank()) {
                    _uiState.value = SummaryUiState.Success(
                        summary = existingSummary,
                        material = material
                    )
                } else {
                    _uiState.value = SummaryUiState.Empty(
                        message = "No study notes generated yet for '${material.title}'.",
                        material = material
                    )
                }
            } catch (e: Exception) {
                _uiState.value = SummaryUiState.Empty(
                    message = "Ready to generate study notes for '${material.title}'.",
                    material = material
                )
            }
        }
    }

    /**
     * Extracts PDF text, verifies ownership, and invokes Gemini via Supabase Edge Function.
     */
    fun generateSummary() {
        val material = _selectedMaterial.value ?: return
        if (!isSummaryEligible(material.type, material.title)) {
            _uiState.value = SummaryUiState.Error(
                message = "Summaries are only available for PDF, Word, and PowerPoint files.",
                canRetry = false
            )
            return
        }
        viewModelScope.launch {
            _isGenerating.value = true
            _uiState.value = SummaryUiState.Loading("Extracting text and generating structured notes with Gemini...")

            val result = summaryRepository.generateSummary(material.id)
            result.fold(
                onSuccess = { summary ->
                    _isGenerating.value = false
                    _uiState.value = SummaryUiState.Success(
                        summary = summary,
                        material = material
                    )
                },
                onFailure = { error ->
                    _isGenerating.value = false
                    val isScanned = error is ScannedPdfException
                    _uiState.value = SummaryUiState.Error(
                        message = error.localizedMessage ?: "Failed to generate summary.",
                        isScannedPdf = isScanned,
                        canRetry = !isScanned
                    )
                }
            )
        }
    }

    /**
     * Regenerates the summary for the currently selected material.
     */
    fun regenerateSummary() {
        generateSummary()
    }

    /**
     * Generates a signed access URL for the material and opens it in the device PDF viewer / browser.
     */
    fun openMaterial(context: Context, material: MaterialItem? = _selectedMaterial.value) {
        val mat = material ?: return
        val path = mat.storagePath
        if (path.isBlank()) {
            Toast.makeText(context, "No storage file path found for this document.", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            val signedUrlResult = storageRepository.createSignedUrl(path, expiresInSeconds = 3600L)
            signedUrlResult.fold(
                onSuccess = { signedUrl ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(signedUrl)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not open document: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                },
                onFailure = { err ->
                    Toast.makeText(context, "Failed to load document: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    /**
     * Deletes the currently selected material from Supabase Storage and Firestore,
     * cleans up any generated summaries, and updates the active selection.
     */
    fun deleteCurrentMaterial(onDeleted: () -> Unit = {}) {
        val current = _selectedMaterial.value ?: return
        viewModelScope.launch {
            _uiState.value = SummaryUiState.Loading("Deleting '${current.title}'...")
            val result = storageRepository.deleteMaterial(current.id)
            result.fold(
                onSuccess = {
                    // Clean up cached summary in Firestore
                    runCatching {
                        FirebaseFirestore.getInstance().collection("summaries").document(current.id).delete()
                    }

                    val updatedList = _userMaterials.value.filter { it.id != current.id }
                    _userMaterials.value = updatedList

                    if (updatedList.isEmpty()) {
                        _selectedMaterial.value = null
                        _uiState.value = SummaryUiState.Empty("No uploaded study materials found. Please upload a PDF first.")
                    } else {
                        val next = updatedList.first()
                        _selectedMaterial.value = next
                        loadSummaryForMaterial(next)
                    }
                    onDeleted()
                },
                onFailure = { error ->
                    _uiState.value = SummaryUiState.Error(
                        message = "Failed to delete material: ${error.localizedMessage ?: "Unknown error"}",
                        canRetry = true
                    )
                }
            )
        }
    }

    private fun Material.toMaterialItem(): MaterialItem {
        val dateFormatted = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(createdAt))
        val sizeFormatted = when {
            size == null || size <= 0L -> "-"
            size < 1024L -> "$size B"
            size < 1024L * 1024L -> "${size / 1024L} KB"
            else -> String.format(Locale.US, "%.1f MB", size / (1024.0 * 1024.0))
        }
        val statusFormatted = when (processingStatus) {
            com.studymate.app.data.model.ProcessingStatus.PENDING -> "Pending"
            com.studymate.app.data.model.ProcessingStatus.PROCESSING -> "Processing"
            com.studymate.app.data.model.ProcessingStatus.COMPLETED -> "Ready"
            com.studymate.app.data.model.ProcessingStatus.FAILED -> "Failed"
        }

        return MaterialItem(
            id = id,
            title = title,
            subject = subject.ifBlank { "General" },
            type = type.name,
            uploadDate = dateFormatted,
            fileSize = sizeFormatted,
            status = statusFormatted,
            readingProgress = 0.0f,
            storagePath = fileUrl ?: ""
        )
    }
}
