package com.studymate.app.data.model

data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
