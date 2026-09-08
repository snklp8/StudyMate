package com.studymate.app.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Quiz
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
fun StudentBottomNav(navController: NavController) {
    val items = listOf(
        Triple(Screen.StudentHome, "Home", Pair(Icons.Filled.Home, Icons.Outlined.Home)),
        Triple(Screen.StudentMaterials, "Materials", Pair(Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook)),
        Triple(Screen.QuizSetup, "Quiz", Pair(Icons.Filled.Quiz, Icons.Outlined.Quiz)),
        Triple(Screen.Progress, "Progress", Pair(Icons.AutoMirrored.Filled.TrendingUp, Icons.AutoMirrored.Outlined.TrendingUp)),
        Triple(Screen.StudentProfile, "Profile", Pair(Icons.Filled.Person, Icons.Outlined.Person))
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        androidx.compose.foundation.layout.Column {
            HorizontalDivider(thickness = 1.dp, color = BorderSubtle)
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { (screen, label, icons) ->
                    val selected = currentRoute == screen.route
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
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.StudentHome.route) { saveState = true }
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

