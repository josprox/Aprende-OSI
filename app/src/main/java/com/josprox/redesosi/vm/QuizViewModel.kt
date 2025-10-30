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
    val feedbackMessage: String? = null, // Mensaje de retroalimentación (Correcto/Incorrecto + Explicación IA)
    val correctOptionKey: String? = null, // Guarda la respuesta correcta para resaltar
    val answeredQuestions: Set<Int> = emptySet() // Para saber qué preguntas ya se contestaron/saltaron
)


@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: StudyRepository,
    savedStateHandle: SavedStateHandle

) : ViewModel() {

    private val moduleId: Int = checkNotNull(savedStateHandle["moduleId"])
    private val attemptId: Long = checkNotNull(savedStateHandle["attemptId"])

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var currentAttempt: TestAttemptEntity? = null

    init {
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
            // Asegúrate de que Questions tiene el campo 'explanationText'
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

            currentAttempt = repository.getTestAttemptById(attemptId)
            val questions = repository.getOrCreateQuestionsForModule(moduleId)
            val savedAnswers = repository.getUserAnswersForAttempt(attemptId)

            if (currentAttempt == null) {
                loadNewTest()
                return@launch
            }

            val score = savedAnswers.count{ it.isCorrect }
            val currentIndex = currentAttempt!!.currentQuestionIndex
            val answeredQIds = savedAnswers.map { it.questionId }.toSet()

            val answeredIndices = questions.mapIndexedNotNull { index, questionEntity ->
                if (answeredQIds.contains(questionEntity.id)) index else null
            }.toSet()


            _uiState.update {
                it.copy(
                    questions = questions,
                    currentQuestionIndex = currentIndex,
                    score = score,
                    isLoading = false,
                    selectedAnswer = null,
                    isAnswerSubmitted = false,
                    feedbackMessage = null,
                    correctOptionKey = null,
                    answeredQuestions = answeredIndices
                )
            }
        }
    }

    fun onAnswerSelected(answer: String) {
        if (!_uiState.value.isAnswerSubmitted) {
            _uiState.update { it.copy(selectedAnswer = answer) }
        }
    }

    /**
     * Guarda la respuesta, muestra el feedback y la respuesta correcta (Guardar y Continuar).
     */
    fun onSaveAndContinueClicked() {
        val currentState = _uiState.value
        if (currentState.selectedAnswer == null || currentState.isAnswerSubmitted) return

        val currentQuestion = currentState.questions[currentState.currentQuestionIndex]
        val selectedAnswerKey = currentState.selectedAnswer!!

        val isCorrect = selectedAnswerKey == currentQuestion.correctAnswer
        val statusText = if (isCorrect) "Correcto" else "Incorrecto"

        // Usamos la explicación generada por la IA que debe estar en QuestionEntity
        val explanationFromIA = currentQuestion.explanationText

        // Creamos la leyenda completa que se muestra y se guarda
        val feedback = "$statusText: $explanationFromIA"

        val newScore = if (isCorrect) currentState.score + 1 else currentState.score

        // 1. Guardar la respuesta en la BD (con la explicación/leyenda)
        saveAnswer(currentQuestion, selectedAnswerKey, isCorrect, feedback)

        // 2. Actualizar el estado para mostrar el feedback
        _uiState.update {
            it.copy(
                score = newScore,
                isAnswerSubmitted = true,
                feedbackMessage = feedback,
                correctOptionKey = currentQuestion.correctAnswer,
                answeredQuestions = it.answeredQuestions + it.currentQuestionIndex
            )
        }
    }

    /**
     * Pasa a la siguiente pregunta sin guardar respuesta (Saltar).
     */
    fun onSkipClicked() {
        val currentState = _uiState.value
        if (currentState.isAnswerSubmitted) return

        // Marcamos la pregunta como saltada para no volver
        _uiState.update {
            it.copy(
                isAnswerSubmitted = false,
                selectedAnswer = null,
                feedbackMessage = null,
                correctOptionKey = null,
                answeredQuestions = it.answeredQuestions + it.currentQuestionIndex
            )
        }

        // Avanza a la siguiente página/pregunta
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
                    isAnswerSubmitted = false,
                    feedbackMessage = null,
                    correctOptionKey = null
                )
            }
            // Guardar el progreso (índice)
            updateAttemptProgress(nextIndex, currentState.score)
        }
    }

    /**
     * Función llamada por el botón "Siguiente Pregunta" después del feedback.
     */
    fun onNextClicked() {
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
        feedback: String? // Explicación guardada
    ) {
        val attemptId = currentAttempt?.id ?: return
        val answerEntity = UserAnswerEntity(
            testAttemptId = attemptId,
            questionId = question.id,
            selectedOption = selectedOption,
            isCorrect = isCorrect,
            explanationText = feedback // Guardamos la leyenda completa (incluyendo Correcto/Incorrecto)
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

    // Agrega esta propiedad pública para acceder al ID en QuizScreen.kt
    val currentAttemptId: Long?
        get() = currentAttempt?.id
}