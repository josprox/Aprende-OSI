package com.josprox.redesosi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
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

    // --- Cálculo del progreso ---
    val progress = if (uiState.questions.isNotEmpty() && !uiState.isQuizFinished) {
        (uiState.currentQuestionIndex.toFloat()) / uiState.questions.size.toFloat()
    } else if (uiState.isQuizFinished) {
        1f // Completo al finalizar
    } else {
        0f // Cargando
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Test de Conocimientos") }) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
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
                ) {

                    // --- Barra de Progreso ---
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp) // Añadimos padding
                    )

                    // --- Esta Columna INTERNA es la que se puede scrollear ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // <-- 1. Ocupa el espacio disponible
                            .verticalScroll(rememberScrollState()) // <-- PERMITE EL SCROLL
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Pregunta ${uiState.currentQuestionIndex + 1}/${uiState.questions.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // <-- Este texto ahora se puede scrollear si es largo
                        Text(text = currentQuestion.questionText, style = MaterialTheme.typography.headlineSmall)

                        Spacer(Modifier.height(8.dp)) // <-- AÑADIDO

                        val options = listOf(
                            "A" to currentQuestion.optionA,
                            "B" to currentQuestion.optionB,
                            "C" to currentQuestion.optionC,
                            "D" to currentQuestion.optionD
                        )

                        options.forEach { (key, text) ->
                            AnswerOption(
                                text = text,
                                isSelected = uiState.selectedAnswer == key,
                                onSelected = { viewModel.onAnswerSelected(key) }
                            )
                        }
                    }

                    // <-- El botón ahora es hijo de la Columna EXTERNA
                    Button(
                        onClick = { viewModel.onNextClicked() },
                        enabled = uiState.selectedAnswer != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
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
    // --- Usamos OutlinedCard para no seleccionada y FilledCard para seleccionada ---
    val cardColors = if (isSelected) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    } else {
        CardDefaults.outlinedCardColors()
    }

    val border = if (isSelected) null else CardDefaults.outlinedCardBorder()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelected
            ),
        colors = cardColors,
        border = border,
        shape = MaterialTheme.shapes.medium // <-- Bordes M3
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
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