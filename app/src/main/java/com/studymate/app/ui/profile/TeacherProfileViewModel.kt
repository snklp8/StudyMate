package com.studymate.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.studymate.app.data.repository.BatchRepository
import com.studymate.app.data.repository.SupabaseStorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class TeacherProfileUiState(
    val isLoading: Boolean = true,
    val name: String = "Faculty Instructor",
    val email: String = "",
    val initials: String = "T",
    val batchesCount: Int = 0,
    val totalStudents: Int = 0,
    val materialsCount: Int = 0
)

class TeacherProfileViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val batchRepository: BatchRepository = BatchRepository(),
    private val storageRepository: SupabaseStorageRepository = SupabaseStorageRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherProfileUiState())
    val uiState: StateFlow<TeacherProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun refresh() {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = auth.currentUser
            val uid = user?.uid
            val userEmail = user?.email ?: ""

            val userName = withContext(Dispatchers.IO) {
                if (uid != null) {
                    try {
                        val doc = firestore.collection("users").document(uid).get().await()
                        doc.getString("name")?.trim().takeIf { !it.isNullOrBlank() }
                            ?: user.displayName
                            ?: userEmail.substringBefore('@')
                    } catch (_: Exception) {
                        user.displayName ?: userEmail.substringBefore('@')
                    }
                } else {
                    "Faculty Instructor"
                }
            } ?: "Faculty Instructor"

            val initials = userName.split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .map { it.first().uppercaseChar() }
                .joinToString("")
                .ifBlank { "T" }

            val batchesDeferred = async(Dispatchers.IO) {
                try { batchRepository.getTeacherBatches() } catch (_: Exception) { emptyList() }
            }
            val materialsDeferred = async(Dispatchers.IO) {
                try { storageRepository.getMaterials().size } catch (_: Exception) { 0 }
            }

            val batches = batchesDeferred.await()
            val materialsCount = materialsDeferred.await()
            val totalStudents = batches.sumOf { it.studentCount }

            _uiState.value = TeacherProfileUiState(
                isLoading = false,
                name = userName,
                email = userEmail,
                initials = initials,
                batchesCount = batches.size,
                totalStudents = totalStudents,
                materialsCount = materialsCount
            )
        }
    }
}
