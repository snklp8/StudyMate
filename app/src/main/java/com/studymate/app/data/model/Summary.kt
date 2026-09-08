package com.studymate.app.data.model

data class Summary(
    val id: String = "",
    val materialId: String = "",
    val overview: String = "",
    val concepts: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    val formulas: List<String> = emptyList(),
    val importantPoints: List<String> = emptyList()
)
