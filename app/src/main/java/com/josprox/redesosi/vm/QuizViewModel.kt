package com.josprox.redesosi.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josprox.redesosi.data.database.QuestionEntity
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

    private val moduleId: Int = checkNotNull(savedStateHandle["moduleId"])

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val questions = repository.getOrCreateQuestionsForModule(moduleId)
            _uiState.update {
                it.copy(
                    questions = questions.shuffled(), // Las mostramos en orden aleatorio
                    isLoading = false
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

        // Mover a la siguiente pregunta o finalizar
        if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = it.currentQuestionIndex + 1,
                    selectedAnswer = null,
                    score = newScore
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isQuizFinished = true,
                    score = newScore
                )
            }
        }
    }
}
