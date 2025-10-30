package com.josprox.redesosi.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.LibraryBooks
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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
            TopAppBar(
                title = {
                    Text(
                        "Biblioteca de Materias",
                        fontWeight = FontWeight.Bold // Título más fuerte
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background // Fondo transparente para efecto limpio
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.prepareForFileAction(SubjectViewModel.FileAction.IMPORT)
                    filePickerLauncher.launch(arrayOf("application/json"))
                },
                icon = { Icon(Icons.Default.FileUpload, contentDescription = "Añadir materia") },
                text = { Text("Importar") },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer, // Usamos tertiary para un FAB distintivo
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // --- Diálogo de Opciones (Diseño más limpio) ---
            selectedSubject?.let { subject ->
                if (!showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { viewModel.onDismissOptionsDialog() },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LibraryBooks, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(subject.name)
                            }
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Autor: ${subject.author}")
                                Text("Versión: v${subject.version}", fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        viewModel.prepareForFileAction(SubjectViewModel.FileAction.UPDATE)
                                        filePickerLauncher.launch(arrayOf("application/json"))
                                        viewModel.onDismissOptionsDialog() // Cerrar después de lanzar
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Create, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Actualizar Contenido (JSON)")
                                }

                                Button(
                                    onClick = { viewModel.onDeleteClicked() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Eliminar Materia")
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { viewModel.onDismissOptionsDialog() }) {
                                Text("Cerrar")
                            }
                        },
                        // Se eliminan los dismissButton para usar los botones dentro de 'text'
                        dismissButton = null
                    )
                }
            }

            // --- Diálogo de Confirmación de Borrado (Se mantiene simple) ---
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.onDismissDeleteDialog() },
                    title = { Text("⚠️ Confirmar Eliminación") },
                    text = { Text("Estás seguro de que deseas eliminar permanentemente \"${selectedSubject?.name ?: ""}\"? Se perderá todo su contenido y el historial de exámenes.") },
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
                    .fillMaxSize(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(subjects) { subject ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.large), // Forma distintiva
                        colors = CardDefaults.cardColors(
                            // Usamos primary con alfa para darle un toque de color sin saturar
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        navController.navigate(AppScreen.ModuleList.createRoute(subject.id))
                                    }
                                    .padding(horizontal = 16.dp, vertical = 16.dp) // Más padding vertical
                            ) {
                                Text(
                                    text = subject.name,
                                    style = MaterialTheme.typography.titleLarge, // Título más prominente
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary // Acento de color en el título
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Autor: ${subject.author}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "v${subject.version}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary // Acento en la versión
                                )
                            }

                            // --- Botón de Opciones (Kebab menu) ---
                            IconButton(onClick = {
                                viewModel.onSubjectLongPress(subject)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Opciones de la materia",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}