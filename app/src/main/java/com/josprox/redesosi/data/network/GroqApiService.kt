package com.josprox.redesosi.data.network

import android.util.Log
import com.josprox.redesosi.BuildConfig
import com.josprox.redesosi.data.database.QuestionEntity
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.dotenv.vault.dotenvVault

// --- Modelos de datos ---
@Serializable
data class GroqRequest(
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

// --- Modelo para el JSON que esperamos de la IA ---
@Serializable
data class QuizPayload(val questions: List<QuizQuestion>)

@Serializable
data class QuizQuestion(
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String, // <-- CAMBIO 1: Opción D añadida
    val correctAnswer: String
)

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

    suspend fun generateQuestions(moduleContent: String, moduleId: Int): List<QuestionEntity> {
        val dotenv = dotenvVault(BuildConfig.DOTENV_KEY) {
            directory = "/assets"
            filename = "env.vault" // instead of '.env', use 'env'
        }
        val apiKey = dotenv["GROQ_CLOUD_API"]
        val modelGroq = dotenv["GROQ_CLOUD_MODEL"]

        if (apiKey.isEmpty() || apiKey == "TU_API_KEY_AQUI") {
            Log.e("GroqApiService", "API Key de Groq no encontrada o inválida.")
            return emptyList()
        }

        // --- CAMBIO 2: Prompt "Nivel EGEL" mejorado ---
        val prompt = """
            ACTÚA COMO UN EXPERTO DISEÑADOR DE EXÁMENES DE CERTIFICACIÓN (EGEL, CCNA).
            Tu objetivo es crear un banco de preguntas de alta dificultad para un examen de nivel licenciatura, basado estrictamente en el siguiente contenido: "$moduleContent".

            REGLAS CRÍTICAS PARA LAS PREGUNTAS:
            1.  **Formato:** Genera entre 15 y 30 preguntas de opción múltiple. (Prioriza calidad sobre cantidad).
            2.  **Opciones:** 4 opciones de respuesta (A, B, C, D).
            3.  **Complejidad (Nivel Licenciatura/EGEL):**
                * Las preguntas deben forzar el **ANÁLISIS**, la **APLICACIÓN** o la **COMPARACIÓN** de conceptos, no la simple memorización.
                * **Distractores Plausibles (CRÍTICO):** Las 3 opciones incorrectas deben ser *altamente plausibles*, *sutiles* y *conceptualmente muy cercanas* a la respuesta correcta. Evita opciones obviamente incorrectas o absurdas. El objetivo es hacer dudar a un estudiante avanzado.
            4.  **Tipos de Pregunta Preferidos:**
                * **Escenario:** "Dada esta situación/problema, ¿qué capa/protocolo es responsable?"
                * **Comparativas:** "¿Cuál es la diferencia *clave* entre el Protocolo X y el Protocolo Y en el contexto de...?"
                * **Diagnóstico:** "Un usuario experimenta [PROBLEMA]. ¿En qué capa es más probable que resida la falla?"

            FORMATO DE SALIDA OBLIGATORIO:
            Responde *únicamente* con el objeto JSON. No incluyas texto introductorio, saludos, explicaciones ni markdown. La estructura exacta es:
            {
              "questions": [
                {
                  "questionText": "Texto de la pregunta...",
                  "optionA": "Opción A (distractor plausible)",
                  "optionB": "Opción B (distractor plausible)",
                  "optionC": "Opción C (respuesta correcta)",
                  "optionD": "Opción D (distractor plausible)",
                  "correctAnswer": "C"
                }
              ]
            }
        """.trimIndent()

        val request = GroqRequest(
            messages = listOf(Message("user", prompt)),
            model = modelGroq
        )

        return try {
            // --- 1️⃣ Obtener respuesta cruda como texto para depuración ---
            val rawResponse = client.post("https://api.groq.com/openai/v1/chat/completions") {
                bearerAuth(apiKey)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.bodyAsText()

            Log.d("GroqApiService", "📩 Respuesta cruda de Groq: $rawResponse")

            // --- CAMBIO 3: Lógica 'try/catch' MEJORADA para evitar falsos positivos ---

            // --- 2️⃣ Decodificar la respuesta (Intento de Éxito) ---
            val response: GroqResponse
            try {
                // Intentamos decodificar como una respuesta de ÉXITO
                response = jsonParser.decodeFromString<GroqResponse>(rawResponse)
            } catch (e: Exception) {
                // --- 3️⃣ Si falla, ASUMIMOS que es un ERROR de API ---
                Log.w("GroqApiService", "No se pudo decodificar como GroqResponse (éxito), intentando como GroqError. Razón: ${e.message}")
                try {
                    // Ahora sí, intentamos decodificar como un objeto de ERROR
                    val errorObj = jsonParser.decodeFromString<GroqError>(rawResponse)
                    Log.e("GroqApiService", "❌ Error REAL desde Groq API: ${errorObj.error?.message}")
                } catch (parseError: Exception) {
                    // Si falla AMBOS, la respuesta es irreconocible
                    Log.e("GroqApiService", "💥 Error CRÍTICO: No se pudo decodificar la respuesta ni como Éxito ni como Error: ${parseError.message}")
                }
                return emptyList()
            }

            // --- 4️⃣ Si llegamos aquí, 'response' es un objeto GroqResponse válido ---
            val jsonContent = response.choices?.firstOrNull()?.message?.content
            if (jsonContent == null) {
                Log.e("GroqApiService", "⚠️ No se recibió contenido en 'choices'.")
                return emptyList()
            }

            Log.d("GroqApiService", "🧠 Contenido recibido del modelo: $jsonContent")

            // --- 5️⃣ Intentar decodificar el JSON generado por el modelo ---
            val quizPayload = try {
                jsonParser.decodeFromString<QuizPayload>(jsonContent)
            } catch (parseError: Exception) {
                Log.e("GroqApiService", "❗ Error al parsear el JSON generado por el modelo: ${parseError.message}")
                return emptyList()
            }

            // --- 6️⃣ Convertir a entidades ---
            val questions = quizPayload.questions.map {
                QuestionEntity(
                    moduleId = moduleId,
                    questionText = it.questionText,
                    optionA = it.optionA,
                    optionB = it.optionB,
                    optionC = it.optionC,
                    optionD = it.optionD, // <-- CAMBIO 4: Mapeo de optionD
                    correctAnswer = it.correctAnswer
                )
            }

            Log.d("GroqApiService", "✅ ${questions.size} preguntas generadas correctamente.")
            questions

        } catch (e: Exception) {
            Log.e("GroqApiService", "💥 Error al generar preguntas: ${e.message}", e)
            emptyList()
        }
    }
}