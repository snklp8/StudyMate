package com.studymate.app.data.mock

import com.studymate.app.data.model.Material
import com.studymate.app.data.model.MaterialType
import com.studymate.app.data.model.ProcessingStatus
import com.studymate.app.data.repository.MaterialRepository
import kotlinx.coroutines.delay

class MockMaterialRepository : MaterialRepository {
    private val materials = mutableListOf(
        Material(
            id = "mat1",
            uploadedBy = "Dr. Alan Turing",
            title = "Introduction to Operating Systems",
            type = MaterialType.PDF,
            subject = "Operating Systems",
            size = 2500000,
            processingStatus = ProcessingStatus.COMPLETED
        ),
        Material(
            id = "mat2",
            uploadedBy = "Dr. Grace Hopper",
            title = "Data Structures and Algorithms",
            type = MaterialType.PPTX,
            subject = "Data Structures",
            size = 5000000,
            processingStatus = ProcessingStatus.COMPLETED
        ),
        Material(
            id = "mat3",
            uploadedBy = "Dr. E.F. Codd",
            title = "Relational Database Concepts",
            type = MaterialType.PDF,
            subject = "DBMS",
            size = 3500000,
            processingStatus = ProcessingStatus.COMPLETED
        )
    )

    override suspend fun getMaterials(): List<Material> {
        delay(500)
        return materials.toList()
    }

    override suspend fun getMaterial(id: String): Material? {
        delay(300)
        return materials.find { it.id == id }
    }

    override suspend fun uploadMaterial(material: Material): Result<Material> {
        delay(500)
        val newMaterial = material.copy(id = "mat${System.currentTimeMillis()}", processingStatus = ProcessingStatus.COMPLETED)
        materials.add(newMaterial)
        return Result.success(newMaterial)
    }

    override suspend fun deleteMaterial(id: String): Result<Unit> {
        delay(500)
        val removed = materials.removeIf { it.id == id }
        return if (removed) Result.success(Unit) else Result.failure(Exception("Material not found"))
    }
}
