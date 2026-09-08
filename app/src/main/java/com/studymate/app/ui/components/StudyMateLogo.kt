package com.studymate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studymate.app.ui.theme.StudyMateIndigo

@Composable
fun StudyMateLogoBadge(
    modifier: Modifier = Modifier,
    iconSize: Dp = 64.dp,
    showTagline: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .clip(RoundedCornerShape(iconSize * 0.28f))
                .background(StudyMateIndigo),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                contentDescription = "StudyMate Logo",
                tint = Color.White,
                modifier = Modifier.size(iconSize * 0.54f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "StudyMate",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        if (showTagline) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Your personal academic cockpit",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
