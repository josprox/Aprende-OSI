package com.josprox.redesosi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.josprox.redesosi.vm.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    navController: NavController,
    moduleId: Int,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Test de Conocimientos") }) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
                Text("Generando preguntas con IA...", modifier = Modifier.padding(top = 70.dp))
            } else if (uiState.questions.isEmpty()) {
                Text("No se pudieron generar las preguntas. Inténtalo de nuevo.")
            } else if (uiState.isQuizFinished) {
                QuizResult(
                    score = uiState.score,
                    totalQuestions = uiState.questions.size,
                    onFinish = { navController.popBackStack() }
                )
            } else {
                val currentQuestion = uiState.questions[uiState.currentQuestionIndex]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Pregunta ${uiState.currentQuestionIndex + 1}/${uiState.questions.size}",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(text = currentQuestion.questionText, style = MaterialTheme.typography.headlineSmall)

                    // Ahora la lista incluye las 4 opciones
                    val options = listOf(
                        "A" to currentQuestion.optionA,
                        "B" to currentQuestion.optionB,
                        "C" to currentQuestion.optionC,
                        "D" to currentQuestion.optionD
                    )
                    // ---------------------------

                    // Este 'forEach' ahora iterará 4 veces
                    options.forEach { (key, text) ->
                        AnswerOption(
                            text = text,
                            isSelected = uiState.selectedAnswer == key,
                            onSelected = { viewModel.onAnswerSelected(key) }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { viewModel.onNextClicked() },
                        enabled = uiState.selectedAnswer != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (uiState.currentQuestionIndex < uiState.questions.size - 1) "Siguiente" else "Finalizar")
                    }
                }
            }
        }
    }
}
@Composable
fun AnswerOption(text: String, isSelected: Boolean, onSelected: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelected
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = text)
        }
    }
}

@Composable
fun QuizResult(score: Int, totalQuestions: Int, onFinish: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("¡Test Finalizado!", style = MaterialTheme.typography.displaySmall)
        Text("Tu puntuación:", style = MaterialTheme.typography.headlineMedium)
        Text("$score / $totalQuestions", style = MaterialTheme.typography.headlineLarge)
        Button(onClick = onFinish) {
            Text("Volver al Módulo")
        }
    }
}
