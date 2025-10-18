package com.josprox.redesosi.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

/**
 * Este es el NavHost principal de la app.
 * Lo llamarás desde MainActivity.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppScreen.Home.route,
        modifier = modifier
    ) {
        // Ruta 1: La pantalla "Home"
        composable(route = AppScreen.Home.route) {
            AppNavegacionPrincipal(navController = navController)
        }
    }
}