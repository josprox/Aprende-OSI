package com.josprox.redesosi.vm

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josprox.redesosi.data.database.SubmoduleEntity
import com.josprox.redesosi.data.network.GroqApiService
import com.josprox.redesosi.data.network.Message
import com.josprox.redesosi.data.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- Nuevo Modelo de Datos para un Mensaje en la UI ---
data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val role: String, // "user" o "model"
    val content: String,
    val isPending: Boolean = false // Si es el mensaje que se está escribiendo
)

// --- Estado de la UI para el Chat ---
data class ChatUiState(
    val chatHistory: List<ChatMessage> = emptyList(),
    val isModelThinking: Boolean = false, // Para mostrar un indicador
    val currentInput: String = "" // Lo que el usuario escribe
)

//=================================================================
// 1. VIEWMODEL
//=================================================================
@HiltViewModel
class ModuleDetailViewModel @Inject constructor(
    private val repository: StudyRepository,
    private val groqApiService: GroqApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Obtenemos el ID del módulo desde el estado guardado
    val moduleId: Int = checkNotNull(savedStateHandle["moduleId"])

    // Este Flow de submódulos está perfecto
    val submodules: Flow<List<SubmoduleEntity>> = repository.getSubmodulesForModule(moduleId)

    // --- Lógica para obtener el título del módulo ---
    private val _moduleTitle = MutableStateFlow("Cargando...")
    val moduleTitle = _moduleTitle.asStateFlow()

    // --- Lógica del Diálogo (Para "Regenerar") ---
    private val _showConfirmDialog = MutableStateFlow(false)
    val showConfirmDialog = _showConfirmDialog.asStateFlow()

    // =========================================================================
    // LÓGICA DEL CHAT
    // =========================================================================

    private val _chatUiState = MutableStateFlow(ChatUiState())
    val chatUiState: StateFlow<ChatUiState> = _chatUiState.asStateFlow()

    // Usaremos un SharedFlow para eventos, como el scroll automático
    private val _scrollToBottomEvent = MutableSharedFlow<Unit>()
    val scrollToBottomEvent: SharedFlow<Unit> = _scrollToBottomEvent.asSharedFlow()

    // Almacenamos el contenido del módulo para el prompt del sistema
    private var moduleContentForChat: String = ""


    init {
        viewModelScope.launch {
            // Buscamos el módulo por su ID para obtener el título y el contenido
            val module = repository.getModuleById(moduleId)
            _moduleTitle.value = module?.title ?: "Detalle del Módulo"

            // CONSTRUIR EL CONTEXTO INICIAL DEL CHAT
            moduleContentForChat = repository.getSubmodulesForModule(moduleId)
                .map { submodules ->
                    // Concatenar los títulos y contenidos de todos los submódulos
                    submodules.joinToString("\n---\n") { "${it.title}\n${it.contentMd}" }
                }
                .firstOrNull() ?: ""

            // Mensaje de Bienvenida Inicial (Solo si el chat está vacío)
            if (moduleContentForChat.isNotBlank() && _chatUiState.value.chatHistory.isEmpty()) {
                _chatUiState.update {
                    it.copy(
                        chatHistory = listOf(
                            ChatMessage(
                                role = "model",
                                content = "¡Hola! Soy tu asistente para el módulo ${_moduleTitle.value}. Puedes preguntarme cualquier duda que tengas sobre el contenido, conceptos o ejemplos. Estoy listo para ayudarte.",
                                isPending = false
                            )
                        )
                    )
                }
            }
        }
    }

    // Función que se llama cuando el usuario escribe
    fun onInputChanged(newInput: String) {
        _chatUiState.update { it.copy(currentInput = newInput) }
    }

    // Función que se llama al enviar el mensaje
    fun onSendMessage() {
        val userMessageText = _chatUiState.value.currentInput.trim()
        if (userMessageText.isBlank() || _chatUiState.value.isModelThinking) return

        // 1. Agregar el mensaje del usuario al historial de la UI
        val userMessage = ChatMessage(role = "user", content = userMessageText)
        _chatUiState.update {
            it.copy(
                chatHistory = it.chatHistory + userMessage,
                currentInput = "",
                isModelThinking = true // Iniciar indicador de carga
            )
        }
        viewModelScope.launch { _scrollToBottomEvent.emit(Unit) } // Scroll a nuevo mensaje

        // 2. Crear el historial completo para enviar a Groq (System + Conversación)
        val systemMessage = buildContextMessage(moduleContentForChat)

        // **CORRECCIÓN DE ROL:**
        // Filtramos el mensaje de bienvenida inicial (id=0) para que la API no falle por rol.
        // También convertimos el historial de la UI (ChatMessage) al modelo de la API (Message).
        val groqHistory = _chatUiState.value.chatHistory
            .filterIndexed { index, chatMessage ->
                // Excluimos el mensaje de bienvenida inicial (el primer mensaje si es del modelo)
                !(index == 0 && chatMessage.role == "model")
            }
            .map { chatMessage ->
                val apiRole = when (chatMessage.role) {
                    "user" -> "user"
                    "model" -> "assistant"
                    else -> "user" // fallback por seguridad
                }
                Message(role = apiRole, content = chatMessage.content)
            }


        // El historial real a enviar (System + Conversación limpia)
        val fullGroqHistory = listOf(systemMessage) + groqHistory

        // 3. Crear el mensaje PENDIENTE de la IA en la UI
        val pendingModelMessage = ChatMessage(
            role = "model",
            content = "",
            isPending = true
        )
        // Agregamos el mensaje pendiente
        _chatUiState.update { it.copy(chatHistory = it.chatHistory + pendingModelMessage) }
        viewModelScope.launch { _scrollToBottomEvent.emit(Unit) } // Scroll a nuevo mensaje

        // 4. Iniciar el streaming con Groq
        viewModelScope.launch {
            try {
                var modelResponseText = ""
                // El índice del mensaje pendiente es siempre el último
                val pendingMessageIndex = _chatUiState.value.chatHistory.lastIndex

                groqApiService.streamChat(fullGroqHistory).collect { chunk ->
                    modelResponseText += chunk

                    // Actualizar el estado con el nuevo texto (sin cambiar 'isPending')
                    _chatUiState.update {
                        val currentHistory = it.chatHistory.toMutableList()
                        // Actualiza el mensaje pendiente por su índice
                        currentHistory[pendingMessageIndex] = pendingModelMessage.copy(content = modelResponseText)
                        it.copy(chatHistory = currentHistory.toList())
                    }
                    _scrollToBottomEvent.emit(Unit) // Scrollear por cada chunk
                }

                // 5. Finalizar el mensaje y detener el indicador
                _chatUiState.update {
                    val finalHistory = it.chatHistory.toMutableList()
                    // Reemplazar el mensaje pendiente por el mensaje finalizado
                    finalHistory[pendingMessageIndex] = pendingModelMessage.copy(
                        content = modelResponseText,
                        isPending = false // ¡Importante! Ya no está pendiente
                    )
                    it.copy(
                        chatHistory = finalHistory.toList(),
                        isModelThinking = false // Detener indicador
                    )
                }

            } catch (e: Exception) {
                Log.e("Chat", "Error en el chat stream: ${e.message}")
                _chatUiState.update {
                    // Manejo de error
                    val errorHistory = it.chatHistory.toMutableList()
                    errorHistory[it.chatHistory.lastIndex] = pendingModelMessage.copy(
                        content = "❌ Lo siento, hubo un error de conexión o API. Inténtalo de nuevo. Detalle: ${e.message}",
                        isPending = false
                    )
                    it.copy(
                        chatHistory = errorHistory.toList(),
                        isModelThinking = false
                    )
                }
            }
        }
    }

    /**
     * Construye el mensaje de rol 'system' que proporciona el contexto del módulo.
     */
    private fun buildContextMessage(content: String): Message {
        val systemPrompt = """
            Eres un asistente de estudio experto en la materia de Redes OSI y protocolos, enfocado en el aprendizaje de estudiantes de nivel licenciatura.
            Tu objetivo es responder las preguntas del usuario basándote **únicamente** en el siguiente contenido del módulo.
            Si la pregunta está fuera del contexto proporcionado, responde amablemente que no tienes información sobre ese tema y que solo puedes ayudar con el contenido del módulo.
            
            CONTENIDO DEL MÓDULO (Tu Fuente Única de Conocimiento):
            ---
            $content
            ---
            
            Tu tono debe ser profesional, claro y conciso.
        """.trimIndent()
        return Message(role = "system", content = systemPrompt)
    }

    // -------------------------------------------------------------------------
    // LÓGICA EXISTENTE (Para Regenerar Preguntas)
    // -------------------------------------------------------------------------

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
