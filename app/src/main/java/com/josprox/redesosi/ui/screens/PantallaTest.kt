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
import com.josprox.redesosi.vm.TestViewModel

@Composable
fun PantallaTest(
    navController: NavHostController,
    viewModel: TestViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    TestContent(
        titulo = uiState.titulo,
        botonTexto = uiState.botonTexto,
        onEmpezarTestClicked = {
            viewModel.onEmpezarTestClicked(navController)
        }
    )
}

@Composable
private fun TestContent(
    titulo: String,
    botonTexto: String,
    onEmpezarTestClicked: () -> Unit,
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
            onClick = onEmpezarTestClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(botonTexto)
        }
    }
}