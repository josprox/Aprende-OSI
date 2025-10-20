package com.josprox.redesosi.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josprox.redesosi.data.database.TestAttemptWithModule
import com.josprox.redesosi.data.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map // <-- IMPORTANTE: AÑADE ESTE IMPORT
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// Estado de la UI
data class CalificacionUiState(
    val completedTests: List<TestAttemptWithModule> = emptyList()
)

@HiltViewModel
class CalificacionViewModel @Inject constructor(
    studyRepository: StudyRepository
) : ViewModel() {

    // --- CÓDIGO CORREGIDO Y SIMPLIFICADO ---
    // 1. Obtenemos el Flow de tests completados del repositorio.
    // 2. Usamos .map { ... } para transformar la List<TestAttemptWithModule> en un CalificacionUiState.
    // 3. Usamos .stateIn { ... } para convertir el Flow en un StateFlow que la UI pueda consumir.
    val uiState: StateFlow<CalificacionUiState> =
        studyRepository.getCompletedTests()
            .map { tests ->
                CalificacionUiState(completedTests = tests)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = CalificacionUiState() // El valor inicial es un UiState vacío
            )
}