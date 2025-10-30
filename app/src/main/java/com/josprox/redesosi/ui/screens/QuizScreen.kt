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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.josprox.redesosi.navigation.AppScreen // Importar AppScreen
import com.josprox.redesosi.vm.QuizViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    navController: NavController,
    moduleId: Int,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // --- Cálculo del progreso (Verificación de corrección) ---
    val progress = if (uiState.questions.isNotEmpty()
        && !uiState.isQuizFinished) {
        (uiState.currentQuestionIndex.toFloat() + 1f)
            .coerceAtMost(uiState.questions.size.toFloat()) / uiState.questions.size.toFloat()
    } else if (uiState.isQuizFinished) {
        1f
    } else {
        0f
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test de Conocimientos", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
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
            } else if (uiState.questions.isEmpty())
            {
                Text("No se pudieron generar las preguntas. Inténtalo de nuevo.")
            } else if (uiState.isQuizFinished)
            {
                // --- CAMBIO CLAVE 1: Pasamos el attemptId y navController ---
                QuizResult(
                    score = uiState.score,
                    totalQuestions = uiState.questions.size,
                    attemptId = viewModel.currentAttemptId, // Propiedad necesaria en QuizViewModel
                    navController = navController, // Necesitamos pasar el NavController
                    onFinish = { navController.popBackStack()}
                )
            } else {
                val currentQuestion =
                    uiState.questions[uiState.currentQuestionIndex]
                val isAnsweredOrSkipped = uiState.answeredQuestions.contains(uiState.currentQuestionIndex)

                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {

                    // --- Barra de Progreso ---
                    LinearProgressIndicator(
                        progress = { progress},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    // --- Contenido con scroll ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Pregunta ${uiState.currentQuestionIndex + 1}/${uiState.questions.size}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Text(
                            text = currentQuestion.questionText,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(8.dp))

                        val options = listOf(
                            "A" to currentQuestion.optionA,
                            "B" to currentQuestion.optionB,
                            "C" to currentQuestion.optionC,
                            "D" to currentQuestion.optionD
                        )

                        options.forEach{ (key, text) ->
                            AnswerOption(
                                text = text,
                                optionKey = key,
                                isSelected = uiState.selectedAnswer == key,
                                isAnswerSubmitted = uiState.isAnswerSubmitted,
                                correctAnswerKey = uiState.correctOptionKey,
                                onSelected = { viewModel.onAnswerSelected(key) }
                            )
                        }

                        // --- Feedback/Leyenda ---
                        if (uiState.isAnswerSubmitted && uiState.feedbackMessage != null) {
                            val isCorrect = uiState.selectedAnswer == uiState.correctOptionKey

                            val color = if (isCorrect) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onErrorContainer
                            val containerColor = if (isCorrect) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer

                            Card(
                                colors = CardDefaults.cardColors(containerColor = containerColor),
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Text(
                                    text = uiState.feedbackMessage!!,
                                    color = color,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }

                    // --- Botones Inferiores (DINÁMICO) ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón Saltar/Avanzar
                        if (!uiState.isAnswerSubmitted && !isAnsweredOrSkipped) {
                            OutlinedButton(
                                onClick = { viewModel.onSkipClicked() },
                                enabled = true
                            ) {
                                Text("Saltar Pregunta")
                            }
                        }

                        if (uiState.isAnswerSubmitted || isAnsweredOrSkipped) {
                            Spacer(Modifier.weight(1f))
                        }

                        // Botón de Acción Principal (Destacado)
                        Button(
                            onClick = {
                                if (uiState.isAnswerSubmitted) {
                                    viewModel.onNextClicked()
                                } else {
                                    viewModel.onSaveAndContinueClicked()
                                }
                            },
                            enabled = uiState.isAnswerSubmitted || uiState.selectedAnswer != null,
                            modifier = Modifier.weight(1f, fill = false),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isAnswerSubmitted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Text(
                                if (uiState.isAnswerSubmitted) {
                                    if (uiState.currentQuestionIndex < uiState.questions.size - 1) "Siguiente Pregunta" else "Finalizar Test"
                                } else {
                                    "Guardar y Continuar"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


// --- Componente de Opción de Respuesta (Se mantiene igual) ---
@Composable
fun AnswerOption(
    text: String,
    optionKey: String,
    isSelected: Boolean,
    isAnswerSubmitted: Boolean,
    correctAnswerKey: String?,
    onSelected: () -> Unit
) {
    val isCorrect = isAnswerSubmitted && optionKey == correctAnswerKey
    val isIncorrect = isAnswerSubmitted && isSelected && optionKey != correctAnswerKey

    val containerColor = when {
        isCorrect -> MaterialTheme.colorScheme.tertiaryContainer
        isIncorrect -> MaterialTheme.colorScheme.errorContainer
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        isCorrect -> MaterialTheme.colorScheme.onTertiaryContainer
        isIncorrect -> MaterialTheme.colorScheme.onErrorContainer
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    val cardColors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor)

    val border = if (isCorrect || isIncorrect || isSelected) null else CardDefaults.outlinedCardBorder()
    val elevation = if (isCorrect || isIncorrect) CardDefaults.cardElevation(4.dp) else CardDefaults.cardElevation(1.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                enabled = !isAnswerSubmitted,
                onClick = onSelected
            ),
        colors = cardColors,
        border = border,
        shape = MaterialTheme.shapes.large,
        elevation = elevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null,
                enabled = !isAnswerSubmitted,
                colors = RadioButtonDefaults.colors(
                    selectedColor = contentColor,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isCorrect || isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}


// --- Componente de Resultado Final (MODIFICADO) ---
@Composable
fun QuizResult(
    score: Int,
    totalQuestions: Int,
    attemptId: Long?, // <-- AÑADIDO: ID del intento
    navController: NavController, // <-- AÑADIDO: NavController
    onFinish: () -> Unit
) {
    val scoreRatio = score.toDouble() / totalQuestions
    val isApproved = scoreRatio >= 0.8

    val color = if (isApproved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.padding(32.dp)
    ) {
        Text(
            "¡Test Finalizado!",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Puntuación:",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                "$score / $totalQuestions",
                style = MaterialTheme.typography.displayLarge,
                color = color,
                fontWeight = FontWeight.Black
            )
        }

        Text(
            if (isApproved) "¡Excelente trabajo! Has demostrado dominio del tema." else "Sigue estudiando. Revisa tus errores para mejorar.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center // Corregido: Usamos TextAlign.Center
        )

        // --- FILA DE BOTONES: Ver Examen y Volver al Módulo ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            // 1. Botón para VER EL EXAMEN (OutlinedButton)
            if (attemptId != null && attemptId != 0L) {
                OutlinedButton(
                    onClick = {
                        navController.navigate(AppScreen.TestReview.createRoute(attemptId))
                    },
                ) {
                    Text("Ver el examen")
                }
                Spacer(Modifier.width(16.dp))
            }

            // 2. Botón para VOLVER AL MÓDULO (Filled Button)
            Button(
                onClick = onFinish,
            ) {
                Text("Volver al Módulo")
            }
        }
    }
}