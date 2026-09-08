package com.studymate.app.data.model

enum class MaterialType { PDF, PPT, PPTX, DOC, DOCX, YOUTUBE }
enum class ProcessingStatus { PENDING, PROCESSING, COMPLETED, FAILED }

data class Material(
    val id: String = "",
    val uploadedBy: String = "",
    val title: String = "",
    val type: MaterialType = MaterialType.PDF,
    val fileUrl: String? = null,
    val youtubeUrl: String? = null,
    val subject: String = "",
    val size: Long? = null,
    val processingStatus: ProcessingStatus = ProcessingStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)
