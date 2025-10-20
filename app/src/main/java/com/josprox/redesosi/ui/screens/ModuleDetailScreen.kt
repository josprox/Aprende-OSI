package com.josprox.redesosi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import com.josprox.redesosi.data.database.SubmoduleEntity
import com.josprox.redesosi.data.repository.StudyRepository
import com.josprox.redesosi.navigation.AppScreen
import com.josprox.redesosi.ui.theme.RedesOSITheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
        _showConfirmDialog.value = false
        viewModelScope.launch {
            repository.forceRegenerateQuestions(moduleId)
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

    // --- Comportamiento de scroll para la TopAppBar ---
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    // El título se basará en el primer submódulo, o un default
    // Asumiendo que SubmoduleEntity tiene 'moduleTitle'
    val title = submodules.firstOrNull()?.let { "Detalle del Módulo" } ?: "Cargando..." // Ajusta esto si 'moduleTitle' está en otro lado


    Scaffold(
        // --- Modificador para conectar el scroll ---
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            // ---  Usamos MediumTopAppBar ---
            MediumTopAppBar(
                title = {
                    Text(
                        text = title, // Usa el título dinámico
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
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
                },
                // --- Pasamos el comportamiento de scroll ---
                scrollBehavior = scrollBehavior
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
            contentPadding = PaddingValues(bottom = 96.dp) // <--  Espacio para el FAB
        ) {
            items(submodules) { submodule ->
                Column(Modifier.padding(vertical = 16.dp)) {
                    Text(submodule.title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))

                    // --- Tema para el Markdown ---
                    RedesOSITheme {
                        RichText(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Markdown(
                                content = submodule.contentMd
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(top = 24.dp))
                }
            }
        }
    }
}