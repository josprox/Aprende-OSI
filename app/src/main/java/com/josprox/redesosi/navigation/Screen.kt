package com.josprox.redesosi.navigation

sealed class AppScreen(val route: String) {
    // Rutas para la navegación principal y el menú inferior
    object Home : AppScreen("home")
    object SubjectList : AppScreen("subjects") // Antes era Aprende
    object Test : AppScreen("test")
    object Grades : AppScreen("grades")
    object Settings : AppScreen("settings")

    // Rutas para la navegación de contenido
    object ModuleList : AppScreen("modules/{subjectId}") {
        fun createRoute(subjectId: Int) = "modules/$subjectId"
    }

    object ModuleDetail : AppScreen("module_detail/{moduleId}") {
        fun createRoute(moduleId: Int) = "module_detail/$moduleId"
    }

    object Quiz : AppScreen("quiz/{moduleId}?attemptId={attemptId}") {

        /**
         * Para empezar un test NUEVO desde la lista de módulos.
         */
        fun createRoute(moduleId: Int) = "quiz/$moduleId?attemptId=0" // 0L indica test nuevo

        /**
         * Para RESUMIR un test desde la pantalla de "Test" (pendientes).
         */
        fun resumeRoute(moduleId: Int, attemptId: Long) = "quiz/$moduleId?attemptId=$attemptId"
    }
    object TestReview : AppScreen("test_review/{attemptId}") {
        fun createRoute(attemptId: Long) = "test_review/$attemptId"
    }
    object BackupRestore : AppScreen("backup_restore")

    object LegalInfo : AppScreen("legal_info")
    object Chat : AppScreen("chat_screen/{moduleId}") { // 1. Define el argumento en la ruta base
        fun createRoute(moduleId: Int) = "chat_screen/$moduleId" // 2. Crea la función constructora
    }
}

