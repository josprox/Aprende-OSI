package com.josprox.redesosi.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josprox.redesosi.data.database.ModuleEntity
import com.josprox.redesosi.data.database.QuestionEntity
import com.josprox.redesosi.data.database.TestAttemptEntity
import com.josprox.redesosi.data.database.UserAnswerEntity
import com.josprox.redesosi.data.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Esta clase combina una pregunta con la respuesta que dio el usuario.
 * Es más fácil para la UI.
 */
data class ReviewedQuestion(
    val question: QuestionEntity,
    val userAnswer: UserAnswerEntity
)

data class TestReviewUiState(
    val isLoading: Boolean = true,
    val attempt: TestAttemptEntity? = null,
    val module: ModuleEntity? = null,
    val reviewedQuestions: List<ReviewedQuestion> = emptyList()
)

@HiltViewModel
class TestReviewViewModel @Inject constructor(
    private val repository: StudyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val attemptId: Long = checkNotNull(savedStateHandle["attemptId"])

    private val _uiState = MutableStateFlow(TestReviewUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadReviewData()
    }

    private fun loadReviewData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. Cargar el intento
            val attempt = repository.getTestAttemptById(attemptId)
            if (attempt == null) {
                _uiState.update { it.copy(isLoading = false) } // Error, no se encontró
                return@launch
            }

            // 2. Cargar el módulo (para el título)
            val module = repository.getModuleById(attempt.moduleId)

            // 3. Cargar las preguntas y las respuestas
            val questions = repository.getOriginalQuestionsForModule(attempt.moduleId)
            val answers = repository.getUserAnswersForAttempt(attemptId)

            // 4. Mapear las respuestas a las preguntas para unirlas fácilmente
            val answersMap = answers.associateBy { it.questionId }

            // 5. Crear la lista de "preguntas revisadas"
            val reviewedQuestions = questions.mapNotNull { question ->
                answersMap[question.id]?.let { answer ->
                    ReviewedQuestion(question = question, userAnswer = answer)
                }
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    attempt = attempt,
                    module = module,
                    reviewedQuestions = reviewedQuestions
                )
            }
        }
    }
}