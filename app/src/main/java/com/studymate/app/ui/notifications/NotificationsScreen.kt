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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Warning
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
import androidx.navigation.NavController
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

data class NotificationItemData(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val type: String,
    val isRead: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    var filter by remember { mutableStateOf("All") } // "All", "Unread", "Read"

    var notifications by remember {
        mutableStateOf(
            listOf(
                NotificationItemData(
                    id = "n1",
                    title = "Teacher assigned a test",
                    message = "Prof. Kumar posted Midterm Mock: Algorithms & Data Structures for Batch CSE-A.",
                    timeAgo = "25m ago",
                    type = "TEST",
                    isRead = false
                ),
                NotificationItemData(
                    id = "n2",
                    title = "New quiz available",
                    message = "Practice quiz on 'Binary Trees & BST Basics' is ready with 10 adaptive questions.",
                    timeAgo = "1h ago",
                    type = "QUIZ",
                    isRead = false
                ),
                NotificationItemData(
                    id = "n3",
                    title = "Material uploaded",
                    message = "New lecture slides 'Process Scheduling & Synchronization.pptx' added to Operating Systems.",
                    timeAgo = "3h ago",
                    type = "MATERIAL",
                    isRead = false
                ),
                NotificationItemData(
                    id = "n4",
                    title = "Weak topic detected",
                    message = "Your assessment error rate on 'AVL Tree Rotations' is higher than normal. Revision recommended.",
                    timeAgo = "Yesterday",
                    type = "WEAK_TOPIC",
                    isRead = true
                ),
                NotificationItemData(
                    id = "n5",
                    title = "Progress milestone achieved!",
                    message = "Congratulations! You completed 12 study modules this week. Keep up the momentum.",
                    timeAgo = "2 days ago",
                    type = "MILESTONE",
                    isRead = true
                ),
                NotificationItemData(
                    id = "n6",
                    title = "Important topic recommendation",
                    message = "'Normalization (1NF-BCNF)' has high exam frequency. Check your tailored study guide.",
                    timeAgo = "3 days ago",
                    type = "RECOMMENDATION",
                    isRead = true
                )
            )
        )
    }

    val filteredList = when (filter) {
        "Unread" -> notifications.filter { !it.isRead }
        "Read" -> notifications.filter { it.isRead }
        else -> notifications
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Notifications", fontWeight = FontWeight.Bold)
                        val unreadCount = notifications.count { !it.isRead }
                        Text(
                            if (unreadCount > 0) "$unreadCount unread updates" else "All caught up",
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
                    IconButton(onClick = {
                        notifications = notifications.map { it.copy(isRead = true) }
                    }) {
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

            // Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Unread", "Read").forEach { tab ->
                    val isSelected = filter == tab
                    val badgeCount = when (tab) {
                        "Unread" -> notifications.count { !it.isRead }
                        "Read" -> notifications.count { it.isRead }
                        else -> notifications.size
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

                        StudyMateCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    notifications = notifications.map {
                                        if (it.id == item.id) it.copy(isRead = true) else it
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
                                        text = item.timeAgo,
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

private fun resolveNotificationVisual(type: String): Triple<ImageVector, Color, Color> {
    return when (type) {
        "TEST" -> Triple(Icons.AutoMirrored.Filled.Assignment, PdfRed, PdfRedContainer)
        "QUIZ" -> Triple(Icons.Default.Quiz, WarningOrange, WarningOrangeContainer)
        "MATERIAL" -> Triple(Icons.AutoMirrored.Filled.MenuBook, DocBlue, DocBlueContainer)
        "WEAK_TOPIC" -> Triple(Icons.Default.Warning, PdfRed, PdfRedContainer)
        "MILESTONE" -> Triple(Icons.AutoMirrored.Filled.TrendingUp, SuccessGreen, SuccessGreenContainer)
        else -> Triple(Icons.Default.AutoAwesome, Color(0xFF4F46E5), Color(0xFFEEF2FF))
    }
}
