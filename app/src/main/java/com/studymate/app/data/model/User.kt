package com.studymate.app.data.model

enum class UserRole { STUDENT, TEACHER }

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.STUDENT,
    val course: String? = null,
    val semester: Int? = null,
    val department: String? = null,
    val profileImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
