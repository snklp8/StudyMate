package com.studymate.app.data.repository

import com.studymate.app.data.model.User
import com.studymate.app.data.model.UserRole

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String, role: UserRole): Result<User>
    suspend fun getCurrentUser(): User?
    suspend fun logout()
}
