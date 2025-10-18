package com.josprox.redesosi.vm

import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CalificacionUiState(
    val titulo: String = "Verifica tu calificación",
    val botonTexto: String = "Ver Calificación"
)

class CalificacionViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CalificacionUiState())
    val uiState = _uiState.asStateFlow()

    fun onVerCalificacionClicked(navController: NavHostController) {
        println("Ver calificación clickeado")
    }
}