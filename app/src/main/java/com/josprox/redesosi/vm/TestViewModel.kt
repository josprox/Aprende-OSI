package com.josprox.redesosi.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.josprox.redesosi.data.database.TestAttemptWithModule
import com.josprox.redesosi.data.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class TestUiState(
    val pendingTests: List<TestAttemptWithModule> = emptyList(),
    val completedTests: List<TestAttemptWithModule> = emptyList()
)

@HiltViewModel
class TestViewModel @Inject constructor(
    studyRepository: StudyRepository
) : ViewModel() {

    // Combinamos ambos flujos (pendientes y completados) en un solo UiState
    val uiState: StateFlow<TestUiState> = combine(
        studyRepository.getPendingTests(),
        studyRepository.getCompletedTests()
    ) { pending, completed ->
        TestUiState(
            pendingTests = pending,
            completedTests = completed
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TestUiState() // Estado inicial vacío
    )

    // Lógica para cuando el usuario hace clic en un test
    fun onTestClicked(navController: NavHostController, testAttemptId: Long, status: String) {
        if (status == "PENDING") {
            // Navega al QuizScreen para resumir el test (necesitas esta ruta)
            // Debes pasar el ID del intento
            navController.navigate("quiz/$testAttemptId/resume") // RUTA DE EJEMPLO
        } else {
            // Navega a una nueva pantalla de revisión (necesitas esta ruta)
            navController.navigate("quiz_review/$testAttemptId") // RUTA DE EJEMPLO
        }
    }
}