package com.studymate.app.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SummaryData(
    val title: String,
    val overallSummary: String,
    val concepts: List<String>,
    val keywords: List<String>,
    val formulas: List<String>,
    val points: List<String>
)

class SummaryViewModel : ViewModel() {
    private val _summary = MutableStateFlow<SummaryData?>(null)
    val summary: StateFlow<SummaryData?> = _summary.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadSummary(materialId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            delay(500)
            _summary.value = SummaryData(
                title = "Data Structures - Binary Search",
                overallSummary = "Binary search is an efficient algorithm for finding an item from a sorted list of items. It works by repeatedly dividing in half the portion of the list that could contain the item, until you've narrowed down the possible locations to just one.\n\nThis method is vastly superior to linear search for large datasets because of its logarithmic time complexity.",
                concepts = listOf("Binary Search", "Time Complexity", "Sorted Arrays", "Divide and Conquer"),
                keywords = listOf("binary search", "O(log n)", "sorted array", "mid-point", "iterative", "recursive"),
                formulas = listOf("Time Complexity: O(log n)", "Space: O(1) iterative / O(log n) recursive"),
                points = listOf(
                    "1. Array must be sorted before applying binary search.",
                    "2. Find the mid element of the array.",
                    "3. If target == mid, return mid index.",
                    "4. If target < mid, search in left half.",
                    "5. If target > mid, search in right half."
                )
            )
            _isLoading.value = false
        }
    }
}
