package com.studymate.app.data.mock

import com.studymate.app.data.model.User
import com.studymate.app.data.model.UserRole
import com.studymate.app.data.repository.AuthRepository
import kotlinx.coroutines.delay

class MockAuthRepository : AuthRepository {
    private var currentUser: User? = User(
        id = "user123",
        name = "Alex Smith",
        email = "alex.smith@university.edu",
        role = UserRole.STUDENT,
        course = "Computer Science",
        semester = 5,
        department = "Engineering"
    )

    override suspend fun login(email: String, password: String): Result<User> {
        delay(500)
        return currentUser?.let {
            Result.success(it.copy(email = email))
        } ?: Result.failure(Exception("Login failed"))
    }

    override suspend fun register(name: String, email: String, password: String, role: UserRole): Result<User> {
        delay(500)
        val newUser = User(
            id = "user${System.currentTimeMillis()}",
            name = name,
            email = email,
            role = role,
            course = "Computer Science",
            semester = 1,
            department = "Engineering"
        )
        currentUser = newUser
        return Result.success(newUser)
    }

    override suspend fun getCurrentUser(): User? {
        delay(200)
        return currentUser
    }

    override suspend fun logout() {
        delay(200)
        currentUser = null
    }
}
