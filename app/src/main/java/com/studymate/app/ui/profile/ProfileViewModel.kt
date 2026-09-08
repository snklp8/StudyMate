package com.studymate.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: String
)

class ProfileViewModel : ViewModel() {
    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            // Mock profile
            _userState.value = User(
                id = "u1",
                name = "Dr. Kumar",
                email = "kumar@university.edu",
                role = "teacher"
            )
        }
    }

    fun logout() {
        // Handle logout logic
        _userState.value = null
    }
}
