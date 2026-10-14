package com.studymate.app.data.model

data class Summary(
    val id: String = "",
    val ownerId: String = "",
    val materialId: String = "",
    val title: String = "",
    val overview: String = "",
    val headings: List<String> = emptyList(),
    val concepts: List<String> = emptyList(),
    val definitions: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    val formulas: List<String> = emptyList(),
    val importantPoints: List<String> = emptyList(),
    val revisionSection: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
