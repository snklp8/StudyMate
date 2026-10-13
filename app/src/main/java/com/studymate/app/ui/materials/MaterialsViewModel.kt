package com.studymate.app.ui.materials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studymate.app.data.model.Material
import com.studymate.app.data.model.MaterialType
import com.studymate.app.data.repository.SupabaseStorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MaterialItem(
    val id: String,
    val title: String,
    val subject: String,
    val type: String,
    val uploadDate: String,
    val fileSize: String,
    val status: String,
    val readingProgress: Float = 0.65f,
    val storagePath: String = ""
)

sealed class MaterialsUiState {
    object Loading : MaterialsUiState()
    data class Success(val materials: List<MaterialItem>) : MaterialsUiState()
    object Empty : MaterialsUiState()
    data class Error(val message: String) : MaterialsUiState()
}

sealed class UploadUiState {
    object Idle : UploadUiState()
    object Uploading : UploadUiState()
    data class Success(val message: String) : UploadUiState()
    data class Error(val message: String) : UploadUiState()
}

class MaterialsViewModel(
    private val repository: SupabaseStorageRepository = SupabaseStorageRepository()
) : ViewModel() {

    private val _materialsState = MutableStateFlow<MaterialsUiState>(MaterialsUiState.Loading)
    val materialsState: StateFlow<MaterialsUiState> = _materialsState.asStateFlow()

    private val _selectedMaterial = MutableStateFlow<MaterialItem?>(null)
    val selectedMaterial: StateFlow<MaterialItem?> = _selectedMaterial.asStateFlow()

    private val _uploadState = MutableStateFlow<UploadUiState>(UploadUiState.Idle)
    val uploadState: StateFlow<UploadUiState> = _uploadState.asStateFlow()

    init {
        loadMaterials()
    }

    fun loadMaterials() {
        viewModelScope.launch {
            _materialsState.value = MaterialsUiState.Loading
            try {
                val materials = repository.getMaterials()
                if (materials.isEmpty()) {
                    _materialsState.value = MaterialsUiState.Empty
                } else {
                    val items = materials.map { it.toMaterialItem() }
                    _materialsState.value = MaterialsUiState.Success(items)
                }
            } catch (e: Exception) {
                _materialsState.value = MaterialsUiState.Error(
                    e.localizedMessage ?: "Failed to load materials"
                )
            }
        }
    }

    fun loadMaterial(id: String) {
        viewModelScope.launch {
            try {
                val material = repository.getMaterial(id)
                _selectedMaterial.value = material?.toMaterialItem()
            } catch (_: Exception) {
                _selectedMaterial.value = null
            }
        }
    }

    fun uploadMaterial(
        title: String,
        subject: String,
        fileType: MaterialType,
        fileName: String,
        fileBytes: ByteArray
    ) {
        viewModelScope.launch {
            _uploadState.value = UploadUiState.Uploading
            val result = repository.uploadFile(
                title = title,
                subject = subject,
                fileType = fileType,
                fileName = fileName,
                fileBytes = fileBytes
            )
            result.fold(
                onSuccess = {
                    _uploadState.value = UploadUiState.Success("Material uploaded successfully!")
                    loadMaterials()
                },
                onFailure = { error ->
                    _uploadState.value = UploadUiState.Error(
                        error.localizedMessage ?: "Failed to upload material"
                    )
                }
            )
        }
    }

    fun deleteMaterial(id: String) {
        viewModelScope.launch {
            repository.deleteMaterial(id)
            loadMaterials()
        }
    }

    fun getSignedUrl(storagePath: String, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val result = repository.createSignedUrl(storagePath)
            onResult(result)
        }
    }

    fun resetUploadState() {
        _uploadState.value = UploadUiState.Idle
    }

    // -------------------------------------------------------------------------
    // Converters & Formatters
    // -------------------------------------------------------------------------

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
