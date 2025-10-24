package com.josprox.redesosi.ui.screens

// import com.josprox.redesosi.ui.theme.RedesOSITheme // <-- ELIMINADO (ya no se usa aquí)
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import com.josprox.redesosi.navigation.AppScreen
import com.josprox.redesosi.vm.ModuleDetailViewModel

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

    // --- MODIFICADO: Obtenemos el título real desde el ViewModel ---
    val title by viewModel.moduleTitle.collectAsState()

    // --- Comportamiento de scroll para la TopAppBar ---
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())


    Scaffold(
        // --- Modificador para conectar el scroll ---
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            // ---  Usamos MediumTopAppBar ---
            MediumTopAppBar(
                title = {
                    Text(
                        text = title, // <-- Título dinámico
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
                .padding(paddingValues),
            // Padding horizontal se aplica a cada item para que el scrollbar llegue al borde
            contentPadding = PaddingValues(bottom = 96.dp) // <-- Espacio para el FAB
        ) {
            items(submodules) { submodule ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp) // Padding horizontal aquí
                        .padding(top = 16.dp, bottom = 24.dp) // Espaciado vertical
                ) {
                    Text(submodule.title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))

                    // --- AÑADIDO: Contenedor para seleccionar texto ---
                    SelectionContainer {
                        RichText(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Markdown(
                                content = submodule.contentMd
                            )
                        }
                    }

                    // --- ELIMINADO: HorizontalDivider ---
                }
            }
        }
    }
}