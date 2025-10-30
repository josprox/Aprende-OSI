package com.josprox.redesosi.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.josprox.redesosi.data.database.TestAttemptWithModule
import com.josprox.redesosi.navigation.AppScreen
import com.josprox.redesosi.vm.TestViewModel

@Composable
fun PantallaTest(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: TestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pendingTests = uiState.pendingTests
    val completedTests = uiState.completedTests

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp), // Aplicamos padding horizontal aquí
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Título Expresivo ---
        item {
            Text(
                text = "Tu Historial de Exámenes",
                style = MaterialTheme.typography.displaySmall, // Título grande
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        // --- Separador: Pendientes ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Assignment, contentDescription = "Pendientes", tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "En Progreso",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        if (pendingTests.isEmpty()) {
            item {
                Text(
                    text = "🎉 ¡Perfecto! No tienes exámenes pendientes.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(pendingTests) { test ->
                TestItemCard(test = test) {
                    navController.navigate("quiz/${test.attempt.moduleId}?attemptId=${test.attempt.id}")
                }
            }
        }

        // --- Separador: Completados ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DoneAll, contentDescription = "Completados", tint = MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Completados para Revisión",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        if (completedTests.isEmpty()) {
            item {
                Text(
                    text = "Empieza un test para ver tu primer resultado.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(completedTests) { test ->
                TestItemCard(test = test, isPending = false) {
                    navController.navigate(AppScreen.TestReview.createRoute(test.attempt.id))
                }
            }
        }

        item { Spacer(Modifier.height(32.dp)) } // Padding extra al final
    }
}

// --- Tarjeta de Diseño Expressive ---
@Composable
fun TestItemCard(
    test: TestAttemptWithModule,
    isPending: Boolean = true,
    onClick: () -> Unit
) {
    // Definimos los colores y el contenido basado en el estado (UI/UX)
    val colorScheme = MaterialTheme.colorScheme
    val (cardContainerColor, iconColor, statusText) = if (isPending) {
        Triple(
            colorScheme.primaryContainer,
            colorScheme.primary,
            "En Progreso (${test.attempt.currentQuestionIndex}/${test.attempt.totalQuestions})"
        )
    } else {
        val score = test.attempt.score
        val isApproved = score >= 8.0 // Ejemplo de UX: Aprobado/Reprobado

        Triple(
            if (isApproved) colorScheme.tertiaryContainer else colorScheme.errorContainer.copy(alpha = 0.6f),
            if (isApproved) colorScheme.tertiary else colorScheme.error,
            "Calificación: %.1f".format(score)
        )
    }

    // Usamos Card y CardDefaults para más control sobre el color
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large, // Usamos una forma grande para ser distintivo
        colors = CardDefaults.cardColors(
            containerColor = cardContainerColor,
            contentColor = colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Sombra marcada
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Título del Módulo
                Text(
                    text = test.moduleTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                // Estado/Calificación
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = iconColor // Usamos el color de acento
                )
            }

            // Icono de acción
            Icon(
                imageVector = if (isPending) Icons.Default.Replay else Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = if (isPending) "Resumir Test" else "Revisar Calificación",
                tint = iconColor // Color de acento para el icono
            )
        }
    }
}