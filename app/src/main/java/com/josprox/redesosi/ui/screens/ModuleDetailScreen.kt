package com.josprox.redesosi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // <-- AÑADIR IMPORT
import androidx.navigation.NavController
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import com.josprox.redesosi.data.database.SubmoduleEntity
import com.josprox.redesosi.data.repository.StudyRepository
import com.josprox.redesosi.navigation.AppScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow // <-- AÑADIR IMPORT
import kotlinx.coroutines.flow.asStateFlow // <-- AÑADIR IMPORT
import kotlinx.coroutines.launch // <-- AÑADIR IMPORT
import javax.inject.Inject

@HiltViewModel
class ModuleDetailViewModel @Inject constructor(
    private val repository: StudyRepository, // <-- CAMBIADO A private val
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val moduleId: Int = checkNotNull(savedStateHandle["moduleId"])

    val submodules: Flow<List<SubmoduleEntity>> = repository.getSubmodulesForModule(moduleId)

    // --- AÑADIDO: Lógica para el diálogo de confirmación ---

    private val _showConfirmDialog = MutableStateFlow(false)
    val showConfirmDialog = _showConfirmDialog.asStateFlow()

    fun onStartTestClicked() {
        _showConfirmDialog.value = true
    }

    fun onDialogDismiss() {
        _showConfirmDialog.value = false
    }

    fun onDialogConfirm(onNavigate: (String) -> Unit) {
        _showConfirmDialog.value = false // Oculta el diálogo
        viewModelScope.launch {
            // 1. Llama a la función destructiva que borra preguntas e historial
            repository.forceRegenerateQuestions(moduleId)

            // 2. Navega a la pantalla del Quiz (que ahora creará un test nuevo)
            onNavigate(AppScreen.Quiz.createRoute(moduleId))
        }
    }
    // --- FIN DE LO AÑADIDO ---
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleDetailScreen(
    navController: NavController,
    moduleId: Int,
    viewModel: ModuleDetailViewModel = hiltViewModel()
) {
    val submodules by viewModel.submodules.collectAsState(initial = emptyList())

    // --- AÑADIDO: Observar el estado del diálogo ---
    val showDialog by viewModel.showConfirmDialog.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(submodules.firstOrNull()?.let { "Detalle del Módulo" } ?: "Cargando...") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                // --- CAMBIADO: Ahora muestra el diálogo en lugar de navegar ---
                onClick = { viewModel.onStartTestClicked() },
                icon = { Icon(Icons.Default.PlayArrow, contentDescription = "") },
                text = { Text("Iniciar Test") }
            )
        }
    ) { paddingValues ->

        // --- AÑADIDO: El Diálogo de Confirmación ---
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onDialogDismiss() },
                title = { Text("¿Empezar test nuevo?") },
                text = { Text("Esto generará nuevas preguntas, pero borrará todo tu historial (exámenes pendientes y calificaciones) para este módulo. ¿Continuar?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.onDialogConfirm { route ->
                                navController.navigate(route)
                            }
                        }
                    ) {
                        Text("Continuar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onDialogDismiss() }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        // --- FIN DE LO AÑADIDO ---

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 80.dp) // Espacio para el FAB
        ) {
            items(submodules) { submodule ->
                Column(Modifier.padding(vertical = 8.dp)) {
                    Text(submodule.title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    RichText(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Markdown(
                            content = submodule.contentMd
                        )
                    }

                    Divider(modifier = Modifier.padding(top = 16.dp))
                }
            }
        }
    }
}