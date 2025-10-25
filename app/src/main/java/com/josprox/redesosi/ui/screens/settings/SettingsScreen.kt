package com.josprox.redesosi.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.josprox.redesosi.navigation.AppScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Configuración") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // Sección 1: Gestión de Datos
            SettingsSection(title = "Gestión de Datos") {
                SettingsItem(
                    headline = "Copia de Seguridad y Restauración",
                    supportingText = "Exporta o importa todos tus datos de la aplicación.",
                    icon = Icons.Default.Save,
                    onClick = {
                        navController.navigate(AppScreen.BackupRestore.route)
                    }
                )
            }

            // --- Espaciador y Separador ---
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // Sección 2: Acerca de la Aplicación
            SettingsSection(title = "Acerca de la Aplicación") {
                SettingsItem(
                    headline = "Información Legal",
                    supportingText = "Política de privacidad y términos de servicio.",
                    icon = Icons.Default.Info,
                    onClick = {
                        // Navegación a la nueva pantalla
                        navController.navigate(AppScreen.LegalInfo.route)
                    }
                )
            }
        }
    }
}

/**
 * Componente reutilizable para los títulos de sección en Ajustes.
 * El padding horizontal se ha movido aquí para que las tarjetas ocupen el ancho total de la sección.
 */
@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

/**
 * Componente reutilizable para cada ítem de ajuste, ahora envuelto en una Card.
 */
@Composable
fun SettingsItem(
    headline: String,
    supportingText: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    // 1. Usamos Card para el diseño de tarjeta con bordes redondeados
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // Pequeño padding vertical entre ítems
        shape = MaterialTheme.shapes.medium, // Bordes redondeados por defecto (o custom si lo defines)
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // Sombra sutil
    ) {
        // 2. Usamos ListItem dentro del Card para el contenido
        ListItem(
            headlineContent = { Text(headline) },
            supportingContent = { Text(supportingText) },
            leadingContent = {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 8.dp)
                )
            },
            // El clickable se aplica al modifier del ListItem
            modifier = Modifier.clickable(onClick = onClick)
        )
    }
}