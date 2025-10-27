package com.josprox.redesosi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.josprox.redesosi.ui.screens.*
import com.josprox.redesosi.ui.screens.settings.BackupRestoreScreen
import com.josprox.redesosi.ui.screens.settings.LegalInfoScreen

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = AppScreen.Home.route,
        modifier = modifier
    ) {
        // 2. La ruta "home" carga la MainScreen, que contiene el menú inferior
        composable(route = AppScreen.Home.route) {
            MainScreen(mainNavController = navController)
        }

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
            arguments = listOf(
                navArgument("moduleId") { type = NavType.IntType },
                navArgument("attemptId") {
                    type = NavType.LongType
                    defaultValue = 0L // 0L se usará para indicar "Test Nuevo"
                }
            )
        ) { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getInt("moduleId") ?: 0
            QuizScreen(
                navController = navController,
                moduleId = moduleId
            )
        }

        composable(
            route = AppScreen.TestReview.route,
            arguments = listOf(navArgument("attemptId") { type = NavType.LongType })
        ) { backStackEntry ->
            val attemptId = backStackEntry.arguments?.getLong("attemptId") ?: 0L
            TestReviewScreen(navController = navController, attemptId = attemptId)
        }

        composable(
            route = AppScreen.Chat.route,
            arguments = listOf(navArgument("moduleId") { type = NavType.IntType }) // <-- 1. Define el argumento
        ) { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getInt("moduleId") ?: 0 // <-- 2. Recupera el argumento
            ChatScreen(navController = navController, moduleId = moduleId) // <-- 3. Pásalo a la pantalla
        }

        composable(route = AppScreen.BackupRestore.route) {
            BackupRestoreScreen(navController = navController)
        }
        composable(route = AppScreen.LegalInfo.route) {
            LegalInfoScreen(navController = navController)
        }
    }
}