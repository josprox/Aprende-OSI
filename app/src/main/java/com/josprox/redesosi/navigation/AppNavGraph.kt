package com.josprox.redesosi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.josprox.redesosi.ui.screens.*

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController // Ahora recibe el NavController
) {
    NavHost(
        navController = navController,
        startDestination = AppScreen.Home.route, // 1. La app empieza en la pantalla principal con el menú
        modifier = modifier
    ) {
        // 2. La ruta "home" carga la MainScreen, que contiene el menú inferior
        composable(route = AppScreen.Home.route) {
            MainScreen(mainNavController = navController)
        }

        // --- El resto de las pantallas se mantienen para la navegación ---

        composable(
            route = AppScreen.ModuleList.route,
            arguments = listOf(navArgument("subjectId") { type = NavType.IntType })
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: 0
            ModuleListScreen(navController = navController, subjectId = subjectId)
        }

        composable(
            route = AppScreen.ModuleDetail.route,
            arguments = listOf(navArgument("moduleId") { type = NavType.IntType })
        ) { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getInt("moduleId") ?: 0
            ModuleDetailScreen(navController = navController, moduleId = moduleId)
        }

        composable(
            route = AppScreen.Quiz.route,
            arguments = listOf(navArgument("moduleId") { type = NavType.IntType })
        ) { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getInt("moduleId") ?: 0
            QuizScreen(navController = navController, moduleId = moduleId)
        }
    }
}

