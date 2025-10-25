package com.josprox.redesosi.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalInfoScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Información Legal") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // Título Principal
            Text(
                "Acuerdos y Políticas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            // --- Sección 1: Términos y Condiciones ---
            LegalSection(title = "Términos y Condiciones de Uso (T&C)") {
                Text(
                    text = "Al usar esta aplicación, usted acepta los siguientes términos: La aplicación es proporcionada 'tal cual' sin garantías. El desarrollador no se hace responsable por la pérdida de datos o daños derivados de su uso. El contenido está protegido por derechos de autor.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // --- Sección 2: Política de Privacidad ---
            LegalSection(title = "Política de Privacidad") {
                Text(
                    text = "Esta aplicación está diseñada para ser privada y no recopila información personal identificable. Los datos (materias, notas) se almacenan localmente en su dispositivo y nunca se transmiten a servidores externos sin su consentimiento explícito (como al crear un backup local).",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // --- Sección 3: Licencias de Código Abierto ---
            LegalSection(title = "Licencias de Código Abierto") {
                Text(
                    text = "Esta aplicación utiliza bibliotecas de terceros bajo licencias de código abierto, incluyendo AndroidX, Jetpack Compose y Kotlin. La lista completa de licencias está disponible previa solicitud. Se respetan todas las obligaciones de licencia.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun LegalSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}