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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
    viewModel: CalificacionViewModel = hiltViewModel() // Inyecta el ViewModel
) {
    // Observa el estado del ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val completedTests = uiState.completedTests

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mis Calificaciones",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        if (completedTests.isEmpty()) {
            Text(
                text = "Aún no has completado ningún examen.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp) // <-- CAMBIO
            ) {
                items(completedTests) { testWithModule ->
                    TestResultCard(testWithModule)
                }
            }
        }
    }
}

@Composable
fun TestResultCard(test: TestAttemptWithModule) {
    val attempt = test.attempt
    val approvalScore = 8.0
    val isApproved = attempt.score >= approvalScore
    val scoreColor = if (isApproved) Color(0xFF006400) else MaterialTheme.colorScheme.error // <-- CAMBIO

    // --- Usamos ElevatedCard + ListItem ---
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = test.moduleTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            supportingContent = {
                Text(
                    text = "Resultado: ${attempt.correctAnswers} / ${attempt.totalQuestions}",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            trailingContent = {
                Text(
                    text = "%.1f".format(attempt.score), // Formatea a 1 decimal
                    style = MaterialTheme.typography.headlineSmall,
                    color = scoreColor,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}