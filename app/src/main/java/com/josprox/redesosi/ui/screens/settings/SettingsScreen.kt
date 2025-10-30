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
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.josprox.redesosi.navigation.AppScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ajustes de la Aplicación",
                        style = MaterialTheme.typography.headlineSmall, // Título más grande y enfocado
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp), // Aplicamos padding horizontal aquí para la columna
        ) {

            Spacer(modifier = Modifier.height(8.dp)) // Espacio inicial

            // Sección 1: Gestión de Datos (Agrupada en una sola Card)
            SettingsGroupCard(title = "Gestión de Datos") {
                SettingsItem(
                    headline = "Copia de Seguridad y Restauración",
                    supportingText = "Exporta o importa todos tus datos de estudio y progreso.",
                    icon = Icons.Default.Save,
                    onClick = {
                        navController.navigate(AppScreen.BackupRestore.route)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp)) // Más espacio entre secciones

            // Sección 2: Acerca de la Aplicación (Agrupada en una sola Card)
            SettingsGroupCard(title = "Información y Legal") {
                SettingsItem(
                    headline = "Acerca de la Aplicación",
                    supportingText = "Detalles de la versión y reconocimiento a desarrolladores.",
                    icon = Icons.Default.Info,
                    onClick = {
                        // Navegación a la nueva pantalla
                        navController.navigate(AppScreen.LegalInfo.route)
                    }
                )
                // Usamos un Divider dentro de la Card para separar los elementos
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingsItem(
                    headline = "Información Legal",
                    supportingText = "Política de privacidad y términos de servicio.",
                    icon = Icons.Default.Lock,
                    onClick = {
                        navController.navigate(AppScreen.LegalInfo.route) // Asumiendo que es la misma pantalla o una ruta relacionada
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp)) // Espacio al final
        }
    }
}

/**
 * Componente que agrupa varios SettingsItem dentro de una Card grande.
 * Esto reduce la 'ruidosidad' visual y agrupa lógicamente.
 */
@Composable
fun SettingsGroupCard(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium, // Título de sección un poco más grande
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary, // Color de acento
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp) // Alineación con la Card
        )

        // Card que contiene todos los ítems de la sección
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large, // Forma distintiva para el grupo
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh // Fondo que destaca ligeramente
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

/**
 * Ítem individual, ahora más limpio y sin Card, ya que es hijo de SettingsGroupCard.
 */
@Composable
fun SettingsItem(
    headline: String,
    supportingText: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(headline, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(supportingText) },
        leadingContent = {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary // Iconos con color de acento
            )
        },
        trailingContent = {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable(onClick = onClick),
        // Usamos colores transparentes para que el fondo lo dé la Card padre
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}