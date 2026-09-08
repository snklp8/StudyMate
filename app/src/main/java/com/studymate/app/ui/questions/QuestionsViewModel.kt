package com.studymate.app.ui.questions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GeneratedQuestionItem(
    val id: String,
    val number: Int,
    val type: String, // "MCQ", "Viva", "Long Answer"
    val difficulty: String,
    val text: String,
    val options: List<String>? = null,
    val keyConcepts: List<String>? = null,
    val expectedPoints: List<String>? = null,
    val isBookmarked: Boolean = false
)

class QuestionsViewModel : ViewModel() {
    val selectedType = MutableStateFlow("MCQ") // "MCQ", "Viva", "Long Answer"
    val selectedDifficulty = MutableStateFlow("Medium")
    val questionCount = MutableStateFlow(5)
    val selectedTopic = MutableStateFlow("Binary Search Trees & Operations")

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generatedQuestions = MutableStateFlow<List<GeneratedQuestionItem>>(emptyList())
    val generatedQuestions: StateFlow<List<GeneratedQuestionItem>> = _generatedQuestions.asStateFlow()

    fun toggleBookmark(id: String) {
        _generatedQuestions.value = _generatedQuestions.value.map {
            if (it.id == id) it.copy(isBookmarked = !it.isBookmarked) else it
        }
    }

    fun generateQuestions(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isGenerating.value = true
            delay(1200)

            val type = selectedType.value
            val diff = selectedDifficulty.value
            val count = questionCount.value
            val list = mutableListOf<GeneratedQuestionItem>()

            when (type) {
                "MCQ" -> {
                    val mcqPool = listOf(
                        Triple(
                            "What is the average and worst-case time complexity of searching in a balanced Binary Search Tree?",
                            listOf("O(1) and O(n)", "O(log n) and O(log n)", "O(log n) and O(n)", "O(n) and O(n log n)"),
                            listOf("Time Complexity", "Tree Balance")
                        ),
                        Triple(
                            "Which tree traversal produces the elements of a Binary Search Tree in strictly ascending order?",
                            listOf("Preorder Traversal", "Postorder Traversal", "Inorder Traversal", "Level-order Traversal"),
                            listOf("Tree Traversal", "Sorting Property")
                        ),
                        Triple(
                            "When deleting a node with two children from a BST, which node replaces it?",
                            listOf("Root node", "Inorder predecessor or successor", "Immediate left child", "Deepest leaf node"),
                            listOf("Node Deletion", "Successor Search")
                        ),
                        Triple(
                            "What is the maximum number of nodes on level L of a binary tree (root at level 0)?",
                            listOf("2^L", "2^(L+1)", "2^(L-1)", "L^2"),
                            listOf("Tree Geometry", "Node Capacity")
                        ),
                        Triple(
                            "In an AVL tree, what is the maximum allowable difference in heights between left and right subtrees?",
                            listOf("0", "1", "2", "log n"),
                            listOf("AVL Balance", "Height Invariant")
                        ),
                        Triple(
                            "Which of the following data structures is optimal for implementing BFS on a tree?",
                            listOf("Stack", "Queue", "Priority Queue", "Hash Map"),
                            listOf("Breadth-First", "Queue Invariant")
                        )
                    )
                    for (i in 0 until count) {
                        val item = mcqPool[i % mcqPool.size]
                        list.add(
                            GeneratedQuestionItem(
                                id = "mcq_${i + 1}",
                                number = i + 1,
                                type = "MCQ",
                                difficulty = diff,
                                text = item.first,
                                options = item.second,
                                keyConcepts = item.third
                            )
                        )
                    }
                }
                "Viva" -> {
                    val vivaPool = listOf(
                        Pair(
                            "Explain why a Binary Search Tree can degenerate into a linked list and what impact this has on search operations.",
                            listOf("Degeneration occurs when inputs arrive pre-sorted", "Worst-case height becomes O(n)", "Search degrades from O(log n) to linear O(n)")
                        ),
                        Pair(
                            "How does an AVL self-balancing tree resolve an insertion violation in the Left-Right (LR) case?",
                            listOf("Performs a double rotation", "Left rotation on left child, followed by right rotation on current node", "Restores balance factor to {-1, 0, 1}")
                        ),
                        Pair(
                            "Compare the recursion stack space overhead of recursive DFS vs iterative BFS queue space.",
                            listOf("DFS stack is proportional to tree height O(h)", "BFS queue holds entire width up to O(n/2) leaves", "Skewed vs dense space tradeoffs")
                        ),
                        Pair(
                            "What is the significance of the Inorder Successor in a BST, and how do you locate it efficiently?",
                            listOf("Smallest key strictly greater than current node", "Found by taking one step right then leftmost leaf", "Vital for binary tree node removal")
                        ),
                        Pair(
                            "Why is Red-Black tree rebalancing typically faster for insertion-heavy workloads compared to AVL trees?",
                            listOf("Requires at most 2 rotations during insert", "Looser height constraint reduces frequent rotation rebalances", "Ideal for standard map/set library internals")
                        )
                    )
                    for (i in 0 until count) {
                        val item = vivaPool[i % vivaPool.size]
                        list.add(
                            GeneratedQuestionItem(
                                id = "viva_${i + 1}",
                                number = i + 1,
                                type = "Viva",
                                difficulty = diff,
                                text = item.first,
                                expectedPoints = item.second
                            )
                        )
                    }
                }
                "Long Answer" -> {
                    val longPool = listOf(
                        Pair(
                            "Describe the algorithm for inserting a new key into an AVL tree. Provide code or pseudocode and trace the rotation required when a balance factor reaches +2 with a left child balance factor of -1.",
                            listOf(
                                "Standard BST insertion via recursion",
                                "Update node heights on backtrack",
                                "Evaluate balance factor: balance = height(left) - height(right)",
                                "Apply Left-Right (LR) double rotation to restore tree balance"
                            )
                        ),
                        Pair(
                            "Analyze the algorithmic differences between Recursive and Iterative tree traversal techniques. Discuss space complexity, call stack overflow implications, and Morris Inorder Traversal.",
                            listOf(
                                "Call stack overhead in recursive DFS (O(h) auxiliary memory)",
                                "Iterative simulation using explicit std::stack",
                                "Threaded binary trees & Morris Traversal achieving O(1) auxiliary space",
                                "Tradeoff between modifying tree pointers temporarily vs execution time"
                            )
                        ),
                        Pair(
                            "Formulate a complete strategy for range search queries in a Binary Search Tree [K1, K2]. Provide complexity analysis and optimize traversal pruning.",
                            listOf(
                                "Recursive condition checking: only visit left if current > K1",
                                "Only visit right if current < K2",
                                "Output current if K1 <= current <= K2",
                                "Prunes unneeded subtrees to achieve O(k + log n) output-sensitive runtime"
                            )
                        )
                    )
                    for (i in 0 until count) {
                        val item = longPool[i % longPool.size]
                        list.add(
                            GeneratedQuestionItem(
                                id = "long_${i + 1}",
                                number = i + 1,
                                type = "Long Answer",
                                difficulty = diff,
                                text = item.first,
                                expectedPoints = item.second
                            )
                        )
                    }
                }
            }

            _generatedQuestions.value = list
            _isGenerating.value = false
            onComplete()
        }
    }
}
