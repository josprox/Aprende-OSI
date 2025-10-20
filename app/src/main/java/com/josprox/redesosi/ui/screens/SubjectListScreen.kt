package com.josprox.redesosi.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi // <-- AÑADIR IMPORT
import androidx.compose.foundation.combinedClickable // <-- AÑADIR IMPORT
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class) // <-- AÑADIR ExperimentalFoundationApi
@Composable
fun SubjectListScreen(
    navController: NavController,
    viewModel: SubjectViewModel = hiltViewModel()
) {
    val subjects by viewModel.subjects.collectAsState(initial = emptyList())

    // --- AÑADIDO: Observar el estado del diálogo de borrado ---
    val subjectToDelete by viewModel.subjectToDelete.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.importMateria(uri)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Materias") })
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // --- AÑADIDO: Diálogo de confirmación para borrar ---
            subjectToDelete?.let { subject ->
                AlertDialog(
                    onDismissRequest = { viewModel.onDismissDeleteDialog() },
                    title = { Text("¿Eliminar Materia?") },
                    text = { Text("¿Estás seguro de que quieres eliminar \"${subject.name}\"? Esta acción es permanente y borrará todos sus módulos y tu historial de exámenes.") },
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
            // --- FIN DE LO AÑADIDO ---

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(subjects) { subject ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            // --- CAMBIADO: Usamos combinedClickable ---
                            .combinedClickable(
                                onClick = {
                                    // Click normal: navega
                                    navController.navigate(AppScreen.ModuleList.createRoute(subject.id))
                                },
                                onLongClick = {
                                    // Click largo: muestra diálogo de borrado
                                    viewModel.onSubjectLongPress(subject)
                                }
                            )
                        // --- FIN DEL CAMBIO ---
                    ) {
                        Text(
                            text = subject.name,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            Button(
                onClick = {
                    filePickerLauncher.launch(arrayOf("application/json"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text("Añadir Materia desde JSON")
            }
        }
    }
}