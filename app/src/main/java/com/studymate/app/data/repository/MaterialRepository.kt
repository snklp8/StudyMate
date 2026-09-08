package com.studymate.app.data.repository

import com.studymate.app.data.model.Material

interface MaterialRepository {
    suspend fun getMaterials(): List<Material>
    suspend fun getMaterial(id: String): Material?
    suspend fun uploadMaterial(material: Material): Result<Material>
    suspend fun deleteMaterial(id: String): Result<Unit>
}
