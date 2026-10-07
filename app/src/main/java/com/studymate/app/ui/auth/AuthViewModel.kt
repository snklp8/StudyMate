package com.studymate.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studymate.app.data.model.User
import com.studymate.app.data.model.UserRole
import com.studymate.app.data.repository.AuthRepository
import com.studymate.app.data.repository.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

sealed interface ForgotPasswordState {
    object Idle : ForgotPasswordState
    object Loading : ForgotPasswordState
    object Success : ForgotPasswordState
    data class Error(val message: String) : ForgotPasswordState
}

class AuthViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = authRepository.login(email, password)
            result.onSuccess { user ->
                _currentUser.value = user
                _loginState.value = LoginState.Success(user)
            }.onFailure { exception ->
                _loginState.value = LoginState.Error(exception.message ?: "Authentication failed")
            }
        }
    }

    fun register(name: String, email: String, password: String, role: UserRole) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            val result = authRepository.register(name, email, password, role)
            result.onSuccess { user ->
                _currentUser.value = user
                _registerState.value = RegisterState.Success(user)
            }.onFailure { exception ->
                _registerState.value = RegisterState.Error(exception.message ?: "Registration failed")
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _forgotPasswordState.value = ForgotPasswordState.Loading
            val result = authRepository.sendPasswordResetEmail(email)
            result.onSuccess {
                _forgotPasswordState.value = ForgotPasswordState.Success
            }.onFailure { exception ->
                _forgotPasswordState.value = ForgotPasswordState.Error(
                    exception.message ?: "Failed to dispatch reset email"
                )
            }
        }
    }

    fun checkCurrentSession(onResolved: (User?) -> Unit) {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _currentUser.value = user
            onResolved(user)
        }
    }

    fun logout(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            authRepository.logout()
            _currentUser.value = null
            _loginState.value = LoginState.Idle
            _registerState.value = RegisterState.Idle
            _forgotPasswordState.value = ForgotPasswordState.Idle
            onComplete()
        }
    }

    fun resetStates() {
        _loginState.value = LoginState.Idle
        _registerState.value = RegisterState.Idle
        _forgotPasswordState.value = ForgotPasswordState.Idle
    }
}
