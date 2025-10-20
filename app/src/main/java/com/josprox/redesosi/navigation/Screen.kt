package com.josprox.redesosi.navigation

sealed class AppScreen(val route: String) {
    // Rutas para la navegación principal y el menú inferior
    object Home : AppScreen("home")
    object SubjectList : AppScreen("subjects") // Antes era Aprende
    object Test : AppScreen("test")
    object Grades : AppScreen("grades")

    // Rutas para la navegación de contenido
    object ModuleList : AppScreen("modules/{subjectId}") {
        fun createRoute(subjectId: Int) = "modules/$subjectId"
    }

    object ModuleDetail : AppScreen("module_detail/{moduleId}") {
        fun createRoute(moduleId: Int) = "module_detail/$moduleId"
    }

    object Quiz : AppScreen("quiz/{moduleId}") {
        fun createRoute(moduleId: Int) = "quiz/$moduleId"
    }
}

