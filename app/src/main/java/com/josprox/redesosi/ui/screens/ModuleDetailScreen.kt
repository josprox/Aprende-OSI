package com.josprox.redesosi.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row // Importar Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionAnswer // Importar QuestionAnswer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import com.josprox.redesosi.navigation.AppScreen
import com.josprox.redesosi.vm.ModuleDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleDetailScreen(
    navController: NavController,
    moduleId: Int,
    viewModel: ModuleDetailViewModel = hiltViewModel()
) {
    val submodules by viewModel.submodules.collectAsState(initial = emptyList())
    val showDialog by viewModel.showConfirmDialog.collectAsState()

    val title by viewModel.moduleTitle.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())


    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = title,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onRegenerateClicked() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Regenerar preguntas"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            // --- AÑADIDO: Agrupamos los FABs en un Row ---
            Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                // 1. Botón para el CHAT
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(AppScreen.Chat.createRoute(moduleId)) }, // ✅ Usar createRoute
                    icon = { Icon(Icons.Default.QuestionAnswer, contentDescription = "") },
                    text = { Text("Preguntar IA") }
                )
                // 2. Botón para el QUIZ (existente)
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(AppScreen.Quiz.createRoute(moduleId)) },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "") },
                    text = { Text("Iniciar Test") }
                )
            }
        }
    ) { paddingValues ->

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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(submodules) { submodule ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = 24.dp)
                ) {
                    Text(submodule.title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))

                    SelectionContainer {
                        RichText(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Markdown(
                                content = submodule.contentMd
                            )
                        }
                    }
                }
            }
        }
    }
}