package com.josprox.redesosi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.josprox.redesosi.vm.CalificacionViewModel

@Composable
fun PantallaCalificacion(
    navController: NavHostController,
    viewModel: CalificacionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    CalificacionContent(
        titulo = uiState.titulo,
        botonTexto = uiState.botonTexto,
        onVerCalificacionClicked = {
            viewModel.onVerCalificacionClicked(navController)
        }
    )
}

@Composable
private fun CalificacionContent(
    titulo: String,
    botonTexto: String,
    onVerCalificacionClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onVerCalificacionClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(botonTexto)
        }
    }
}