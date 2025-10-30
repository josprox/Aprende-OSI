package com.josprox.redesosi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.josprox.redesosi.vm.ChatMessage
import com.josprox.redesosi.vm.ModuleDetailViewModel
import com.mikepenz.markdown.compose.components.MarkdownComponents
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeFence
import com.mikepenz.markdown.m3.Markdown
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxThemes
import kotlinx.coroutines.flow.collectLatest


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    moduleId: Int,
    viewModel: ModuleDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.chatUiState.collectAsState()
    val title by viewModel.moduleTitle.collectAsState()
    val scrollState = rememberLazyListState()

    // Manejar el evento de scroll
    LaunchedEffect(uiState.chatHistory.size) {
        if (uiState.chatHistory.isNotEmpty()) {
            scrollState.animateScrollToItem(uiState.chatHistory.lastIndex)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.scrollToBottomEvent.collectLatest {
            if (uiState.chatHistory.isNotEmpty()) {
                scrollState.animateScrollToItem(uiState.chatHistory.lastIndex)
            }
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // --- Configuración de Highlights (Debe estar aquí o en el ViewModel) ---
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    val highlightsBuilder = remember(isDarkTheme) {
        Highlights.Builder().theme(SyntaxThemes.atom(darkMode = isDarkTheme))
    }
    val customMarkdownComponents = markdownComponents(
        // Sustituimos CodeFence para el resaltado
        codeFence = {
            MarkdownHighlightedCodeFence(
                content = it.content,
                node = it.node,
                highlightsBuilder = highlightsBuilder,
                // showHeader ha sido eliminado
            )
        }
        // No necesitamos sobrescribir codeBlock aquí ya que casi el código IA usa codeFence (```)
    )
    // --- Fin Configuración de Highlights ---


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Asistente Módulo $title",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Área del chat
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.chatHistory) { message ->
                    MessageBubble(
                        message = message,
                        isUser = message.role == "user",
                        customMarkdownComponents = customMarkdownComponents // Pasamos los componentes
                    )
                }
                // Indicador de carga
                if (uiState.isModelThinking && (uiState.chatHistory.isEmpty() || uiState.chatHistory.last().isPending.not())) {
                    item {
                        ThinkingIndicator()
                    }
                }
            }

            // Input del usuario
            ChatInput(
                currentInput = uiState.currentInput,
                onInputChanged = viewModel::onInputChanged,
                onSend = viewModel::onSendMessage,
                isEnabled = !uiState.isModelThinking
            )
        }
    }
}

// --- Componente de Burbuja de Mensaje Mejorado (Modificado para recibir Componentes) ---
@Composable
fun MessageBubble(
    message: ChatMessage,
    isUser: Boolean,
    customMarkdownComponents: MarkdownComponents // Recibe la interfaz correcta
) {
    val cornerRadius = 16.dp
    val tailRadius = 4.dp

    val bubbleShape = if (isUser) {
        RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius,
            bottomStart = cornerRadius,
            bottomEnd = tailRadius
        )
    } else {
        RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius,
            bottomStart = tailRadius,
            bottomEnd = cornerRadius
        )
    }

    val containerColor = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val contentColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = bubbleShape,
            colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.widthIn(min = 80.dp, max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {

                if (message.isPending) {
                    AnimatedLoadingDots(
                        dotColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    if (isUser) {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = contentColor
                        )
                    } else {
                        // --- USAMOS EL MARKDOWN AVANZADO ---
                        Markdown(
                            content = message.content,
                            components = customMarkdownComponents
                        )
                    }
                }
            }
        }
    }
}

// --- Componentes auxiliares (ChatInput, AnimatedLoadingDots, ThinkingIndicator) se mantienen igual ---
@Composable
fun ChatInput(
    currentInput: String,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    isEnabled: Boolean
) {
    Surface(
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            TextField(
                value = currentInput,
                onValueChange = onInputChanged,
                placeholder = { Text("Pregunta sobre el módulo...") },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp, max = 150.dp),
                shape = RoundedCornerShape(28.dp),
                singleLine = false,
                maxLines = 5,
                enabled = isEnabled,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Default
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilledIconButton(
                onClick = onSend,
                enabled = isEnabled && currentInput.isNotBlank(),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Enviar",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun AnimatedLoadingDots(dotColor: Color = MaterialTheme.colorScheme.onSurface) {
    val dots = listOf("·", "··", "···")
    var dotIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(400)
            dotIndex = (dotIndex + 1) % dots.size
        }
    }
    Text(
        text = "Escribiendo${dots[dotIndex]}",
        style = MaterialTheme.typography.bodyLarge,
        color = dotColor
    )
}

@Composable
fun ThinkingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.widthIn(min = 80.dp, max = 320.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                AnimatedLoadingDots(
                    dotColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}