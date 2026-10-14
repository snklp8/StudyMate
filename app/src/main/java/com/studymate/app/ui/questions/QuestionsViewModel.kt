package com.studymate.app.ui.questions

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studymate.app.data.model.Difficulty
import com.studymate.app.data.model.QuestionType
import com.studymate.app.data.repository.GeminiQuestionRepository
import com.studymate.app.data.repository.SupabaseStorageRepository
import com.studymate.app.ui.materials.MaterialItem
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

class QuestionsViewModel(
    private val questionRepository: GeminiQuestionRepository = GeminiQuestionRepository(),
    private val storageRepository: SupabaseStorageRepository = SupabaseStorageRepository()
) : ViewModel() {

    val selectedType = MutableStateFlow("MCQ")
    val selectedDifficulty = MutableStateFlow("Medium")
    val questionCount = MutableStateFlow(5)
    val selectedTopic = MutableStateFlow("All Document Topics")

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentMaterial = MutableStateFlow<MaterialItem?>(null)
    val currentMaterial: StateFlow<MaterialItem?> = _currentMaterial.asStateFlow()

    private val _generatedQuestions = MutableStateFlow<List<GeneratedQuestionItem>>(emptyList())
    val generatedQuestions: StateFlow<List<GeneratedQuestionItem>> = _generatedQuestions.asStateFlow()

    fun loadMaterial(materialId: String) {
        if (materialId.isBlank()) return
        viewModelScope.launch {
            val mat = storageRepository.getMaterial(materialId)
            if (mat != null) {
                _currentMaterial.value = MaterialItem(
                    id = mat.id,
                    title = mat.title,
                    subject = mat.subject,
                    type = mat.type.name,
                    uploadDate = "",
                    fileSize = "",
                    status = "Ready",
                    storagePath = mat.fileUrl ?: ""
                )
                selectedTopic.value = "${mat.subject}: ${mat.title}"
            }
        }
    }

    fun toggleBookmark(id: String) {
        _generatedQuestions.value = _generatedQuestions.value.map {
            if (it.id == id) it.copy(isBookmarked = !it.isBookmarked) else it
        }
    }

    fun generateQuestions(materialId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isGenerating.value = true
            _errorMessage.value = null

            val type = selectedType.value
            val diff = selectedDifficulty.value
            val count = questionCount.value

            Log.d("QuestionsViewModel", "Starting generateQuestions: mat=$materialId type=$type diff=$diff count=$count")

            val result = questionRepository.generateQuestionItems(
                materialId = materialId,
                type = type,
                difficulty = diff,
                count = count
            )

            result.fold(
                onSuccess = { list ->
                    Log.d("QuestionsViewModel", "Success: ${list.size} questions generated")
                    _generatedQuestions.value = list
                    _errorMessage.value = null
                    _isGenerating.value = false
                    onComplete()
                },
                onFailure = { error ->
                    val msg = error.localizedMessage ?: "Failed to generate questions using Gemini."
                    Log.e("QuestionsViewModel", "FAIL: $msg", error)
                    _errorMessage.value = msg
                    _isGenerating.value = false
                }
            )
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun generateFallbackQuestions(type: String, diff: String, count: Int) {
        val list = mutableListOf<GeneratedQuestionItem>()
        val title = _currentMaterial.value?.title ?: "Document"
        for (i in 1..count) {
            when (type) {
                "MCQ" -> list.add(
                    GeneratedQuestionItem(
                        id = "q_$i",
                        number = i,
                        type = "MCQ",
                        difficulty = diff,
                        text = "Sample Question $i on $title?",
                        options = listOf("Key concept analysis", "Alternative interpretation", "Standard proof method", "Edge-case variation"),
                        keyConcepts = listOf("Core Theory", "Application")
                    )
                )
                "Viva" -> list.add(
                    GeneratedQuestionItem(
                        id = "v_$i",
                        number = i,
                        type = "Viva",
                        difficulty = diff,
                        text = "Explain the fundamental mechanism of topic $i in $title.",
                        expectedPoints = listOf("Clear definition of concepts", "Tradeoff analysis", "Practical scenario application")
                    )
                )
                else -> list.add(
                    GeneratedQuestionItem(
                        id = "l_$i",
                        number = i,
                        type = "Long Answer",
                        difficulty = diff,
                        text = "Provide a comprehensive analysis of the concepts discussed in section $i of $title.",
                        expectedPoints = listOf("Detailed theoretical foundations", "Mathematical or logical derivation", "Critical evaluation")
                    )
                )
            }
        }
        _generatedQuestions.value = list
    }
}
