package com.studymate.app.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.studymate.app.ui.theme.BorderSubtle

@Composable
fun TeacherBottomNav(navController: NavController) {
    val items = listOf(
        Triple(Screen.TeacherDashboard, "Dashboard", Pair(Icons.Filled.Dashboard, Icons.Outlined.Dashboard)),
        Triple(Screen.BatchList, "Batches", Pair(Icons.Filled.Groups, Icons.Outlined.Groups)),
        Triple(Screen.TestList, "Tests", Pair(Icons.AutoMirrored.Filled.Assignment, Icons.AutoMirrored.Outlined.Assignment)),
        Triple(Screen.BatchPerformance, "Performance", Pair(Icons.AutoMirrored.Filled.TrendingUp, Icons.AutoMirrored.Outlined.TrendingUp)),
        Triple(Screen.TeacherProfile, "Profile", Pair(Icons.Filled.Person, Icons.Outlined.Person))
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            HorizontalDivider(thickness = 1.dp, color = BorderSubtle)
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { (screen, label, icons) ->
                    val selected = currentRoute == screen.route ||
                            (screen == Screen.BatchPerformance && currentRoute?.startsWith("batch_performance") == true)
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selected) icons.first else icons.second,
                                contentDescription = label
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        selected = selected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        onClick = {
                            if (!selected) {
                                val destination = if (screen == Screen.BatchPerformance) {
                                    Screen.BatchPerformance.createRoute("all")
                                } else {
                                    screen.route
                                }
                                navController.navigate(destination) {
                                    popUpTo(Screen.TeacherDashboard.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
