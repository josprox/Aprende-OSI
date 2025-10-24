package com.josprox.redesosi.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.josprox.redesosi.navigation.AppScreen
import com.josprox.redesosi.vm.SubjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectListScreen(
    navController: NavController,
    viewModel: SubjectViewModel = hiltViewModel()
) {
    val subjects by viewModel.subjects.collectAsState(initial = emptyList())

    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
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
                    viewModel.prepareForFileAction(SubjectViewModel.FileAction.IMPORT)
                    filePickerLauncher.launch(arrayOf("application/json"))
                },
                icon = { Icon(Icons.Default.FileUpload, contentDescription = "Añadir materia") },
                text = { Text("Añadir Materia") }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // --- Diálogo de Opciones ---
            selectedSubject?.let { subject ->
                if (!showDeleteDialog) {
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
                                TextButton(
                                    onClick = {
                                        viewModel.prepareForFileAction(SubjectViewModel.FileAction.UPDATE)
                                        filePickerLauncher.launch(arrayOf("application/json"))
                                        viewModel.onDismissOptionsDialog()
                                    }
                                ) {
                                    Text("Actualizar desde JSON")
                                }
                                TextButton(
                                    onClick = {
                                        viewModel.onDismissOptionsDialog()
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

                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f) // Ocupa el espacio disponible
                                    .clickable { // <-- Acción de clic normal
                                        navController.navigate(AppScreen.ModuleList.createRoute(subject.id))
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp) // Padding ajustado
                            ) {
                                Text(
                                    text = subject.name,
                                    style = MaterialTheme.typography.titleMedium, // Un poco más pequeño
                                    // El color correcto para texto sobre 'surfaceVariant'
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Autor: ${subject.author} - v${subject.version}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // --- Botón de Opciones (Kebab menu) ---
                            IconButton(onClick = {
                                viewModel.onSubjectLongPress(subject) // Muestra el diálogo de opciones
                            }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Opciones de la materia",
                                    // Aseguramos que el icono también tenga el color correcto
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}