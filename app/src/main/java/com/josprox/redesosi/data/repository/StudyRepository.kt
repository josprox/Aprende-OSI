package com.josprox.redesosi.data.repository

import android.util.Log
import com.josprox.redesosi.data.database.ModuleEntity
import com.josprox.redesosi.data.database.QuestionEntity
import com.josprox.redesosi.data.database.StudyDao
import com.josprox.redesosi.data.database.SubmoduleEntity
import com.josprox.redesosi.data.network.GroqApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import com.josprox.redesosi.data.database.TestAttemptEntity
import com.josprox.redesosi.data.database.UserAnswerEntity
import kotlinx.coroutines.flow.first // Asegúrate de tener este import
import androidx.room.Transaction
import com.josprox.redesosi.data.SubjectImport
import com.josprox.redesosi.data.database.SubjectEntity
import kotlinx.serialization.json.Json


@Singleton
class StudyRepository @Inject constructor(
    private val studyDao: StudyDao,
    private val groqApiService: GroqApiService
) {
    // Se mantienen los nombres de tus funciones
    fun getAllSubjects() = studyDao.getAllSubjects()
    fun getModulesForSubject(subjectId: Int): Flow<List<ModuleEntity>> = studyDao.getModulesForSubject(subjectId)
    fun getSubmodulesForModule(moduleId: Int): Flow<List<SubmoduleEntity>> = studyDao.getSubmodulesForModule(moduleId)

    /**
     * Obtiene las preguntas de la BD. Si no existen, las genera con la API y las guarda.
     * Esta versión NO borra las preguntas si ya existen.
     */
    suspend fun getOrCreateQuestionsForModule(moduleId: Int): List<QuestionEntity> {
        // 1. Intenta obtener preguntas existentes (usando la nueva función DAO ordenada)
        var questions = studyDao.getOriginalQuestionsForModule(moduleId)

        // 2. Si no hay, y solo si no hay, las genera.
        if (questions.isEmpty()) {
            Log.d("StudyRepository", "No hay preguntas para $moduleId. Generando nuevas con la API.")

            // 3. Llama al servicio para obtener las preguntas
            val submodules = studyDao.getSubmodulesForModule(moduleId).first()
            val content = submodules.joinToString("\n\n") { "## ${it.title}\n${it.contentMd}" }

            if (content.isBlank()) {
                Log.w("StudyRepository", "El contenido para generar preguntas está vacío para el módulo $moduleId.")
                return emptyList()
            }

            val newQuestions = groqApiService.generateQuestions(content, moduleId)
            if (newQuestions.isNotEmpty()) {
                studyDao.insertQuestions(newQuestions)
                Log.d("StudyRepository", "Nuevas ${newQuestions.size} preguntas guardadas en la BD.")
                questions = studyDao.getOriginalQuestionsForModule(moduleId)
            }
        } else {
            Log.d("StudyRepository", "Usando ${questions.size} preguntas existentes de la BD para $moduleId.")
        }
        return questions
    }

    // Flujo de intentos completados para la pantalla de "Calificación"
    fun getCompletedTests() = studyDao.getCompletedTestsWithModule()

    // Flujo de intentos pendientes para la pantalla de "Test"
    fun getPendingTests() = studyDao.getPendingTestsWithModule()

    // --- Estas funciones las usarás desde tu QuizViewModel (el que hace el examen) ---

    suspend fun createTestAttempt(moduleId: Int, totalQuestions: Int): Long {
        val newAttempt = TestAttemptEntity(
            moduleId = moduleId,
            status = "PENDING",
            totalQuestions = totalQuestions,
            currentQuestionIndex = 0 // <-- Asegúrate de incluir esto
        )
        return studyDao.insertTestAttempt(newAttempt)
    }

    // Para finalizar un intento y guardar la calificación
    suspend fun finishTestAttempt(attempt: TestAttemptEntity) {
        studyDao.updateTestAttempt(attempt)
    }

    // Para buscar un test pendiente y resumirlo
    suspend fun findPendingTest(moduleId: Int): TestAttemptEntity? {
        return studyDao.getPendingTestForModule(moduleId)
    }

    //--------
    suspend fun getTestAttemptById(attemptId: Long): TestAttemptEntity? {
        return studyDao.getTestAttemptById(attemptId)
    }

    suspend fun getUserAnswersForAttempt(attemptId: Long): List<UserAnswerEntity> {
        return studyDao.getUserAnswersForAttempt(attemptId)
    }

    suspend fun saveUserAnswer(answer: UserAnswerEntity) {
        studyDao.insertUserAnswer(answer)
    }

    /**
     * Actualiza un intento de examen en la BD.
     * Lo usaremos tanto para guardar el progreso (PENDING)
     * como para marcarlo como finalizado (COMPLETED).
     */
    suspend fun updateTestAttempt(attempt: TestAttemptEntity) {
        studyDao.updateTestAttempt(attempt)
    }

    suspend fun getModuleById(moduleId: Int): ModuleEntity? {
        return studyDao.getModuleById(moduleId)
    }

    suspend fun getOriginalQuestionsForModule(moduleId: Int): List<QuestionEntity> {
        return studyDao.getOriginalQuestionsForModule(moduleId)
    }

    /**
     * Borra permanentemente todas las preguntas, intentos y respuestas
     * de un módulo específico para forzar la regeneración.
     */
    suspend fun forceRegenerateQuestions(moduleId: Int) {
        // 1. Borra todos los intentos (completados y pendientes) y sus respuestas.
        studyDao.deleteAttemptsForModule(moduleId)

        // 2. Borra las preguntas viejas.
        studyDao.deleteQuestionsForModule(moduleId)

        // 3. ¡Listo! La próxima vez que QuizViewModel llame a
        // getOrCreateQuestionsForModule(), las volverá a generar.
    }

    /**
     * Parsea un string JSON, valida la estructura e importa la nueva materia
     * a la base de datos de forma transaccional.
     */
    @Transaction // <-- Esto asegura que si algo falla, no se guarde nada (todo o nada)
    suspend fun importSubjectFromJson(jsonString: String) {
        // 1. Parsear el JSON a nuestras clases DTO
        val subjectImport = Json.decodeFromString<SubjectImport>(jsonString)

        // 2. Insertar la materia principal y obtener su nuevo ID
        val newSubjectId = studyDao.insertSubject(
            SubjectEntity(
                id = 0, // Room generará el ID
                name = subjectImport.name
            )
        )

        // 3. Iterar sobre los módulos, insertarlos y obtener sus IDs
        subjectImport.modules.forEach { moduleImport ->
            val newModuleId = studyDao.insertModule(
                ModuleEntity(
                    id = 0,
                    subjectId = newSubjectId.toInt(), // Usamos el ID de la materia padre
                    title = moduleImport.title,
                    shortDescription = moduleImport.shortDescription
                )
            )

            // 4. Iterar sobre los submódulos e insertarlos
            val submoduleEntities = moduleImport.submodules.map { submoduleImport ->
                SubmoduleEntity(
                    id = 0,
                    moduleId = newModuleId.toInt(), // Usamos el ID del módulo padre
                    title = submoduleImport.title,
                    contentMd = submoduleImport.contentMd
                )
            }
            // (Usamos la función plural que ya tenías para eficiencia)
            studyDao.insertSubmodules(submoduleEntities)
        }
    }

    /**
     * Borra una materia y  su contenido asociado (módulos, preguntas, historial).
     */
    suspend fun deleteSubject(subjectId: Int) {
        studyDao.deleteSubjectById(subjectId)
    }
}

