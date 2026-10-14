package com.studymate.app.data.mock

import com.studymate.app.data.model.Summary
import com.studymate.app.data.repository.SummaryRepository
import kotlinx.coroutines.delay

class MockSummaryRepository : SummaryRepository {
    private val summaries = mutableMapOf(
        "mat1" to Summary(
            id = "sum1",
            materialId = "mat1",
            title = "Operating System Concepts",
            overview = "An overview of operating system structures, processes, and memory management.",
            headings = listOf("Kernel Architecture", "Process Scheduling", "Virtual Memory"),
            concepts = listOf("Process Management", "Memory Management", "File Systems", "Concurrency"),
            definitions = listOf(
                "Deadlock: A state where a set of processes are blocked because each process is holding a resource and waiting for another.",
                "Paging: A memory management scheme that eliminates the need for contiguous allocation of physical memory."
            ),
            keywords = listOf("OS", "Kernel", "Thread", "Deadlock", "Paging"),
            formulas = emptyList(),
            importantPoints = listOf("OS acts as an intermediary between user and hardware.", "Deadlocks require mutual exclusion, hold and wait, no preemption, and circular wait."),
            revisionSection = listOf(
                "Understand the 4 Coffman conditions required for deadlocks.",
                "Compare preemptive vs non-preemptive scheduling algorithms."
            )
        ),
        "mat2" to Summary(
            id = "sum2",
            materialId = "mat2",
            title = "Data Structures & Algorithms",
            overview = "Core concepts of Data Structures including Arrays, Linked Lists, Trees, and Graphs.",
            headings = listOf("Linear Data Structures", "Tree Traversals", "Asymptotic Analysis"),
            concepts = listOf("Arrays", "Linked Lists", "Trees", "Graphs", "Sorting"),
            definitions = listOf(
                "Binary Search Tree: A node-based binary tree data structure with logarithmic average search time.",
                "Time Complexity: The computational complexity that describes the amount of computer time it takes to run an algorithm."
            ),
            keywords = listOf("Node", "Edge", "Root", "Traversal", "O(n)"),
            formulas = listOf("Time Complexity: O(1), O(log n), O(n), O(n^2)"),
            importantPoints = listOf("Arrays have fixed size.", "Binary search trees allow fast lookup, addition, and removal."),
            revisionSection = listOf(
                "Review difference between BFS (Queue) and DFS (Stack/Recursion).",
                "Practice Binary Search mid-point calculation avoiding overflow."
            )
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
            title = "Auto-Generated Study Notes",
            overview = "Auto-generated summary for the selected material. It contains vital insights synthesized from the source document.",
            headings = listOf("Overview & Scope", "Key Mechanics", "Applications"),
            concepts = listOf("Concept A: Core foundation", "Concept B: Practical implementation"),
            definitions = listOf("Core Mechanism: The fundamental operating principle described in the text."),
            keywords = listOf("Analysis", "Architecture", "Review"),
            formulas = listOf("Standard Calculation: Metric = Value / Time"),
            importantPoints = listOf("Carefully follow established constraints.", "Review key definitions before examination."),
            revisionSection = listOf("Verify key terms and relationships.", "Complete self-assessment quiz.")
        )
        summaries[materialId] = newSummary
        return Result.success(newSummary)
    }
}
