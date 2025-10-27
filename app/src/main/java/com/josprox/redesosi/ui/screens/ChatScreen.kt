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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import com.josprox.redesosi.vm.ChatMessage
import com.josprox.redesosi.vm.ModuleDetailViewModel
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

    Scaffold(
        topBar = {
            LargeTopAppBar( // Usamos LargeTopAppBar para el estilo Expressive/Académico
                title = { Text("Asistente Módulo $title") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
                        isUser = message.role == "user"
                    )
                }
                // Indicador de carga justo debajo del último mensaje
                if (uiState.isModelThinking && uiState.chatHistory.isNotEmpty() && uiState.chatHistory.last().isPending.not()) {
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

// --- Componente de Burbuja de Mensaje Mejorado (Más orgánico) ---
@Composable
fun MessageBubble(message: ChatMessage, isUser: Boolean) {
    // Definimos esquinas redondeadas generosas (16.dp)
    val cornerRadius = 16.dp
    val tailRadius = 4.dp

    // Ajustamos la forma para darle un toque orgánico y M3
    val bubbleShape = if (isUser) {
        RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius,
            bottomStart = cornerRadius,
            bottomEnd = tailRadius // Cola a la derecha
        )
    } else {
        RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius,
            bottomStart = tailRadius, // Cola a la izquierda
            bottomEnd = cornerRadius
        )
    }

    // Usamos colores y elevación para M3
    val containerColor = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = bubbleShape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Añadimos una ligera elevación
            modifier = Modifier.widthIn(min = 80.dp, max = 320.dp) // Rango de ancho
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {

                // Si la IA está escribiendo, usa un indicador animado
                if (message.isPending) {
                    Text(
                        text = message.content.ifEmpty { "..." }, // Muestra puntos si el contenido está vacío
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    AnimatedLoadingDots() // Indicador de tipeo
                } else {
                    // --- APLICAMOS MARKDOWN/RICH TEXT SOLO AL MENSAJE DEL ASISTENTE ---
                    if (isUser) {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        // El mensaje del asistente se muestra con RichText y Markdown
                        SelectionContainer {
                            RichText(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(align = Alignment.Top)
                            ) {
                                Markdown(content = message.content)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Componente de Entrada de Texto Mejorado (Estilo Filled M3) ---
@Composable
fun ChatInput(
    currentInput: String,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    isEnabled: Boolean
) {
    // Usamos Surface para una elevación clara en el área de input
    Surface(
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Usamos un TextField regular (Filled) para un estilo más moderno y menos rústico
            TextField(
                value = currentInput,
                onValueChange = onInputChanged,
                placeholder = { Text("Pregunta sobre el módulo...") },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp, max = 150.dp), // Altura mínima y máxima
                shape = RoundedCornerShape(28.dp), // Forma de píldora
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

            // --- CORRECCIÓN: Usamos un FilledIconButton para evitar la ambigüedad del FAB ---
            FilledIconButton(
                onClick = onSend,
                enabled = isEnabled && currentInput.isNotBlank(),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                // Usamos un tamaño fijo para que se vea como un FAB compacto
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Enviar",
                    modifier = Modifier.size(24.dp)
                )
            }
            // --- FIN CORRECCIÓN ---
        }
    }
}

// --- Indicador de Tipeo (Puntos Animados) ---
@Composable
fun AnimatedLoadingDots() {
    val dots = listOf("·", "··", "···")
    // Usamos mutableIntStateOf para evitar el error de invocación Composable
    var dotIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(400)
            dotIndex = (dotIndex + 1) % dots.size
        }
    }
    // Mostramos el texto "Escribiendo" junto con los puntos animados
    Text(
        text = "Escribiendo${dots[dotIndex]}",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Light),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    )
}

// --- Indicador de Carga cuando no hay burbuja pendiente ---
@Composable
fun ThinkingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.widthIn(min = 80.dp, max = 320.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                AnimatedLoadingDots()
            }
        }
    }
}
