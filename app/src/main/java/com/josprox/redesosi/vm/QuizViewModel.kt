package com.josprox.redesosi.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josprox.redesosi.data.database.QuestionEntity
import com.josprox.redesosi.data.database.TestAttemptEntity
import com.josprox.redesosi.data.database.UserAnswerEntity
import com.josprox.redesosi.data.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizUiState(
    val questions: List<QuestionEntity> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswer: String? = null,
    val score: Int = 0,
    val isQuizFinished: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: StudyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Leemos ambos IDs desde el SavedStateHandle (gracias a la navegación)
    private val moduleId: Int = checkNotNull(savedStateHandle["moduleId"])
    private val attemptId: Long = checkNotNull(savedStateHandle["attemptId"]) // Será 0L si es un test nuevo

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    // Guardamos una referencia al intento actual
    private var currentAttempt: TestAttemptEntity? = null

    init {
        // Decidimos si cargar un test nuevo o resumir uno existente
        if (attemptId == 0L) {
            loadNewTest()
        } else {
            loadAndResumeTest(attemptId)
        }
    }

    /**
     * Carga las preguntas y crea un NUEVO intento de examen.
     */
    private fun loadNewTest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val questions = repository.getOrCreateQuestionsForModule(moduleId)

            if (questions.isNotEmpty()) {
                val newAttemptId = repository.createTestAttempt(
                    moduleId = moduleId,
                    totalQuestions = questions.size
                )
                currentAttempt = TestAttemptEntity(
                    id = newAttemptId,
                    moduleId = moduleId,
                    status = "PENDING",
                    totalQuestions = questions.size,
                    currentQuestionIndex = 0
                )
            }

            _uiState.update {
                it.copy(
                    // NO usamos .shuffled() - el orden debe ser fijo
                    questions = questions,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Carga un intento PENDIENTE y restaura el estado.
     */
    private fun loadAndResumeTest(attemptId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. Cargar el intento y las preguntas
            currentAttempt = repository.getTestAttemptById(attemptId)
            val questions = repository.getOrCreateQuestionsForModule(moduleId)

            // 2. Cargar las respuestas ya guardadas
            val savedAnswers = repository.getUserAnswersForAttempt(attemptId)

            if (currentAttempt == null) {
                // Error, no se encontró el test, empezamos uno nuevo
                loadNewTest()
                return@launch
            }

            // 3. Recalcular el puntaje y el índice
            val score = savedAnswers.count { it.isCorrect }
            val currentIndex = currentAttempt!!.currentQuestionIndex

            _uiState.update {
                it.copy(
                    questions = questions, // Mismo orden fijo
                    currentQuestionIndex = currentIndex,
                    score = score,
                    isLoading = false,
                    selectedAnswer = null // Empezamos sin nada seleccionado
                )
            }
        }
    }

    fun onAnswerSelected(answer: String) {
        _uiState.update { it.copy(selectedAnswer = answer) }
    }

    fun onNextClicked() {
        val currentState = _uiState.value
        val currentQuestion = currentState.questions[currentState.currentQuestionIndex]

        // Calcular si la respuesta es correcta
        val isCorrect = currentState.selectedAnswer == currentQuestion.correctAnswer
        val newScore = if (isCorrect) currentState.score + 1 else currentState.score

        // Guardar la respuesta del usuario en la BD
        val attemptId = currentAttempt?.id ?: return
        val answerEntity = UserAnswerEntity(
            testAttemptId = attemptId,
            questionId = currentQuestion.id,
            selectedOption = currentState.selectedAnswer!!,
            isCorrect = isCorrect
        )
        viewModelScope.launch {
            repository.saveUserAnswer(answerEntity)
        }


        // Mover a la siguiente pregunta o finalizar
        if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
            val nextIndex = currentState.currentQuestionIndex + 1
            _uiState.update {
                it.copy(
                    currentQuestionIndex = nextIndex,
                    selectedAnswer = null,
                    score = newScore
                )
            }

            // --- AÑADIDO: Guardar el progreso (índice y puntaje) ---
            viewModelScope.launch {
                currentAttempt?.let {
                    // Actualizamos el intento a "PENDING" pero con el nuevo índice/puntaje
                    val updatedAttempt = it.copy(
                        correctAnswers = newScore,
                        currentQuestionIndex = nextIndex,
                        timestamp = System.currentTimeMillis() // Actualiza la hora
                    )
                    currentAttempt = updatedAttempt // Actualiza la copia local
                    repository.updateTestAttempt(updatedAttempt)
                }
            }

        } else {
            // El examen ha terminado
            _uiState.update {
                it.copy(
                    isQuizFinished = true,
                    score = newScore
                )
            }

            // --- AÑADIDO: Finalizar el intento en la BD ---
            viewModelScope.launch {
                currentAttempt?.let { attempt ->
                    val finalScore = (newScore.toDouble() / currentState.questions.size) * 10.0
                    val finalAttempt = attempt.copy(
                        status = "COMPLETED",
                        score = finalScore,
                        correctAnswers = newScore,
                        currentQuestionIndex = currentState.questions.size, // Índice final
                        timestamp = System.currentTimeMillis()
                    )
                    repository.updateTestAttempt(finalAttempt)
                }
            }
        }
    }
}