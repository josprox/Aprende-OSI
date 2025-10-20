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

// ✅ No necesitas cambiar tus data class.
// La respuesta de Groq tiene más campos, pero ignoreUnknownKeys=true los omitirá.
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
    val correctAnswer: String
)

class GroqApiService {

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        // isLenient = true // Puedes descomentar esto si la IA genera JSON inválido (ej. comas extra)
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            // ✅ --- 2. USA EL MISMO PARSER AQUÍ ---
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

        val prompt = """
            Basado en el siguiente contenido sobre redes de computadoras, genera mínimo 5 preguntas de opción múltiple (A, B, C), máximo 50 preguntas.
            El contenido es: "$moduleContent".
            
            Debes devolver la respuesta únicamente en formato JSON, con la siguiente estructura:
            {
              "questions": [
                {
                  "questionText": "Texto de la pregunta...",
                  "optionA": "Opción A",
                  "optionB": "Opción B",
                  "optionC": "Opción C",
                  "correctAnswer": "A"
                }
              ]
            }
            Asegúrate de que la respuesta sea solo el objeto JSON, sin texto adicional ni markdown.
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

            // --- 2️⃣ Si contiene error, loguéalo y detén ---
            if ("error" in rawResponse.lowercase()) {
                // ✅ --- 3. USA EL PARSER CONFIGURADO ---
                val errorObj = jsonParser.decodeFromString<GroqError>(rawResponse)
                Log.e("GroqApiService", "❌ Error desde Groq API: ${errorObj.error?.message}")
                return emptyList()
            }

            // --- 3️⃣ Decodificar la respuesta ---
            // ✅ --- 4. USA EL PARSER CONFIGURADO ---
            // Aquí es donde ocurría tu error. Ahora usará la instancia correcta.
            val response = jsonParser.decodeFromString<GroqResponse>(rawResponse)

            val jsonContent = response.choices?.firstOrNull()?.message?.content
            if (jsonContent == null) {
                Log.e("GroqApiService", "⚠️ No se recibió contenido en 'choices'.")
                return emptyList()
            }

            Log.d("GroqApiService", "🧠 Contenido recibido del modelo: $jsonContent")

            // --- 4️⃣ Intentar decodificar el JSON generado por el modelo ---
            val quizPayload = try {
                // ✅ --- 5. USA EL PARSER CONFIGURADO (también aquí) ---
                jsonParser.decodeFromString<QuizPayload>(jsonContent)
            } catch (parseError: Exception) {
                Log.e("GroqApiService", "❗ Error al parsear el JSON generado por el modelo: ${parseError.message}")
                return emptyList()
            }

            // --- 5️⃣ Convertir a entidades ---
            val questions = quizPayload.questions.map {
                QuestionEntity(
                    moduleId = moduleId,
                    questionText = it.questionText,
                    optionA = it.optionA,
                    optionB = it.optionB,
                    optionC = it.optionC,
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