package com.josprox.redesosi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.josprox.redesosi.vm.ReviewedQuestion
import com.josprox.redesosi.vm.TestReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestReviewScreen(
    navController: NavController,
    attemptId: Long,
    viewModel: TestReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        uiState.module?.title ?: "Revisión de Examen",
                        fontWeight = FontWeight.Bold // Título más audaz
                    )
                },
                // Usamos colores de la app bar para que no distraiga
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.attempt == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: No se encontró el examen.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp) // Más espacio entre tarjetas
            ) {
                // 1. Cabecera con el resultado
                item {
                    TestResultHeader(
                        title = uiState.module?.title ?: "Examen",
                        score = uiState.attempt!!.score,
                        correct = uiState.attempt!!.correctAnswers,
                        total = uiState.attempt!!.totalQuestions
                    )
                }

                // 2. Lista de preguntas y respuestas
                items(uiState.reviewedQuestions, key = { it.question.id }) { reviewedQuestion ->
                    ReviewQuestionCard(reviewedQuestion = reviewedQuestion)
                }

                // Espacio al final de la lista
                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

// --- Componente de Cabecera de Resultado (Más Expresivo) ---
@Composable
fun TestResultHeader(title: String, score: Double, correct: Int, total: Int) {
    val approvalScore = 8.0
    val isApproved = score >= approvalScore

    val colorScheme = MaterialTheme.colorScheme
    val headerColor = if (isApproved) colorScheme.tertiaryContainer else colorScheme.errorContainer
    val scoreColor = if (isApproved) colorScheme.onTertiaryContainer else colorScheme.error

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge, // Forma distintiva y grande
        colors = CardDefaults.cardColors(containerColor = headerColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) { // Más padding
            Text(
                text = "Revisión: $title",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$correct de $total correctas",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    text = "%.1f".format(score),
                    style = MaterialTheme.typography.displayMedium, // Calificación grande
                    color = scoreColor,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

// --- Componente de Revisión de Pregunta (Foco en la Explicación) ---
@Composable
fun ReviewQuestionCard(reviewedQuestion: ReviewedQuestion) {
    val question = reviewedQuestion.question
    val userAnswer = reviewedQuestion.userAnswer

    val colorScheme = MaterialTheme.colorScheme

    // Determinamos si el usuario respondió correctamente
    val isUserCorrect = userAnswer.isCorrect

    // Usamos OutlinedCard para la pregunta y la respuesta
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large, // Forma más grande
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isUserCorrect) colorScheme.tertiary.copy(alpha = 0.8f)
                else colorScheme.error.copy(alpha = 0.8f)
            ),
            width = 2.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Título de la Pregunta
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.titleLarge, // Más grande
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            // Lista de Opciones
            val options = listOf(
                "A" to question.optionA,
                "B" to question.optionB,
                "C" to question.optionC,
                "D" to question.optionD
            )

            options.forEach { (key, text) ->
                AnswerReviewOption(
                    text = text,
                    optionKey = key,
                    userSelection = userAnswer.selectedOption,
                    correctAnswer = question.correctAnswer
                )
                Spacer(Modifier.height(8.dp))
            }

            // --- SECCIÓN: EXPLICACIÓN DETALLADA (EL FOCO) ---
            if (!userAnswer.explanationText.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                // Usamos un Card relleno para la explicación, destacando el contenido
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        // Color de fondo más sólido para la explicación
                        containerColor = colorScheme.secondaryContainer,
                        contentColor = colorScheme.onSecondaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Explicación Detallada (IA):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = userAnswer.explanationText!!, // Explicación guardada
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            // --- FIN SECCIÓN ---
        }
    }
}

private data class AnswerStyle(
    val backgroundColor: Color,
    val icon: ImageVector?,
    val iconColor: Color,
    val contentColor: Color
)

// --- Componente de Opción Individual (Detalle) ---
@Composable
fun AnswerReviewOption(
    text: String,
    optionKey: String,
    userSelection: String,
    correctAnswer: String
) {
    val isCorrect = optionKey == correctAnswer
    val isSelected = optionKey == userSelection

    val colorScheme = MaterialTheme.colorScheme

    // Lógica de colores de Material 3 para Aprobación/Error
    val (backgroundColor, icon, iconColor, contentColor) = when {
        // Opción correcta (siempre terciario/aprobación)
        isCorrect -> AnswerStyle(
            backgroundColor = colorScheme.tertiaryContainer.copy(alpha = 0.8f),
            icon = Icons.Default.Check,
            iconColor = colorScheme.onTertiaryContainer,
            contentColor = colorScheme.onTertiaryContainer
        )
        // Opción que el usuario eligió y estaba INCORRECTA (error)
        isSelected && !isCorrect -> AnswerStyle(
            backgroundColor = colorScheme.errorContainer.copy(alpha = 0.8f),
            icon = Icons.Default.Close,
            iconColor = colorScheme.onErrorContainer,
            contentColor = colorScheme.onErrorContainer
        )
        // Opción neutral (ni correcta, ni seleccionada)
        else -> AnswerStyle(
            backgroundColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
            icon = null,
            iconColor = Color.Transparent,
            contentColor = colorScheme.onSurfaceVariant
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isCorrect || isSelected) FontWeight.Medium else FontWeight.Normal
            )
            if (icon != null) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = if (isCorrect) "Respuesta Correcta" else "Respuesta Incorrecta",
                    tint = iconColor
                )
            }
        }
    }
}