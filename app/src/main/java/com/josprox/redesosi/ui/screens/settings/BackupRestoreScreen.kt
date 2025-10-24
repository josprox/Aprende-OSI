package com.josprox.redesosi.ui.screens.settings


import android.app.Application
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

    // Inicializamos el ViewModel usando nuestro Factory
    val viewModel: BackupViewModel = viewModel(
        factory = BackupViewModelFactory(application, database)
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- Launchers para los selectores de archivos ---

    // 1. Launcher para CREAR (Exportar)
    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"), // Usamos un tipo MIME genérico
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
            uri?.let {
                // Aquí podrías mostrar un diálogo de confirmación
                // "ADVERTENCIA: Esto sobreescribirá todos tus datos actuales."
                // Por simplicidad, lo restauramos directamente.
                viewModel.restoreBackup(context, it)
            }
        }
    )

    // --- Efecto para mostrar Snackbars ---
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage() // Limpia el mensaje después de mostrarlo
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Backup y Restauración") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {

            if (uiState.isLoading) {
                CircularProgressIndicator()
                Text(uiState.message ?: "Procesando...")
            } else {
                // --- Botón de Exportar ---
                Text(
                    "Guarda todos tus datos (materias, exámenes, progreso) en un solo archivo.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = {
                    // Nombre de archivo sugerido
                    backupLauncher.launch("backup_redesosi.db")
                }) {
                    Text("Crear Backup (Exportar)")
                }

                Spacer(Modifier.height(32.dp))

                // --- Botón de Importar ---
                Text(
                    "Restaura tus datos desde un archivo de backup. (La app se reiniciará)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = {
                    restoreLauncher.launch(arrayOf("application/octet-stream"))
                }) {
                    Text("Restaurar Backup (Importar)")
                }

                if(uiState.isError) {
                    Text(
                        uiState.message ?: "Ocurrió un error",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}