package com.studymate.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studymate.app.ui.theme.WeakRed
import com.studymate.app.ui.theme.WeakRedContainer

@Composable
fun TopicChip(
    topic: String,
    isWeak: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val containerColor = if (isWeak) WeakRedContainer else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isWeak) WeakRed else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isWeak) WeakRed.copy(alpha = 0.25f) else Color.Transparent

    Surface(
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier,
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isWeak) {
                Surface(
                    modifier = Modifier.size(6.dp),
                    shape = CircleShape,
                    color = WeakRed
                ) {}
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = topic,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

