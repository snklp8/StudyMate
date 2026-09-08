package com.studymate.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studymate.app.data.model.UserRole
import com.studymate.app.data.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface LoginState {
    object Idle : LoginState
    object Loading : LoginState
    data class Success(val user: User) : LoginState
    data class Error(val message: String) : LoginState
}

sealed interface RegisterState {
    object Idle : RegisterState
    object Loading : RegisterState
    data class Success(val user: User) : RegisterState
    data class Error(val message: String) : RegisterState
}

class AuthViewModel : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            delay(1000)
            
            if (email.isNotBlank() && password.isNotBlank()) {
                val role = if (email.contains("teacher", ignoreCase = true)) UserRole.TEACHER else UserRole.STUDENT
                val user = User(id = "1", name = "Test User", email = email, role = role)
                _currentUser.value = user
                _loginState.value = LoginState.Success(user)
            } else {
                _loginState.value = LoginState.Error("Invalid email or password")
            }
        }
    }

    fun register(name: String, email: String, password: String, role: UserRole) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            delay(1000)
            
            if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                val user = User(id = "1", name = name, email = email, role = role)
                _currentUser.value = user
                _registerState.value = RegisterState.Success(user)
            } else {
                _registerState.value = RegisterState.Error("All fields are required")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _loginState.value = LoginState.Idle
        _registerState.value = RegisterState.Idle
    }
}
