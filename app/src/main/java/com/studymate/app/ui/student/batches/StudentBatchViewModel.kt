package com.studymate.app.ui.student.batches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studymate.app.data.model.Batch
import com.studymate.app.data.model.Material
import com.studymate.app.data.repository.BatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class StudentBatchesUiState {
    object Loading : StudentBatchesUiState()
    data class Success(val batches: List<Batch>) : StudentBatchesUiState()
    object Empty : StudentBatchesUiState()
    data class Error(val message: String) : StudentBatchesUiState()
}

sealed class JoinBatchUiState {
    object Idle : JoinBatchUiState()
    object Loading : JoinBatchUiState()
    data class Success(val batch: Batch) : JoinBatchUiState()
    data class Error(val message: String) : JoinBatchUiState()
}

class StudentBatchViewModel(
    private val repository: BatchRepository = BatchRepository()
) : ViewModel() {

    private val _batchesState = MutableStateFlow<StudentBatchesUiState>(StudentBatchesUiState.Loading)
    val batchesState: StateFlow<StudentBatchesUiState> = _batchesState.asStateFlow()

    private val _selectedBatch = MutableStateFlow<Batch?>(null)
    val selectedBatch: StateFlow<Batch?> = _selectedBatch.asStateFlow()

    private val _batchMaterials = MutableStateFlow<List<Material>>(emptyList())
    val batchMaterials: StateFlow<List<Material>> = _batchMaterials.asStateFlow()

    private val _joinState = MutableStateFlow<JoinBatchUiState>(JoinBatchUiState.Idle)
    val joinState: StateFlow<JoinBatchUiState> = _joinState.asStateFlow()

    init {
        loadBatches()
    }

    fun loadBatches() {
        viewModelScope.launch {
            _batchesState.value = StudentBatchesUiState.Loading
            try {
                val batches = repository.getStudentBatches()
                _batchesState.value = if (batches.isEmpty()) {
                    StudentBatchesUiState.Empty
                } else {
                    StudentBatchesUiState.Success(batches)
                }
            } catch (e: Exception) {
                _batchesState.value = StudentBatchesUiState.Error(
                    e.localizedMessage ?: "Failed to load batches"
                )
            }
        }
    }

    fun loadBatch(batchId: String) {
        viewModelScope.launch {
            _selectedBatch.value = repository.getBatchById(batchId)
        }
    }

    fun loadBatchMaterials(batchId: String) {
        viewModelScope.launch {
            try {
                _batchMaterials.value = repository.getBatchMaterials(batchId)
            } catch (_: Exception) {
                _batchMaterials.value = emptyList()
            }
        }
    }

    fun joinBatch(code: String) {
        viewModelScope.launch {
            _joinState.value = JoinBatchUiState.Loading
            val result = repository.joinBatch(code)
            result.fold(
                onSuccess = { batch ->
                    _joinState.value = JoinBatchUiState.Success(batch)
                    loadBatches()
                },
                onFailure = { error ->
                    _joinState.value = JoinBatchUiState.Error(
                        error.localizedMessage ?: "Failed to join batch"
                    )
                }
            )
        }
    }

    fun leaveBatch(batchId: String) {
        viewModelScope.launch {
            repository.leaveBatch(batchId)
            loadBatches()
        }
    }

    fun resetJoinState() {
        _joinState.value = JoinBatchUiState.Idle
    }
}
