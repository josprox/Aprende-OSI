package com.josprox.redesosi.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    viewModel: TestViewModel = hiltViewModel() // Inyecta el ViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val pendingTests = uiState.pendingTests
    val completedTests = uiState.completedTests

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Mis Exámenes",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }

        // --- Sección de Tests Pendientes ---
        item {
            Text(
                text = "Pendientes por Terminar",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )
        }
        if (pendingTests.isEmpty()) {
            item {
                Text(
                    text = "No tienes exámenes pendientes.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(pendingTests) { test ->
                TestItemCard(test = test) {
                    // Lógica al hacer clic: Resumir test
                    // Debes crear esta ruta de navegación
                    navController.navigate("quiz/${test.attempt.moduleId}?attemptId=${test.attempt.id}")
                }
            }
        }

        // --- Sección de Tests Completados (para Revisión) ---
        item {
            Text(
                text = "Exámenes Completados (Revisión)",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )
        }
        if (completedTests.isEmpty()) {
            item {
                Text(
                    text = "Aún no has completado ningún examen.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(completedTests) { test ->
                TestItemCard(test = test, isPending = false) {
                    // Lógica al hacer clic: Revisar test
                    // Debes crear una nueva pantalla y ruta para esto
                    navController.navigate(AppScreen.TestReview.createRoute(test.attempt.id))
                }
            }
        }
    }
}

@Composable
fun TestItemCard(
    test: TestAttemptWithModule,
    isPending: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = test.moduleTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (isPending) {
                    Text(
                        text = "En progreso...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "Calificación: %.1f".format(test.attempt.score),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Icon(
                imageVector = if (isPending) Icons.Default.Replay else Icons.Default.ArrowForward,
                contentDescription = if (isPending) "Resumir" else "Revisar"
            )
        }
    }
}