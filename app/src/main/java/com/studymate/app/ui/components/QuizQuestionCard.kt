package com.studymate.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studymate.app.ui.theme.BorderSubtle
import com.studymate.app.ui.theme.SuccessGreen
import com.studymate.app.ui.theme.SuccessGreenContainer
import com.studymate.app.ui.theme.WeakRed
import com.studymate.app.ui.theme.WeakRedContainer

@Composable
fun QuizQuestionCard(
    questionNumber: Int,
    question: String,
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    showCorrectAnswer: Boolean,
    correctAnswer: String?,
    modifier: Modifier = Modifier
) {
    val labels = listOf("A", "B", "C", "D", "E", "F")

    StudyMateCard(modifier = modifier.fillMaxWidth(), contentPadding = 18.dp) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = "Question $questionNumber",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = question,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.titleMedium.lineHeight
        )
        Spacer(modifier = Modifier.height(16.dp))

        options.forEachIndexed { index, option ->
            val isSelected = option == selectedOption
            val isCorrect = showCorrectAnswer && option == correctAnswer
            val isWrong = showCorrectAnswer && isSelected && option != correctAnswer

            val (bgColor, borderColor, textColor) = when {
                isCorrect -> Triple(SuccessGreenContainer, SuccessGreen, SuccessGreen)
                isWrong -> Triple(WeakRedContainer, WeakRed, WeakRed)
                isSelected -> Triple(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.primary
                )
                else -> Triple(MaterialTheme.colorScheme.surface, BorderSubtle, MaterialTheme.colorScheme.onSurface)
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable(enabled = !showCorrectAnswer) { onOptionSelected(option) },
                shape = RoundedCornerShape(10.dp),
                color = bgColor,
                border = BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = if (isSelected || isCorrect || isWrong) borderColor else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = labels.getOrElse(index) { "${index + 1}" },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected || isCorrect || isWrong) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected || isCorrect) FontWeight.SemiBold else FontWeight.Normal,
                        color = textColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

