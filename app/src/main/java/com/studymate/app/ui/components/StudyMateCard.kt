package com.studymate.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.studymate.app.ui.theme.BorderSubtle

@Composable
fun StudyMateCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = BorderSubtle,
    borderWidth: Dp = 1.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardColors = CardDefaults.cardColors(containerColor = containerColor)
    val borderStroke = if (borderWidth > 0.dp) BorderStroke(borderWidth, borderColor) else null

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            colors = cardColors,
            border = borderStroke,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            colors = cardColors,
            border = borderStroke,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    }
}

