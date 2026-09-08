package com.studymate.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.studymate.app.navigation.Screen
import com.studymate.app.ui.components.StudyMateCard
import com.studymate.app.ui.theme.BorderSubtle
import com.studymate.app.ui.theme.WeakRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var pushNotifications by remember { mutableStateOf(true) }
    var quizReminders by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Notifications Group
            SettingsSectionHeader(title = "NOTIFICATIONS")
            StudyMateCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 12.dp
            ) {
                Column {
                    SettingsSwitchRow(
                        icon = Icons.Default.Notifications,
                        title = "Push Notifications",
                        subtitle = "Test assignments and material updates",
                        checked = pushNotifications,
                        onCheckedChange = { pushNotifications = it }
                    )
                    HorizontalDivider(thickness = 1.dp, color = BorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                    SettingsSwitchRow(
                        icon = Icons.Default.Notifications,
                        title = "Study Reminders",
                        subtitle = "Daily targets and quiz milestones",
                        checked = quizReminders,
                        onCheckedChange = { quizReminders = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Appearance Group
            SettingsSectionHeader(title = "APPEARANCE")
            StudyMateCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 12.dp
            ) {
                SettingsSwitchRow(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Theme",
                    subtitle = "Reduce glare during night study sessions",
                    checked = darkMode,
                    onCheckedChange = { darkMode = it }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Account & Security Group
            SettingsSectionHeader(title = "ACCOUNT & SECURITY")
            StudyMateCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 12.dp
            ) {
                Column {
                    SettingsNavRow(
                        icon = Icons.Default.Person,
                        title = "Edit Profile Info",
                        subtitle = "University ID, department, and contact",
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Profile edit mode active")
                            }
                        }
                    )
                    HorizontalDivider(thickness = 1.dp, color = BorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                    SettingsNavRow(
                        icon = Icons.Default.Lock,
                        title = "Change Password",
                        subtitle = "Update your account password",
                        onClick = { navController.navigate(Screen.ForgotPassword.route) }
                    )
                    HorizontalDivider(thickness = 1.dp, color = BorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                    SettingsNavRow(
                        icon = Icons.Default.Security,
                        title = "Privacy & Data",
                        subtitle = "Manage stored academic progress",
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("All data stored safely on campus servers")
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Help & Support
            SettingsSectionHeader(title = "HELP & SUPPORT")
            StudyMateCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 12.dp
            ) {
                Column {
                    SettingsNavRow(
                        icon = Icons.Default.HelpOutline,
                        title = "Help & Documentation",
                        subtitle = "StudyMate user guide & tutorial",
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("StudyMate Guide v2.4 · Contact campus IT support")
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Logout
            StudyMateCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                containerColor = MaterialTheme.colorScheme.surface,
                contentPadding = 14.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = WeakRed,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sign Out",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = WeakRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}
