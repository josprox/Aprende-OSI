package com.josprox.redesosi.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.School
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Clase sellada (sealed class) que define las rutas principales de la app.
 * Estas son las pantallas "grandes" que usará el NavHost principal.
 */
sealed class AppScreen(val route: String) {
    object Home : AppScreen("home")
}

/**
 * Clase sellada para los items de la barra de navegación.
 */
sealed class TabScreen(
    val route: String, // Esta ruta es solo un ID para el Pager
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    // Objeto para la pantalla "Aprende"
    object Aprende : TabScreen("aprende", "Aprende", Icons.Filled.School, Icons.Outlined.School)

    // Objeto para la pantalla "Test"
    object Test : TabScreen("test", "Test", Icons.Filled.Quiz, Icons.Outlined.Quiz)

    // Objeto para la pantalla "Calificación"
    object Calificacion : TabScreen("calificacion", "Calificación", Icons.Filled.Grade, Icons.Outlined.Grade)
}

/**
 * Esta es la lista que usa tu BottomNavBar
 */
val bottomNavigationItems = listOf(
    TabScreen.Aprende,
    TabScreen.Test,
    TabScreen.Calificacion
)