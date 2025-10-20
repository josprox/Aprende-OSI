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

    // --- REEMPLAZA TU OBJECT QUIZ CON ESTE ---
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
}

