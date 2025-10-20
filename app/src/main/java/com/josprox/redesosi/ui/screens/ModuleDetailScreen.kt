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
import androidx.navigation.NavController
import com.halilibo.richtext.markdown.Markdown // Import correcto
import com.halilibo.richtext.ui.material3.RichText // Import para el contenedor
import com.josprox.redesosi.data.database.SubmoduleEntity
import com.josprox.redesosi.data.repository.StudyRepository
import com.josprox.redesosi.navigation.AppScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ModuleDetailViewModel @Inject constructor(
    repository: StudyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val moduleId: Int = checkNotNull(savedStateHandle["moduleId"])
    val submodules: Flow<List<SubmoduleEntity>> = repository.getSubmodulesForModule(moduleId)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleDetailScreen(
    navController: NavController,
    moduleId: Int,
    viewModel: ModuleDetailViewModel = hiltViewModel()
) {
    val submodules by viewModel.submodules.collectAsState(initial = emptyList())

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
                onClick = { navController.navigate(AppScreen.Quiz.createRoute(moduleId)) },
                icon = { Icon(Icons.Default.PlayArrow, contentDescription = "") },
                text = { Text("Iniciar Test") }
            )
        }
    ) { paddingValues ->
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

                    // --- BLOQUE CORREGIDO ---
                    // Se envuelve el componente Markdown en un RichText,
                    // que es quien recibe el modifier.
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

