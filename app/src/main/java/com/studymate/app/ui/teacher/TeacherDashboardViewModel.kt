package com.studymate.app.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.studymate.app.data.model.Batch
import com.studymate.app.data.model.Test
import com.studymate.app.data.repository.BatchRepository
import com.studymate.app.data.repository.SupabaseStorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class TeacherDashboardUiState(
    val isLoading: Boolean = false,
    val teacherName: String = "Instructor",
    val teacherEmail: String = "",
    val teacherInitials: String = "T",
    val totalStudents: Int = 0,
    val batchCount: Int = 0,
    val materialCount: Int = 0,
    val testCount: Int = 0,
    val batches: List<Batch> = emptyList(),
    val recentTests: List<Test> = emptyList()
)

class TeacherDashboardViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val batchRepository: BatchRepository = BatchRepository(),
    private val materialRepository: SupabaseStorageRepository = SupabaseStorageRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeacherDashboardUiState(isLoading = true))
    val uiState: StateFlow<TeacherDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                val uid = user?.uid
                val teacherEmail = user?.email ?: ""

                val teacherName = withContext(Dispatchers.IO) {
                    if (uid != null) {
                        try {
                            val doc = firestore.collection("users").document(uid).get().await()
                            doc.getString("name")?.trim().takeIf { !it.isNullOrBlank() }
                                ?: user.displayName
                                ?: teacherEmail.substringBefore('@')
                        } catch (_: Exception) {
                            user.displayName ?: teacherEmail.substringBefore('@')
                        }
                    } else {
                        "Instructor"
                    }
                } ?: "Instructor"

                val teacherInitials = teacherName.split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .map { it.first().uppercaseChar() }
                    .joinToString("")
                    .ifBlank { "T" }

                val batches = batchRepository.getTeacherBatches()
                val materials = materialRepository.getMaterials()

                // Calculate total students across all batches
                val totalStudents = batches.sumOf { it.studentCount }

                _uiState.value = TeacherDashboardUiState(
                    isLoading = false,
                    teacherName = teacherName,
                    teacherEmail = teacherEmail,
                    teacherInitials = teacherInitials,
                    totalStudents = totalStudents,
                    batchCount = batches.size,
                    materialCount = materials.size,
                    testCount = 0,
                    batches = batches.take(3),
                    recentTests = emptyList()
                )
            } catch (_: Exception) {
                _uiState.value = TeacherDashboardUiState(
                    isLoading = false,
                    totalStudents = 0,
                    batchCount = 0,
                    materialCount = 0,
                    testCount = 0
                )
            }
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadDashboardData()
    }
}
