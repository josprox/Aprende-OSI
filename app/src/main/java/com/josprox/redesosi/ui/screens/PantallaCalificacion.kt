package com.josprox.redesosi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.josprox.redesosi.data.database.TestAttemptWithModule
import com.josprox.redesosi.vm.CalificacionViewModel

@Composable
fun PantallaCalificacion(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: CalificacionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val completedTests = uiState.completedTests

    LazyColumn( // Usamos LazyColumn como contenedor principal para el scroll
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        // --- ITEM 1: TÍTULO COLAPSABLE ---
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Historial de Calificaciones",
                    style = MaterialTheme.typography.displaySmall, // Título grande que se desplaza
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
            }
            if (completedTests.isEmpty()) {
                Text(
                    text = "¡Anímate! Completa tu primer examen para ver tus resultados aquí.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        // --- LISTA DE RESULTADOS ---
        if (completedTests.isNotEmpty()) {
            items(completedTests) { testWithModule ->
                TestResultCard(testWithModule)
            }
        }
    }
}

@Composable
fun TestResultCard(test: TestAttemptWithModule) {
    val attempt = test.attempt
    val approvalScore = 8.0
    val isApproved = attempt.score >= approvalScore

    // --- LÓGICA DE COLOR DINÁMICO M3 ---
    val colorScheme = MaterialTheme.colorScheme

    // 1. Color del fondo de la tarjeta
    val cardContainerColor = if (isApproved) {
        colorScheme.tertiaryContainer // Aprobado: Contenedor temático (e.g., Azul/Verde)
    } else {
        colorScheme.errorContainer.copy(alpha = 0.8f) // Reprobado: Contenedor de Error
    }

    // 2. Color del texto de la calificación (el grande)
    val scoreDisplayColor = if (isApproved) {
        colorScheme.onTertiaryContainer // Aprobado: Texto sobre el contenedor temático
    } else {
        colorScheme.error // Reprobado: Color de Error (Rojo dinámico)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = cardContainerColor,
            contentColor = colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 1. Título de la Materia
            Text(
                text = test.moduleTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))

            // 2. Puntaje / Correctas
            Text(
                text = "Respuestas Correctas: ${attempt.correctAnswers} / ${attempt.totalQuestions}",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            // 3. Calificación Final (Lo más prominente)
            Text(
                text = "Calificación Final",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurfaceVariant
            )
            Text(
                text = "%.1f".format(attempt.score),
                style = MaterialTheme.typography.displayMedium,
                color = scoreDisplayColor, // <-- Color dinámico aplicado aquí
                fontWeight = FontWeight.Black
            )
        }
    }
}