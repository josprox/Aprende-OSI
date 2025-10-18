package com.josprox.redesosi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Importante
import com.josprox.redesosi.vm.AprendeViewModel
import androidx.navigation.NavHostController

@Composable
fun PantallaAprende(
    navController: NavHostController,
    viewModel: AprendeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AprendeContent(
        titulo = uiState.titulo,
        tarjetaTitulo = uiState.tarjetaTitulo,
        tarjetaDescripcion = uiState.tarjetaDescripcion,
        botonTexto = uiState.botonTexto,
        onBotonOsiClicked = {
            viewModel.onBotonOsiClicked(navController)
        }
    )
}

@Composable
private fun AprendeContent(
    titulo: String,
    tarjetaTitulo: String,
    tarjetaDescripcion: String,
    botonTexto: String,
    onBotonOsiClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = tarjetaTitulo,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = tarjetaDescripcion,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onBotonOsiClicked, // Llama al evento
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(botonTexto)
                }
            }
        }
    }
}