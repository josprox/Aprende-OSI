package com.josprox.redesosi.ui.screens.settings

import android.app.Application
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.josprox.redesosi.data.database.AppDatabase
import com.josprox.redesosi.vm.BackupViewModel
import com.josprox.redesosi.vm.BackupViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val database = AppDatabase.getDatabase(application)

    val viewModel: BackupViewModel = viewModel(
        factory = BackupViewModelFactory(application, database)
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado para controlar la aparición del diálogo de confirmación
    var showRestoreDialog by remember { mutableStateOf<Uri?>(null) }

    // --- Launchers para los selectores de archivos ---

    // 1. Launcher para CREAR (Exportar)
    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri: Uri? ->
            uri?.let {
                viewModel.createBackup(context, it)
            }
        }
    )

    // 2. Launcher para ABRIR (Importar)
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            // Al seleccionar el archivo, mostramos el diálogo de confirmación
            showRestoreDialog = uri
        }
    )

    // --- Efecto para mostrar Snackbars ---
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Copia de Seguridad y Restauración") }, // Título más claro
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Permite el scroll si hay mucho contenido
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            // Sección de Mensajes de Carga y Error
            if (uiState.isLoading) {
                Spacer(Modifier.height(32.dp))
                CircularProgressIndicator(Modifier.size(48.dp))
                Spacer(Modifier.height(16.dp))
                Text(
                    uiState.message ?: "Procesando...",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            } else if (uiState.isError) {
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "❌ Error",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            uiState.message ?: "Ocurrió un error inesperado al procesar la operación.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Secciones de Acción (Visibles solo si NO está cargando o en error crítico)
            if (!uiState.isLoading) {

                Spacer(Modifier.height(32.dp))

                // --- CARD: Exportar / Backup ---
                ActionCard(
                    title = "Crear Copia de Seguridad",
                    description = "Guarda todos tus datos de la aplicación (materias, exámenes, progreso) en un archivo local para que puedas moverlos o guardarlos.",
                    buttonText = "Guardar Backup",
                    icon = Icons.Filled.Save,
                    enabled = !uiState.isLoading,
                    onClick = {
                        backupLauncher.launch("backup_AprendeMas.db")
                    }
                )

                Divider(Modifier.padding(vertical = 32.dp))

                // --- CARD: Importar / Restaurar ---
                ActionCard(
                    title = "Restaurar Datos",
                    description = "Carga datos desde un archivo de copia de seguridad previo. ¡ATENCIÓN! Esto SOBRESCRIBIRÁ todos tus datos actuales.",
                    buttonText = "Restaurar Backup",
                    icon = Icons.Filled.Restore,
                    enabled = !uiState.isLoading,
                    isDestructive = true,
                    onClick = {
                        restoreLauncher.launch(arrayOf("application/octet-stream"))
                    }
                )
            }
        }
    }

    // --- Diálogo de Confirmación (UX Crítica) ---
    showRestoreDialog?.let { uri ->
        RestoreConfirmationDialog(
            onDismiss = { showRestoreDialog = null },
            onConfirm = {
                // Aquí lanzamos la restauración real
                viewModel.restoreBackup(context, uri)
                showRestoreDialog = null
            }
        )
    }
}


/**
 * Componente para unificar el diseño de las tarjetas de acción.
 */
@Composable
fun ActionCard(
    title: String,
    description: String,
    buttonText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDestructive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if(isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                colors = if (isDestructive) ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ) else ButtonDefaults.buttonColors()
            ) {
                Text(buttonText)
            }
        }
    }
}

/**
 * Diálogo de advertencia para la restauración de datos.
 */
@Composable
fun RestoreConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Info, contentDescription = "Advertencia", tint = MaterialTheme.colorScheme.error) },
        title = { Text("⚠️ Advertencia de Restauración") },
        text = {
            Column {
                Text(
                    "Estás a punto de **SOBRESCRIBIR** todos tus datos actuales (materias, exámenes, notas, etc.) con los datos del archivo de backup.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Esta acción **NO** se puede deshacer y podría causar la pérdida de información reciente.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "¿Deseas continuar y reiniciar la aplicación?",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Confirmar Restauración")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}