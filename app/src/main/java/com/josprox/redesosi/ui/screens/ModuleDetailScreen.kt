package com.josprox.redesosi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh // <-- Import
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // <-- Import
import androidx.navigation.NavController
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import com.josprox.redesosi.data.database.SubmoduleEntity
import com.josprox.redesosi.data.repository.StudyRepository
import com.josprox.redesosi.navigation.AppScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow // <-- Import
import kotlinx.coroutines.flow.asStateFlow // <-- Import
import kotlinx.coroutines.launch // <-- Import
import javax.inject.Inject

//=================================================================
// 1. VIEWMODEL
//=================================================================
@HiltViewModel
class ModuleDetailViewModel @Inject constructor(
    private val repository: StudyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Obtenemos el ID del módulo desde el estado guardado
    val moduleId: Int = checkNotNull(savedStateHandle["moduleId"])

    // Este Flow de submódulos está perfecto
    val submodules: Flow<List<SubmoduleEntity>> = repository.getSubmodulesForModule(moduleId)

    // --- Lógica del Diálogo (Para "Regenerar") ---

    private val _showConfirmDialog = MutableStateFlow(false)
    val showConfirmDialog = _showConfirmDialog.asStateFlow()

    // Esto se llama al presionar el botón de "Regenerar"
    fun onRegenerateClicked() {
        _showConfirmDialog.value = true
    }

    fun onDialogDismiss() {
        _showConfirmDialog.value = false
    }

    // Esto se llama al confirmar el diálogo
    fun onRegenerateConfirm() {
        _showConfirmDialog.value = false // Oculta el diálogo
        viewModelScope.launch {
            // 1. Llama a la función destructiva
            // ESTO SOLO AFECTA AL 'moduleId' ACTUAL
            repository.forceRegenerateQuestions(moduleId)

            // 2. Aquí puedes añadir un Toast si quieres notificar al usuario
            // (requiere inyectar el 'Application' en el ViewModel)
        }
    }
}

//=================================================================
// 2. SCREEN COMPOSABLE
//=================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleDetailScreen(
    navController: NavController,
    moduleId: Int,
    viewModel: ModuleDetailViewModel = hiltViewModel()
) {
    val submodules by viewModel.submodules.collectAsState(initial = emptyList())
    val showDialog by viewModel.showConfirmDialog.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(submodules.firstOrNull()?.let { "Detalle del Módulo" } ?: "Cargando...") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                // --- Botón de "Regenerar" en la barra ---
                actions = {
                    IconButton(onClick = { viewModel.onRegenerateClicked() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Regenerar preguntas"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                // Esto inicia un test nuevo (attemptId=0)
                // NO borra el historial. Reutiliza las preguntas existentes.
                onClick = { navController.navigate(AppScreen.Quiz.createRoute(moduleId)) },
                icon = { Icon(Icons.Default.PlayArrow, contentDescription = "") },
                text = { Text("Iniciar Test") }
            )
        }
    ) { paddingValues ->

        // --- Diálogo de Confirmación (para Regenerar) ---
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onDialogDismiss() },
                title = { Text("¿Regenerar Preguntas?") },
                text = { Text("Esto borrará permanentemente todo tu historial (exámenes pendientes y calificaciones) y generará preguntas nuevas para este módulo. ¿Continuar?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.onRegenerateConfirm()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Regenerar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onDialogDismiss() }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // --- Contenido de la Pantalla ---
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