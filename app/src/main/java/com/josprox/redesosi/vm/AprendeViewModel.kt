// package com.josprox.redesosi.ui.aprende
package com.josprox.redesosi.vm

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.navigation.NavHostController

data class AprendeUiState(
    val titulo: String = "Sistemas de trabajo con conocimientos",
    val tarjetaTitulo: String = "Aprende del tema",
    val tarjetaDescripcion: String = "Explora los conceptos fundamentales del Modelo OSI para entender cómo se comunican las redes.",
    val botonTexto: String = "Ver Modelo OSI"
)

// 2. El ViewModel
class AprendeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AprendeUiState())
    val uiState = _uiState.asStateFlow()
    fun onBotonOsiClicked(navController: NavHostController) {
        println("Botón OSI clickeado. Lógica de negocio aquí.")
    }
}