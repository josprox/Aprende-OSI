package com.josprox.redesosi.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.josprox.redesosi.navigation.AppScreen
import com.josprox.redesosi.vm.SubjectViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SubjectListScreen(
    navController: NavController,
    viewModel: SubjectViewModel = hiltViewModel()
) {
    val subjects by viewModel.subjects.collectAsState(initial = emptyList())

    // --- Ahora tenemos dos estados de diálogo ---
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                // --- Llamamos a la nueva función central ---
                viewModel.processFile(uri)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Materias") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    // --- Preparamos al VM para un IMPORT ---
                    viewModel.prepareForFileAction(SubjectViewModel.FileAction.IMPORT)
                    filePickerLauncher.launch(arrayOf("application/json"))
                    
                },
                icon = { Icon(Icons.Default.FileUpload, contentDescription = "Añadir materia") },
                text = { Text("Añadir Materia") }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // --- Diálogo de Opciones (se muestra primero) ---
            selectedSubject?.let { subject ->
                if (!showDeleteDialog) { // Solo muestra este si NO estamos confirmando el borrado
                    AlertDialog(
                        onDismissRequest = { viewModel.onDismissOptionsDialog() },
                        title = { Text(subject.name) },
                        text = {
                            Column {
                                Text("¿Qué deseas hacer con esta materia?")
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { viewModel.onDismissOptionsDialog() }) {
                                Text("Cancelar")
                            }
                        },
                        dismissButton = {
                            Column {
                                // Botón para Actualizar
                                TextButton(
                                    onClick = {
                                        // 1. Prepara al VM para un UPDATE
                                        viewModel.prepareForFileAction(SubjectViewModel.FileAction.UPDATE)
                                        // 2. Lanza el picker
                                        filePickerLauncher.launch(arrayOf("application/json"))
                                        // 3. Cierra el diálogo de opciones
                                        viewModel.onDismissOptionsDialog()
                                    }
                                ) {
                                    Text("Actualizar desde JSON")
                                }

                                // Botón para Eliminar (que abre el otro diálogo)
                                TextButton(
                                    onClick = {
                                        // 1. Cierra este diálogo
                                        viewModel.onDismissOptionsDialog()
                                        // 2. Pide al VM que abra el diálogo de confirmación
                                        viewModel.onDeleteClicked()
                                    }
                                ) {
                                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    )
                }
            }

            // --- Diálogo de Confirmación de Borrado ---
            // Ahora se controla por 'showDeleteDialog'
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.onDismissDeleteDialog() },
                    title = { Text("¿Eliminar Materia?") },
                    text = { Text("¿Estás seguro de que quieres eliminar \"${selectedSubject?.name ?: ""}\"? Esta acción es permanente y borrará todos sus módulos y tu historial de exámenes.") },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.onConfirmDelete() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.onDismissDeleteDialog() }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
            

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(subjects) { subject ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    navController.navigate(AppScreen.ModuleList.createRoute(subject.id))
                                },
                                onLongClick = {
                                    viewModel.onSubjectLongPress(subject)
                                }
                            ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.titleLarge,
                            )
                            // Mostramos autor y versión
                            Text(
                                text = "Autor: ${subject.author} - v${subject.version}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}