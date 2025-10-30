package com.josprox.redesosi.data.network

// Imports para streaming
import android.util.Log
import com.josprox.redesosi.BuildConfig
import com.josprox.redesosi.data.database.QuestionEntity
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.cancel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.dotenv.vault.dotenvVault
import kotlin.coroutines.coroutineContext

// --- Modelos de datos Existentes ---
@Serializable
data class GroqRequest(
// ... (Modelos de datos restantes)
    val messages: List<Message>,
    val model: String,
    val temperature: Double = 0.7,
    val max_tokens: Int = 2048,
    val top_p: Double = 1.0,
    val stream: Boolean = false,
    val response_format: ResponseFormat = ResponseFormat("json_object")
)

@Serializable
data class Message(val role: String, val content: String)

@Serializable
data class ResponseFormat(val type: String)

@Serializable
data class GroqResponse(val choices: List<Choice>? = null)

@Serializable
data class Choice(val message: Message)

@Serializable
data class GroqError(val error: GroqErrorDetail? = null)

@Serializable
data class GroqErrorDetail(val message: String? = null, val type: String? = null)

// --- Modelo para el JSON que esperamos de la IA (Quiz) ---
@Serializable
data class QuizPayload(val questions: List<QuizQuestion>)

@Serializable
data class QuizQuestion(
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String,
    val explanationText: String
)

// --- Modelos Específicos para Streaming ---
@Serializable
data class StreamResponse(
    val id: String,
    val choices: List<StreamChoice>? = null,
    val created: Long,
    val model: String,
    val system_fingerprint: String? = null,
    val `object`: String
)

@Serializable
data class StreamChoice(
    val index: Int,
    val delta: StreamDelta,
    val logprobs: String? = null,
    val finish_reason: String? = null
)

@Serializable
data class StreamDelta(
    val content: String? = null,
    val role: String? = null
)
// --------------------------------------------------------

