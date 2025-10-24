package com.josprox.redesosi.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import com.josprox.redesosi.navigation.AppScreen
import com.josprox.redesosi.ui.screens.settings.SettingsScreen

// Se definen los items del menú inferior
private sealed class BottomNavItem(
    val route: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val label: String
) {
    object Learn : BottomNavItem(
        AppScreen.SubjectList.route,
        Icons.AutoMirrored.Filled.List,
        Icons.AutoMirrored.Outlined.List,
        "Aprende"
    )
    object Test : BottomNavItem(
        AppScreen.Test.route,
        Icons.Default.School,
        Icons.Outlined.School,
        "Test"
    )
    object Grades : BottomNavItem(
        AppScreen.Grades.route,
        Icons.Default.CheckCircle,
        Icons.Outlined.CheckCircle,
        "Calificación"
    )
    object Settings : BottomNavItem(
        AppScreen.Settings.route,
        Icons.Default.Settings,
        Icons.Outlined.Settings,
        "Ajustes"
    )
}

@Composable
fun MainScreen(mainNavController: NavHostController) {
    val bottomNavController = rememberNavController()
    val navItems = listOf(BottomNavItem.Learn, BottomNavItem.Test, BottomNavItem.Grades, BottomNavItem.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navItems.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.filledIcon else screen.outlinedIcon,
                                contentDescription = screen.label
                            )
                        },
                        label = { Text(screen.label) },
                        selected = isSelected,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // NavHost para las pantallas del menú inferior
        NavHost(
            navController = bottomNavController,
            startDestination = AppScreen.SubjectList.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppScreen.SubjectList.route) {
                SubjectListScreen(navController = mainNavController)
            }
            composable(AppScreen.Test.route) {
                PantallaTest(navController = mainNavController)
            }
            composable(AppScreen.Grades.route) {
                PantallaCalificacion(navController = mainNavController)
            }
            composable(AppScreen.Settings.route) {
                SettingsScreen(navController = mainNavController)
            }
        }
    }
}