package com.studymate.app.data.mock

import com.studymate.app.data.model.Summary
import com.studymate.app.data.repository.SummaryRepository
import kotlinx.coroutines.delay

class MockSummaryRepository : SummaryRepository {
    private val summaries = mutableMapOf(
        "mat1" to Summary(
            id = "sum1",
            materialId = "mat1",
            overview = "An overview of operating system structures, processes, and memory management.",
            concepts = listOf("Process Management", "Memory Management", "File Systems", "Concurrency"),
            keywords = listOf("OS", "Kernel", "Thread", "Deadlock", "Paging"),
            formulas = emptyList(),
            importantPoints = listOf("OS acts as an intermediary between user and hardware.", "Deadlocks require mutual exclusion, hold and wait, no preemption, and circular wait.")
        ),
        "mat2" to Summary(
            id = "sum2",
            materialId = "mat2",
            overview = "Core concepts of Data Structures including Arrays, Linked Lists, Trees, and Graphs.",
            concepts = listOf("Arrays", "Linked Lists", "Trees", "Graphs", "Sorting"),
            keywords = listOf("Node", "Edge", "Root", "Traversal", "O(n)"),
            formulas = listOf("Time Complexity: O(1), O(log n), O(n), O(n^2)"),
            importantPoints = listOf("Arrays have fixed size.", "Binary search trees allow fast lookup, addition, and removal.")
        )
    )

    override suspend fun getSummary(materialId: String): Summary? {
        delay(500)
        return summaries[materialId]
    }

    override suspend fun generateSummary(materialId: String): Result<Summary> {
        delay(500)
        val newSummary = Summary(
            id = "sum${System.currentTimeMillis()}",
            materialId = materialId,
            overview = "Auto-generated summary for the selected material. It contains vital insights.",
            concepts = listOf("Concept A", "Concept B", "Concept C"),
            keywords = listOf("Key1", "Key2", "Key3"),
            formulas = listOf("Formula X"),
            importantPoints = listOf("Important Point 1", "Important Point 2")
        )
        summaries[materialId] = newSummary
        return Result.success(newSummary)
    }
}
