package com.josprox.redesosi.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.List
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
import com.josprox.redesosi.navigation.AppScreen

// Se definen los items del menú inferior
private sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Learn : BottomNavItem(AppScreen.SubjectList.route, Icons.Default.List, "Aprende")
    object Test : BottomNavItem(AppScreen.Test.route, Icons.Default.Create, "Test")
    object Grades : BottomNavItem(AppScreen.Grades.route, Icons.Default.CheckCircle, "Calificación")
}

@Composable
fun MainScreen(mainNavController: NavHostController) {
    val bottomNavController = rememberNavController()
    val navItems = listOf(BottomNavItem.Learn, BottomNavItem.Test, BottomNavItem.Grades)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
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
                // Pasamos el NavController principal para poder navegar a otras pantallas
                SubjectListScreen(navController = mainNavController)
            }
            composable(AppScreen.Test.route) {
                PantallaTest(navController = mainNavController)
            }
            composable(AppScreen.Grades.route) {
                PantallaCalificacion(navController = mainNavController)
            }
        }
    }
}

