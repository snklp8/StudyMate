package com.studymate.app.ui.materials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studymate.app.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MaterialItem(
    val id: String,
    val title: String,
    val subject: String,
    val type: String,
    val uploadDate: String,
    val fileSize: String,
    val status: String,
    val readingProgress: Float = 0.65f
)

sealed class MaterialsUiState {
    object Loading : MaterialsUiState()
    data class Success(val materials: List<MaterialItem>) : MaterialsUiState()
    object Empty : MaterialsUiState()
    data class Error(val message: String) : MaterialsUiState()
}

class MaterialsViewModel : ViewModel() {
    private val _materialsState = MutableStateFlow<MaterialsUiState>(MaterialsUiState.Loading)
    val materialsState: StateFlow<MaterialsUiState> = _materialsState.asStateFlow()

    private val _selectedMaterial = MutableStateFlow<MaterialItem?>(null)
    val selectedMaterial: StateFlow<MaterialItem?> = _selectedMaterial.asStateFlow()

    private val mockList = listOf(
        MaterialItem("1", "Data Structures PDF", "Computer Science", "PDF", "Oct 10, 2023", "2 MB", "Processed"),
        MaterialItem("2", "Operating Systems PPT", "Computer Science", "PPT", "Oct 12, 2023", "5 MB", "Processed"),
        MaterialItem("3", "DBMS YouTube", "Computer Science", "YOUTUBE", "Oct 15, 2023", "-", "Processed"),
        MaterialItem("4", "Algorithms Notes", "Computer Science", "DOC", "Oct 16, 2023", "1 MB", "Processed"),
        MaterialItem("5", "Computer Networks", "Computer Science", "PDF", "Oct 17, 2023", "3 MB", "Processed"),
        MaterialItem("6", "Software Engineering", "Computer Science", "PPT", "Oct 18, 2023", "4 MB", "Processed"),
        MaterialItem("7", "AI Basics", "Computer Science", "PDF", "Oct 19, 2023", "2 MB", "Processed"),
        MaterialItem("8", "Machine Learning", "Computer Science", "YOUTUBE", "Oct 20, 2023", "-", "Processed")
    )

    init {
        loadMaterials()
    }

    fun loadMaterials() {
        viewModelScope.launch {
            _materialsState.value = MaterialsUiState.Loading
            delay(800)
            _materialsState.value = MaterialsUiState.Success(mockList)
        }
    }

    fun loadMaterial(id: String) {
        viewModelScope.launch {
            _selectedMaterial.value = mockList.find { it.id == id }
        }
    }

    fun uploadMaterial(title: String, subject: String, type: String) {
        // Mock upload
        viewModelScope.launch {
            // Usually we'd upload and then refresh the list
        }
    }
}
