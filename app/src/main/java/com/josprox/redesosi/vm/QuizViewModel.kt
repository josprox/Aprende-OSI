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
    val isLoading: Boolean = true,
    // --- NUEVOS CAMPOS DE FEEDBACK Y PROGRESO ---
    val isAnswerSubmitted: Boolean = false, // Indica si el usuario ya presionó 'Guardar y Continuar'
    val feedbackMessage: String? = null, // Mensaje de retroalimentación (Correcto/Incorrecto)
    val correctOptionKey: String? = null, // Guarda la respuesta correcta para resaltar
    val answeredQuestions: Set<Int> = emptySet() // Para saber qué preguntas ya se contestaron/saltaron
)


@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: StudyRepository,
    savedStateHandle: SavedStateHandle

) : ViewModel() {

    // Leemos ambos IDs desde el SavedStateHandle (gracias a la navegación)
    private val moduleId: Int = checkNotNull(savedStateHandle["moduleId"])
    private val attemptId: Long = checkNotNull(savedStateHandle["attemptId"])
    // Será 0L si es un test nuevo

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

            // 3. Recalcular el puntaje, índice y preguntas ya respondidas
            val score = savedAnswers.count{ it.isCorrect }
            val currentIndex = currentAttempt!!.currentQuestionIndex
            val answeredQIds = savedAnswers.map { it.questionId }.toSet()

            // Mapeamos los ID de preguntas a su índice dentro de la lista de questions
            val answeredIndices = questions.mapIndexedNotNull { index, questionEntity ->
                if (answeredQIds.contains(questionEntity.id)) index else null
            }.toSet()


            _uiState.update {
                it.copy(
                    questions = questions, // Mismo orden fijo
                    currentQuestionIndex = currentIndex,
                    score = score,
                    isLoading = false,
                    selectedAnswer = null,
                    isAnswerSubmitted = false,
                    feedbackMessage = null,
                    correctOptionKey = null,
                    answeredQuestions = answeredIndices // Restauramos el conjunto de índices
                )
            }
        }
    }

    fun onAnswerSelected(answer: String) {
        // Solo permite seleccionar si la respuesta no ha sido enviada aún
        if (!_uiState.value.isAnswerSubmitted) {
            _uiState.update { it.copy(selectedAnswer = answer) }
        }
    }

    /**
     * Guarda la respuesta, muestra el feedback y la respuesta correcta.
     */
    fun onSaveAndContinueClicked() {
        val currentState = _uiState.value
        if (currentState.selectedAnswer == null || currentState.isAnswerSubmitted) return

        val currentQuestion = currentState.questions[currentState.currentQuestionIndex]
        val selectedAnswerKey = currentState.selectedAnswer!!

        val isCorrect = selectedAnswerKey == currentQuestion.correctAnswer
        val statusText = if (isCorrect) "Correcto" else "Incorrecto"

        // ** <--- CAMBIO CLAVE: USAR LA EXPLICACIÓN GENERADA POR LA IA ---> **
        // Usamos la explicación de la QuestionEntity. Si la respuesta es incorrecta,
        // aun así mostramos la explicación de la respuesta correcta.
        val explanationFromIA = currentQuestion.explanationText

        // Creamos la leyenda que se muestra y se guarda
        val feedback = "$statusText: $explanationFromIA"

        val newScore = if (isCorrect) currentState.score + 1 else currentState.score

        // 1. Guardar la respuesta en la BD (con la explicación/leyenda)
        // Usamos el 'feedback' completo (status + explicación de IA)
        saveAnswer(currentQuestion, selectedAnswerKey, isCorrect, feedback)


        // 2. Actualizar el estado para mostrar el feedback
        _uiState.update {
            it.copy(
                score = newScore, // Sumamos punto
                isAnswerSubmitted = true,
                feedbackMessage = feedback, // Muestra el mensaje detallado
                correctOptionKey = currentQuestion.correctAnswer,
                // Marcamos la pregunta como respondida
                answeredQuestions = it.answeredQuestions + it.currentQuestionIndex
            )
        }
    }

    /**
     * Pasa a la siguiente pregunta sin guardar respuesta.
     */
    fun onSkipClicked() {
        val currentState = _uiState.value
        // No se puede saltar si ya se envió la respuesta
        if (currentState.isAnswerSubmitted) return

        // Marcamos como "saltada" (respondida con null, lógicamente)
        // para que no vuelva y para que el botón avance a la siguiente
        _uiState.update {
            it.copy(
                isAnswerSubmitted = false,
                selectedAnswer = null,
                feedbackMessage = null,
                correctOptionKey = null,
                answeredQuestions = it.answeredQuestions + it.currentQuestionIndex // ¡Importante para no volver!
            )
        }

        // Usamos la lógica de onNextPage para avanzar
        onNextPage()
    }

    /**
     * Mueve a la siguiente pregunta o finaliza el test.
     */
    fun onNextPage() {
        val currentState = _uiState.value
        val nextIndex = currentState.currentQuestionIndex + 1
        val isLastQuestion = nextIndex >= currentState.questions.size

        if (isLastQuestion) {
            // El examen ha terminado
            _uiState.update {
                it.copy(
                    isQuizFinished = true,
                    isAnswerSubmitted = false
                )
            }
            finalizeAttempt()
        } else {
            // Mover a la siguiente pregunta
            _uiState.update {
                it.copy(
                    currentQuestionIndex = nextIndex,
                    selectedAnswer = null,
                    isAnswerSubmitted = false, // Limpiamos el estado de envío
                    feedbackMessage = null,
                    correctOptionKey = null
                )
            }
            // Guardar el progreso (índice)
            updateAttemptProgress(nextIndex, currentState.score)
        }
    }

    /**
     * Función que reemplaza la lógica del viejo onNextClicked.
     * Ahora solo sirve para avanzar DESPUÉS de ver la retroalimentación.
     */
    fun onNextClicked() {
        // Solo avanza si la respuesta fue enviada (isAnswerSubmitted = true)
        if (_uiState.value.isAnswerSubmitted) {
            onNextPage()
        }
    }


    /**
     * Lógica unificada para guardar la respuesta en la BD.
     */
    private fun saveAnswer(
        question: QuestionEntity,
        selectedOption: String,
        isCorrect: Boolean,
        feedback: String? // Ahora guardamos la explicación/leyenda aquí
    ) {
        val attemptId = currentAttempt?.id ?: return
        val answerEntity = UserAnswerEntity(
            testAttemptId = attemptId,
            questionId = question.id,
            selectedOption = selectedOption,
            isCorrect = isCorrect,
            explanationText = feedback // Guardamos la leyenda
        )
        viewModelScope.launch {
            repository.saveUserAnswer(answerEntity)
        }
    }

    /**
     * Lógica unificada para actualizar el progreso.
     */
    private fun updateAttemptProgress(newIndex: Int, newScore: Int) {
        viewModelScope.launch{
            currentAttempt?.let{
                val updatedAttempt = it.copy(
                    correctAnswers = newScore,
                    currentQuestionIndex = newIndex,
                    timestamp = System.currentTimeMillis()
                )
                currentAttempt = updatedAttempt
                repository.updateTestAttempt(updatedAttempt)
            }
        }
    }

    /**
     * Lógica unificada para finalizar el intento.
     */
    private fun finalizeAttempt() {
        viewModelScope.launch{
            currentAttempt?.let{ attempt ->
                val currentState = _uiState.value
                // Verificamos si hay preguntas (evitar división por cero)
                val totalQ = currentState.questions.size.toDouble().takeIf { it > 0 } ?: 1.0
                val finalScore = (currentState.score.toDouble() / totalQ) * 10.0
                val finalAttempt = attempt.copy(
                    status = "COMPLETED",
                    score = finalScore,
                    correctAnswers = currentState.score,
                    currentQuestionIndex = currentState.questions.size,
                    timestamp = System.currentTimeMillis()
                )
                repository.updateTestAttempt(finalAttempt)
            }
        }
    }
}