package com.studymate.app.ui.teacher.batches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studymate.app.data.model.Batch
import com.studymate.app.data.model.BatchStudent
import com.studymate.app.data.model.Material
import com.studymate.app.data.repository.BatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BatchesUiState {
    object Loading : BatchesUiState()
    data class Success(val batches: List<Batch>) : BatchesUiState()
    object Empty : BatchesUiState()
    data class Error(val message: String) : BatchesUiState()
}

sealed class CreateBatchUiState {
    object Idle : CreateBatchUiState()
    object Loading : CreateBatchUiState()
    data class Success(val batch: Batch) : CreateBatchUiState()
    data class Error(val message: String) : CreateBatchUiState()
}

class BatchViewModel(
    private val repository: BatchRepository = BatchRepository()
) : ViewModel() {
    private val _batchesState = MutableStateFlow<BatchesUiState>(BatchesUiState.Loading)
    val batchesState: StateFlow<BatchesUiState> = _batchesState.asStateFlow()

    private val _selectedBatch = MutableStateFlow<Batch?>(null)
    val selectedBatch: StateFlow<Batch?> = _selectedBatch.asStateFlow()

    private val _batchStudents = MutableStateFlow<List<BatchStudent>>(emptyList())
    val batchStudents: StateFlow<List<BatchStudent>> = _batchStudents.asStateFlow()

    private val _batchMaterials = MutableStateFlow<List<Material>>(emptyList())
    val batchMaterials: StateFlow<List<Material>> = _batchMaterials.asStateFlow()

    private val _createState = MutableStateFlow<CreateBatchUiState>(CreateBatchUiState.Idle)
    val createState: StateFlow<CreateBatchUiState> = _createState.asStateFlow()

    init {
        loadBatches()
    }

    fun loadBatches() {
        viewModelScope.launch {
            _batchesState.value = BatchesUiState.Loading
            try {
                val list = repository.getTeacherBatches()
                if (list.isEmpty()) {
                    _batchesState.value = BatchesUiState.Empty
                } else {
                    _batchesState.value = BatchesUiState.Success(list)
                }
            } catch (e: Exception) {
                _batchesState.value = BatchesUiState.Error(e.localizedMessage ?: "Failed to load batches")
            }
        }
    }

    fun loadBatch(id: String) {
        viewModelScope.launch {
            try {
                val b = repository.getBatchById(id)
                _selectedBatch.value = b
            } catch (_: Exception) {
                _selectedBatch.value = null
            }
        }
    }

    fun loadBatchStudents(batchId: String) {
        viewModelScope.launch {
            try {
                val students = repository.getBatchStudents(batchId)
                _batchStudents.value = students
            } catch (_: Exception) {
                _batchStudents.value = emptyList()
            }
        }
    }

    fun loadBatchMaterials(batchId: String) {
        viewModelScope.launch {
            try {
                val materials = repository.getBatchMaterials(batchId)
                _batchMaterials.value = materials
            } catch (_: Exception) {
                _batchMaterials.value = emptyList()
            }
        }
    }

    fun createBatch(
        name: String,
        course: String,
        semester: Int,
        year: String,
        description: String,
        onSuccess: (Batch) -> Unit = {}
    ) {
        viewModelScope.launch {
            _createState.value = CreateBatchUiState.Loading
            val result = repository.createBatch(
                name = name,
                course = course,
                semester = semester,
                academicYear = year,
                description = description
            )
            result.fold(
                onSuccess = { created ->
                    _createState.value = CreateBatchUiState.Success(created)
                    loadBatches()
                    onSuccess(created)
                },
                onFailure = { error ->
                    _createState.value = CreateBatchUiState.Error(error.localizedMessage ?: "Failed to create batch")
                }
            )
        }
    }

    fun resetCreateState() {
        _createState.value = CreateBatchUiState.Idle
    }
}
