package com.studymate.app.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.studymate.app.data.model.AppNotification
import com.studymate.app.ui.components.EmptyState
import com.studymate.app.ui.components.StudyMateCard
import com.studymate.app.ui.theme.BorderSubtle
import com.studymate.app.ui.theme.DocBlue
import com.studymate.app.ui.theme.DocBlueContainer
import com.studymate.app.ui.theme.PdfRed
import com.studymate.app.ui.theme.PdfRedContainer
import com.studymate.app.ui.theme.SuccessGreen
import com.studymate.app.ui.theme.SuccessGreenContainer
import com.studymate.app.ui.theme.WarningOrange
import com.studymate.app.ui.theme.WarningOrangeContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationsViewModel = viewModel()
) {
    var filter by remember { mutableStateOf("All") }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Notifications", fontWeight = FontWeight.Bold)
                        val subtitle = when (uiState) {
                            is NotificationsUiState.Success -> {
                                val unread = (uiState as NotificationsUiState.Success).notifications.count { !it.isRead }
                                if (unread > 0) "$unread unread updates" else "All caught up"
                            }
                            is NotificationsUiState.Loading -> "Loading..."
                            else -> "All caught up"
                        }
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.markAllAsRead() }) {
                        Icon(Icons.Default.DoneAll, contentDescription = "Mark all as read")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            when (uiState) {
                is NotificationsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is NotificationsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            (uiState as NotificationsUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is NotificationsUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(bottom = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            title = "No Notifications Yet",
                            message = "You'll receive notifications when teachers upload materials or you join batches.",
                            icon = Icons.Default.NotificationsNone
                        )
                    }
                }
                is NotificationsUiState.Success -> {
                    val allNotifications = (uiState as NotificationsUiState.Success).notifications
                    val filteredList = when (filter) {
                        "Unread" -> allNotifications.filter { !it.isRead }
                        "Read" -> allNotifications.filter { it.isRead }
                        else -> allNotifications
                    }

                    // Filter Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Unread", "Read").forEach { tab ->
                            val isSelected = filter == tab
                            val badgeCount = when (tab) {
                                "Unread" -> allNotifications.count { !it.isRead }
                                "Read" -> allNotifications.count { it.isRead }
                                else -> allNotifications.size
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = { filter = tab },
                                label = {
                                    Text(
                                        text = "$tab ($badgeCount)",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) MaterialTheme.colorScheme.primary else BorderSubtle
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (filteredList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyState(
                                title = "No Notifications",
                                message = if (filter == "Unread") "You're all caught up! No unread messages." else "No notification history found.",
                                icon = Icons.Default.NotificationsNone
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredList, key = { it.id }) { item ->
                                val (icon, tint, bg) = resolveNotificationVisual(item.type)
                                val timeAgo = formatTimeAgo(item.createdAt)

                                StudyMateCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (!item.isRead) {
                                                viewModel.markAsRead(item.id)
                                            }
                                        },
                                    containerColor = if (!item.isRead) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                                    borderColor = if (!item.isRead) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else BorderSubtle,
                                    contentPadding = 14.dp
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(bg, shape = RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = tint,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = item.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = if (!item.isRead) FontWeight.Bold else FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )

                                                if (!item.isRead) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = item.message,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 18.sp
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = timeAgo,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun resolveNotificationVisual(type: String): Triple<ImageVector, Color, Color> {
    return when (type) {
        "TEST" -> Triple(Icons.AutoMirrored.Filled.Assignment, PdfRed, PdfRedContainer)
        "QUIZ" -> Triple(Icons.Default.Quiz, WarningOrange, WarningOrangeContainer)
        "MATERIAL" -> Triple(Icons.AutoMirrored.Filled.MenuBook, DocBlue, DocBlueContainer)
        "BATCH_MATERIAL" -> Triple(Icons.AutoMirrored.Filled.MenuBook, DocBlue, DocBlueContainer)
        "BATCH_JOIN" -> Triple(Icons.Default.Groups, SuccessGreen, SuccessGreenContainer)
        "WEAK_TOPIC" -> Triple(Icons.Default.Warning, PdfRed, PdfRedContainer)
        "MILESTONE" -> Triple(Icons.AutoMirrored.Filled.TrendingUp, SuccessGreen, SuccessGreenContainer)
        else -> Triple(Icons.Default.AutoAwesome, Color(0xFF4F46E5), Color(0xFFEEF2FF))
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 2 -> "Yesterday"
        days < 7 -> "${days} days ago"
        else -> {
            val sdf = java.text.SimpleDateFormat("MMM d", java.util.Locale.US)
            sdf.format(java.util.Date(timestamp))
        }
    }
}
