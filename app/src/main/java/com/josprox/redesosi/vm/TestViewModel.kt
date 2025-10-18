package com.josprox.redesosi.vm

import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TestUiState(
    val titulo: String = "Verifica tus conocimientos",
    val botonTexto: String = "Empezar Skills Test"
)

class TestViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TestUiState())
    val uiState = _uiState.asStateFlow()

    // La función de evento ahora acepta el NavController
    fun onEmpezarTestClicked(navController: NavHostController) {
        println("Empezar test clickeado")
    }
}