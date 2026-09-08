package com.studymate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.studymate.app.data.model.Material
import com.studymate.app.data.model.MaterialType
import com.studymate.app.data.model.ProcessingStatus
import com.studymate.app.ui.theme.DocBlue
import com.studymate.app.ui.theme.DocBlueContainer
import com.studymate.app.ui.theme.PdfRed
import com.studymate.app.ui.theme.PdfRedContainer
import com.studymate.app.ui.theme.PptOrange
import com.studymate.app.ui.theme.PptOrangeContainer
import com.studymate.app.ui.theme.SuccessGreen
import com.studymate.app.ui.theme.SuccessGreenContainer
import com.studymate.app.ui.theme.WarningOrange
import com.studymate.app.ui.theme.WarningOrangeContainer
import com.studymate.app.ui.theme.WeakRed
import com.studymate.app.ui.theme.WeakRedContainer
import com.studymate.app.ui.theme.YouTubeRed
import com.studymate.app.ui.theme.YouTubeRedContainer

data class FileTypeVisual(
    val icon: ImageVector,
    val tint: Color,
    val container: Color,
    val label: String
)

fun resolveFileTypeVisual(typeStr: String): FileTypeVisual {
    val upper = typeStr.uppercase()
    return when {
        upper.contains("PDF") -> FileTypeVisual(Icons.Default.PictureAsPdf, PdfRed, PdfRedContainer, "PDF")
        upper.contains("PPT") -> FileTypeVisual(Icons.Default.Slideshow, PptOrange, PptOrangeContainer, "PPT")
        upper.contains("YOUTUBE") || upper.contains("VIDEO") -> FileTypeVisual(Icons.Default.PlayCircle, YouTubeRed, YouTubeRedContainer, "VIDEO")
        else -> FileTypeVisual(Icons.Default.Description, DocBlue, DocBlueContainer, "DOC")
    }
}

@Composable
fun MaterialCard(
    material: Material,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val visual = when (material.type) {
        MaterialType.PDF -> FileTypeVisual(Icons.Default.PictureAsPdf, PdfRed, PdfRedContainer, "PDF")
        MaterialType.PPT, MaterialType.PPTX -> FileTypeVisual(Icons.Default.Slideshow, PptOrange, PptOrangeContainer, "PPT")
        MaterialType.DOC, MaterialType.DOCX -> FileTypeVisual(Icons.Default.Description, DocBlue, DocBlueContainer, "DOC")
        MaterialType.YOUTUBE -> FileTypeVisual(Icons.Default.PlayCircle, YouTubeRed, YouTubeRedContainer, "YOUTUBE")
    }

    val (statusText, statusColor, statusContainer) = when (material.processingStatus) {
        ProcessingStatus.PENDING -> Triple("Pending", WarningOrange, WarningOrangeContainer)
        ProcessingStatus.PROCESSING -> Triple("Processing", DocBlue, DocBlueContainer)
        ProcessingStatus.COMPLETED -> Triple("Ready", SuccessGreen, SuccessGreenContainer)
        ProcessingStatus.FAILED -> Triple("Failed", WeakRed, WeakRedContainer)
    }

    MaterialCardRow(
        title = material.title,
        subtitle = "${material.subject} • ${visual.label}",
        visual = visual,
        statusText = statusText,
        statusColor = statusColor,
        statusContainer = statusContainer,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun MaterialCardItem(
    title: String,
    subject: String,
    type: String,
    modifier: Modifier = Modifier,
    metadata: String? = null,
    status: String? = null,
    onClick: (() -> Unit)? = null
) {
    val visual = resolveFileTypeVisual(type)
    val subtitleText = buildString {
        append(subject)
        append(" • ")
        append(visual.label)
        if (!metadata.isNullOrBlank()) {
            append(" • ")
            append(metadata)
        }
    }

    MaterialCardRow(
        title = title,
        subtitle = subtitleText,
        visual = visual,
        statusText = status ?: "Ready",
        statusColor = SuccessGreen,
        statusContainer = SuccessGreenContainer,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun MaterialCardRow(
    title: String,
    subtitle: String,
    visual: FileTypeVisual,
    statusText: String,
    statusColor: Color,
    statusContainer: Color,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    StudyMateCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color = visual.container, shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = visual.icon,
                    contentDescription = visual.label,
                    tint = visual.tint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = statusContainer
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