class GroqApiService {

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(jsonParser)
        }
    }

    // -------------------------------------------------------------------------
    // TU FUNCIÓN EXISTENTE DE generateQuestions
    // -------------------------------------------------------------------------
    suspend fun generateQuestions(moduleContent: String, moduleId: Int): List<QuestionEntity> {
        val dotenv = dotenvVault(BuildConfig.DOTENV_KEY) {
            directory = "/assets"
            filename = "env.vault"
        }
        val apiKey = dotenv["GROQ_CLOUD_API"]
        val modelGroq = dotenv["GROQ_CLOUD_MODEL"]

        if (apiKey.isNullOrEmpty() || apiKey == "TU_API_KEY_AQUI") {
            Log.e("GroqApiService", "API Key de Groq no encontrada o inválida.")
            return emptyList()
        }

        val prompt = """
            ACTÚA COMO UN EXPERTO DISEÑADOR DE EXÁMENES DE CERTIFICACIÓN (EGEL, CCNA).
            Tu objetivo es crear un banco de preguntas de alta dificultad para un examen de nivel licenciatura, basado estrictamente en el siguiente contenido: "$moduleContent".

            REGLAS CRÍTICAS PARA LAS PREGUNTAS:
            1.  **Formato:** Genera entre 15 y 30 preguntas de opción múltiple. (Prioriza calidad sobre cantidad).
            2.  **Opciones:** 4 opciones de respuesta (A, B, C, D).
            3.  **Complejidad (Nivel Licenciatura/EGEL):** Las preguntas deben forzar el ANÁLISIS, la APLICACIÓN o la COMPARACIÓN de conceptos.
            4.  **Explicación (CRÍTICO):** Para cada pregunta, debes incluir un campo 'explanationText' que justifique de forma concisa (máx. 2 frases) POR QUÉ la 'correctAnswer' es la correcta, basándose explícitamente en el contenido proporcionado.
            
            FORMATO DE SALIDA OBLIGATORIO:
            Responde *únicamente* con el objeto JSON. No incluyas texto introductorio. La estructura exacta es:
            {
                "questions": [
                    {
                       "questionText": "Texto de la pregunta...",
                       "optionA": "Opción A (distractor plausible)",
                       "optionB": "Opción B (distractor plausible)",
                       "optionC": "Opción C (respuesta correcta)",
                       "optionD": "Opción D (distractor plausible)",
                       "correctAnswer": "C",
                       "explanationText": "Correcto, el concepto X es fundamental en la capa Y porque..." // <--- ¡NUEVA ESTRUCTURA!
                    }
                ]
            }
        """.trimIndent()

        val request = GroqRequest(
            messages = listOf(Message("user", prompt)),
            model = modelGroq,
            response_format = ResponseFormat("json_object")
        )

        return try {
            val rawResponse = client.post("https://api.groq.com/openai/v1/chat/completions") {
                bearerAuth(apiKey)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.bodyAsText()

            Log.d("GroqApiService", "📩 Respuesta cruda de Groq: $rawResponse")

            val response: GroqResponse
            try {
                response = jsonParser.decodeFromString<GroqResponse>(rawResponse)
            } catch (e: Exception) {
                Log.w("GroqApiService", "No se pudo decodificar como GroqResponse (éxito), intentando como GroqError. Razón: ${e.message}")
                try {
                    val errorObj = jsonParser.decodeFromString<GroqError>(rawResponse)
                    Log.e("GroqApiService", "❌ Error REAL desde Groq API: ${errorObj.error?.message}")
                } catch (parseError: Exception) {
                    Log.e("GroqApiService", "💥 Error CRÍTICO: No se pudo decodificar la respuesta ni como Éxito ni como Error: ${parseError.message}")
                }
                return emptyList()
            }

            val jsonContent = response.choices?.firstOrNull()?.message?.content
            if (jsonContent == null) {
                Log.e("GroqApiService", "⚠️ No se recibió contenido en 'choices'.")
                return emptyList()
            }

            Log.d("GroqApiService", "🧠 Contenido recibido del modelo: $jsonContent")

            val quizPayload = try {
                jsonParser.decodeFromString<QuizPayload>(jsonContent)
            } catch (parseError: Exception) {
                Log.e("GroqApiService", "❗ Error al parsear el JSON generado por el modelo: ${parseError.message}")
                return emptyList()
            }

            val questions = quizPayload.questions.map {
                QuestionEntity(
                    moduleId = moduleId,
                    questionText = it.questionText,
                    optionA = it.optionA,
                    optionB = it.optionB,
                    optionC = it.optionC,
                    optionD = it.optionD,
                    correctAnswer = it.correctAnswer,
                    explanationText = it.explanationText
                )
            }

            Log.d("GroqApiService", "✅ ${questions.size} preguntas generadas correctamente.")
            questions

        } catch (e: Exception) {
            Log.e("GroqApiService", "💥 Error al generar preguntas: ${e.message}", e)
            emptyList()
        }
    }
    // -------------------------------------------------------------------------
    // FUNCIÓN DE CHAT CON STREAMING
    // -------------------------------------------------------------------------
    fun streamChat(chatHistory: List<Message>): Flow<String> = flow {
        val dotenv = dotenvVault(BuildConfig.DOTENV_KEY) {
            directory = "/assets"
            filename = "env.vault"
        }
        val apiKey = dotenv["GROQ_CLOUD_API"]
        val modelGroq = dotenv["GROQ_CLOUD_MODEL"]

        if (apiKey.isNullOrEmpty() || apiKey == "TU_API_KEY_AQUI") {
            Log.e("GroqApiService", "API Key de Groq no encontrada o inválida.")
            emit("Error: Clave de API de Groq no configurada.")
            return@flow
        }

        // Crear la solicitud de Groq para streaming
        val request = GroqRequest(
            messages = chatHistory,
            model = modelGroq,
            stream = true,
            response_format = ResponseFormat("text")
        )

        // Ejecutar la solicitud como un POST preparado (streaming)
        client.preparePost("https://api.groq.com/openai/v1/chat/completions") {
            bearerAuth(apiKey)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.execute { httpResponse ->
            // Manejar la respuesta del servidor
            if (httpResponse.status.value !in 200..299) {
                val errorBody = httpResponse.bodyAsText()
                Log.e("GroqApiService", "❌ Error de Groq HTTP ${httpResponse.status}: $errorBody")
                emit("Error del servidor (${httpResponse.status}): $errorBody")
                return@execute
            }

            // Leer el stream de datos
            val channel = httpResponse.body<io.ktor.utils.io.ByteReadChannel>()
            try {
                while (!channel.isClosedForRead && coroutineContext.isActive) {
                    val line = channel.readUTF8Line() // Línea 266 (ahora debería funcionar)
                    if (line.isNullOrBlank()) continue

                    // Las líneas del stream empiezan con "data: "
                    if (line.startsWith("data: ")) {
                        val jsonString = line.substring(6)
                        if (jsonString == "[DONE]") break // Final del stream

                        try {
                            // Decodificar el fragmento del stream
                            val streamResponse = jsonParser.decodeFromString<StreamResponse>(jsonString)
                            val content = streamResponse.choices?.firstOrNull()?.delta?.content

                            // Emitir el contenido si existe
                            if (!content.isNullOrBlank()) {
                                emit(content)
                            }
                        } catch (e: Exception) {
                            Log.e("GroqApiService", "Error parseando línea del stream: $jsonString. Error: ${e.message}")
                        }
                    }
                }
            } finally {
                // Aseguramos que el canal se cierre al finalizar o fallar
                channel.cancel()
            }
        }
    }
}