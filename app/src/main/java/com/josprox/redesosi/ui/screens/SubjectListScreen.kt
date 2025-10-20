package com.josprox.redesosi.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    filePickerLauncher.launch(arrayOf("application/json"))
                },
                icon = { Icon(Icons.Default.FileUpload, contentDescription = "Añadir materia") },
                text = { Text("Añadir Materia") }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

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
                                    // Click normal: navega
                                    navController.navigate(AppScreen.ModuleList.createRoute(subject.id))
                                },
                                onLongClick = {
                                    // Click largo: muestra diálogo de borrado
                                    viewModel.onSubjectLongPress(subject)
                                }
                            ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = subject.name,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}