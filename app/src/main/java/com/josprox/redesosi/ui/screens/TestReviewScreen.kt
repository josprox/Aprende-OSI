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
        topBar = { TopAppBar(title = { Text(uiState.module?.title ?: "Revisión de Examen") }) }
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                items(uiState.reviewedQuestions) { reviewedQuestion ->
                    ReviewQuestionCard(reviewedQuestion = reviewedQuestion)
                }
            }
        }
    }
}

@Composable
fun TestResultHeader(title: String, score: Double, correct: Int, total: Int) {
    val approvalScore = 8.0
    val isApproved = score >= approvalScore
    val scoreColor = if (isApproved) Color(0xFF006400) else MaterialTheme.colorScheme.error

    // --- CAMBIO: Usamos Card normal con CardDefaults.filledTonalCardColors() ---
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Resultado: $correct / $total",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "%.1f".format(score),
                    style = MaterialTheme.typography.displaySmall,
                    color = scoreColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ReviewQuestionCard(reviewedQuestion: ReviewedQuestion) {
    val question = reviewedQuestion.question
    val userAnswer = reviewedQuestion.userAnswer

    // --- Tarjeta más sutil ---
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            // Creamos una lista de las 4 opciones
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
        }
    }
}

// --- Data class para solucionar el error de Triple ---
private data class AnswerStyle(
    val backgroundColor: Color,
    val icon: ImageVector?,
    val iconColor: Color,
    val contentColor: Color
)

@Composable
fun AnswerReviewOption(
    text: String,
    optionKey: String,
    userSelection: String,
    correctAnswer: String
) {
    val isCorrect = optionKey == correctAnswer
    val isSelected = optionKey == userSelection

    // --- Lógica de color ahora usa el data class ---
    val (backgroundColor, icon, iconColor, contentColor) = when {
        // Opción correcta (siempre se marca)
        isCorrect -> AnswerStyle(
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f),
            icon = Icons.Default.Check,
            iconColor = MaterialTheme.colorScheme.onTertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
        // Opción que el usuario eligió y estaba INCORRECTA
        isSelected && !isCorrect -> AnswerStyle(
            backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
            icon = Icons.Default.Close,
            iconColor = MaterialTheme.colorScheme.onErrorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
        // Opción neutral (ni correcta, ni seleccionada)
        else -> AnswerStyle(
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            icon = null,
            iconColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                style = MaterialTheme.typography.bodyLarge
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