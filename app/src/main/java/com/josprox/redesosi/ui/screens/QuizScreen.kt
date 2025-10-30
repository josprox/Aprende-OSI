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

    // --- Cálculo del progreso (CORREGIDO) ---
    val progress = if (uiState.questions.isNotEmpty()
        && !uiState.isQuizFinished) {
        (uiState.currentQuestionIndex.toFloat() + 1f) // Se añade 1f para mostrar el progreso de la pregunta actual
            .coerceAtMost(uiState.questions.size.toFloat()) // Asegura que no pase de 1f
        uiState.questions.size.toFloat() // <--- OPERADOR DE DIVISIÓN AÑADIDO
    } else if (uiState.isQuizFinished) {
        1f // Completo al finalizar
    } else {
        0f // Cargando
    }

    Scaffold(
        topBar = { TopAppBar(title
        = { Text("Test de Conocimientos") }) }
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
                QuizResult(
                    score = uiState.score,
                    totalQuestions = uiState.questions.size,
                    onFinish = { navController.popBackStack()}
                )
            } else {
                val currentQuestion =
                    uiState.questions[uiState.currentQuestionIndex]
                // Se considera "respondida" si está en el set (contestada o saltada)
                val isAnsweredOrSkipped = uiState.answeredQuestions.contains(uiState.currentQuestionIndex)

                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {

                    // --- Barra de Progreso ---
                    LinearProgressIndicator(
                        progress = { progress},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // --- Esta Columna INTERNA es la que se puede scrollear ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Pregunta ${uiState.currentQuestionIndex + 1}/${uiState.questions.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // <-- Este texto ahora se puede scrollear si es largo
                        Text(text = currentQuestion.questionText,
                            style = MaterialTheme.typography.headlineSmall)

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
                                optionKey = key, // <--- Pasamos la clave
                                isSelected = uiState.selectedAnswer == key,
                                isAnswerSubmitted = uiState.isAnswerSubmitted, // <--- Pasamos el estado
                                correctAnswerKey = uiState.correctOptionKey, // <--- Pasamos la respuesta correcta
                                onSelected = { viewModel.onAnswerSelected(key) }
                            )
                        }

                        // --- Feedback/Leyenda (NUEVA SECCIÓN) ---
                        if (uiState.isAnswerSubmitted && uiState.feedbackMessage != null) {
                            val isCorrect = uiState.selectedAnswer == uiState.correctOptionKey
                            // Colores más fuertes para el texto
                            val color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                            // Colores pastel para el fondo
                            val containerColor = if (isCorrect) Color(0xFFC8E6C9) else Color(0xFFFFCDD2)

                            Card(
                                colors = CardDefaults.cardColors(containerColor = containerColor),
                                modifier = Modifier.fillMaxWidth()
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
                        // Botón Saltar/Avanzar (Solo si no se ha respondido)
                        // Mostramos "Saltar" si la pregunta aún no está en el set de respondidas/saltadas
                        if (!uiState.isAnswerSubmitted && !isAnsweredOrSkipped) {
                            OutlinedButton(
                                onClick = { viewModel.onSkipClicked() },
                                // Lo deshabilitamos solo si ya se respondió/saltó
                                enabled = true
                            ) {
                                Text("Saltar Pregunta")
                            }
                        }

                        // Usamos Spacer si el botón de saltar no está presente para alinear a la derecha
                        if (uiState.isAnswerSubmitted || isAnsweredOrSkipped) {
                            Spacer(Modifier.weight(1f))
                        }

                        // Botón de Acción Principal (Dinámico)
                        Button(
                            onClick = {
                                if (uiState.isAnswerSubmitted) {
                                    viewModel.onNextClicked() // Mover a la siguiente
                                } else {
                                    viewModel.onSaveAndContinueClicked() // Guardar y retroalimentar
                                }
                            },
                            enabled = uiState.isAnswerSubmitted || uiState.selectedAnswer != null,
                            modifier = Modifier.weight(1f, fill = false) // Para que tome el espacio necesario
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


@Composable
fun AnswerOption(
    text: String,
    optionKey: String, // <--- Clave de la opción (A, B, C, D)
    isSelected: Boolean,
    isAnswerSubmitted: Boolean, // <--- Estado de envío
    correctAnswerKey: String?, // <--- Clave de respuesta correcta
    onSelected: () -> Unit
) {
    val isCorrect = isAnswerSubmitted && optionKey == correctAnswerKey
    val isIncorrect = isAnswerSubmitted && isSelected && optionKey != correctAnswerKey

    val containerColor = when {
        isCorrect -> Color(0xFFC8E6C9) // Verde pastel para correcto
        isIncorrect -> Color(0xFFFFCDD2) // Rojo pastel para incorrecto
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        isCorrect -> Color(0xFF2E7D32) // Verde oscuro
        isIncorrect -> Color(0xFFC62828) // Rojo oscuro
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    val cardColors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor)
    val border = if (isCorrect || isIncorrect) null else CardDefaults.outlinedCardBorder()

    // El Card se puede seleccionar solo si la respuesta NO ha sido enviada
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                enabled = !isAnswerSubmitted, // <--- Bloquea la selección
                onClick = onSelected
            ),
        colors = cardColors,
        border = border,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cambiamos el color del RadioButton
            RadioButton(
                selected = isSelected,
                onClick = null,
                enabled = !isAnswerSubmitted, // <--- Bloquea el RadioButton
                colors = RadioButtonDefaults.colors(
                    selectedColor = if (isCorrect || isIncorrect) contentColor else MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal // Resalta el texto correcto
            )
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
        Text("Tu puntuación:", style
        = MaterialTheme.typography.headlineMedium)
        Text("$score / $totalQuestions",
            style = MaterialTheme.typography.headlineLarge)
        Button(onClick = onFinish) {
            Text("Volver al Módulo")
        }
    }
}