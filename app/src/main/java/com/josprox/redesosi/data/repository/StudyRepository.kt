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
    @Transaction // <-- Esto asegura que si algo falla, no se guarde nada (full o nada)
    suspend fun importSubjectFromJson(jsonString: String) {
        // 1. Parsear el JSON a nuestras clases DTO
        val subjectImport = Json.decodeFromString<SubjectImport>(jsonString)

        // 2. Insertar la materia principal y obtener su nuevo ID
        val newSubjectId = studyDao.insertSubject(
            SubjectEntity(
                id = 0, // Room generará el ID
                name = subjectImport.name,
                author = subjectImport.author, // <-- AÑADIDO
                version = subjectImport.version // <-- AÑADIDO
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

    // --- ¡NUEVA FUNCIÓN DE ACTUALIZACIÓN! ---

    /**
     * Actualiza una materia existente desde un JSON, preservando el progreso.
     * Compara el nuevo JSON con los datos existentes en la BD.
     *
     * - Si un módulo o submódulo existe (por título), actualiza su contenido.
     * - Si un módulo o submódulo es nuevo en el JSON, lo inserta.
     * - Si un módulo o submódulo existe en la BD pero no en el JSON, lo borra.
     * - Si el contenido de un módulo cambia, borra sus preguntas y
     * exámenes PENDIENTES para forzar la regeneración.
     * - Los exámenes COMPLETADOS se conservan.
     *
     * @param subjectId El ID de la materia (SubjectEntity) que se va a actualizar.
     * @param jsonString El nuevo contenido del curso en formato JSON.
     */
    @Transaction
    suspend fun updateSubjectFromJson(subjectId: Int, jsonString: String) {
        // 1. Parsear el JSON
        val subjectImport = Json.decodeFromString<SubjectImport>(jsonString)

        // 2. Actualizar la entidad Subject principal
        // Creamos una nueva entidad con el ID existente y los datos nuevos
        studyDao.updateSubject(
            SubjectEntity(
                id = subjectId,
                name = subjectImport.name,
                author = subjectImport.author, // <-- AÑADIDO
                version = subjectImport.version // <-- AÑADIDO
            )
        )

        // 3. Obtener listas de módulos (los nuevos del JSON y los viejos de la BD)
        val newModules = subjectImport.modules
        val oldModules = studyDao.getModulesForSubject(subjectId).first()

        // 4. Crear "Mapas" para compararlos fácilmente por su título
        val newModulesMap = newModules.associateBy { it.title }
        val oldModulesMap = oldModules.associateBy { it.title }

        // --- INICIO DEL "MERGE" DE MÓDULOS ---

        // 5. Recorrer los MÓDULOS NUEVOS (del JSON) para Insertar o Actualizar
        for (newModule in newModules) {
            val oldModule = oldModulesMap[newModule.title]

            if (oldModule == null) {
                // --- CASO A: MÓDULO NUEVO (Insertar) ---
                Log.d("RepoUpdate", "Insertando nuevo módulo: ${newModule.title}")

                // Inserta el módulo y obtiene su nuevo ID
                val newModuleId = studyDao.insertModule(
                    ModuleEntity(
                        id = 0, // Room generará el ID
                        subjectId = subjectId,
                        title = newModule.title,
                        shortDescription = newModule.shortDescription
                    )
                )

                // Inserta todos sus submódulos
                val newSubmodules = newModule.submodules.map { subImport ->
                    SubmoduleEntity(
                        id = 0,
                        moduleId = newModuleId.toInt(),
                        title = subImport.title,
                        contentMd = subImport.contentMd
                    )
                }
                studyDao.insertSubmodules(newSubmodules)

            } else {
                // --- CASO B: MÓDULO EXISTENTE (Actualizar) ---
                Log.d("RepoUpdate", "Actualizando módulo existente: ${newModule.title}")
                val moduleId = oldModule.id

                // Actualiza la descripción corta del módulo
                studyDao.updateModule(oldModule.copy(shortDescription = newModule.shortDescription))

                // Ahora, debemos hacer el mismo "merge" con los SUBMÓDULOS
                var contentChanged = false // Bandera para saber si borramos las preguntas

                val newSubmodules = newModule.submodules
                val oldSubmodules = studyDao.getSubmodulesForModule(moduleId).first()
                val newSubmodulesMap = newSubmodules.associateBy { it.title }
                val oldSubmodulesMap = oldSubmodules.associateBy { it.title }

                // B.1: Recorrer submódulos nuevos (Insertar/Actualizar)
                for (newSub in newSubmodules) {
                    val oldSub = oldSubmodulesMap[newSub.title]

                    if (oldSub == null) {
                        // Submódulo nuevo, insertarlo
                        Log.d("RepoUpdate", " -> Insertando submódulo: ${newSub.title}")
                        studyDao.insertSubmodule(
                            SubmoduleEntity(
                                id = 0,
                                moduleId = moduleId,
                                title = newSub.title,
                                contentMd = newSub.contentMd
                            )
                        )
                        contentChanged = true
                    } else {
                        // Submódulo existente, comparar contenido
                        if (oldSub.contentMd != newSub.contentMd) {
                            Log.d("RepoUpdate", " -> Actualizando submódulo: ${newSub.title}")
                            studyDao.updateSubmodule(oldSub.copy(contentMd = newSub.contentMd))
                            contentChanged = true
                        }
                    }
                }

                // B.2: Recorrer submódulos viejos (Borrar)
                for (oldSub in oldSubmodules) {
                    if (!newSubmodulesMap.containsKey(oldSub.title)) {
                        // Este submódulo fue eliminado en el JSON
                        Log.d("RepoUpdate", " -> Borrando submódulo: ${oldSub.title}")
                        studyDao.deleteSubmoduleById(oldSub.id)
                        contentChanged = true
                    }
                }

                // B.3: Limpiar preguntas si el contenido cambió
                if (contentChanged) {
                    Log.i(
                        "RepoUpdate",
                        "El contenido del módulo ${oldModule.title} cambió. " +
                                "Borrando preguntas viejas e intentos PENDIENTES."
                    )
                    // Borra preguntas viejas (se regenerarán la próxima vez)
                    studyDao.deleteQuestionsForModule(moduleId)

                    // Borra intentos PENDIENTES (preserva los COMPLETADOS)
                    studyDao.deletePendingAttemptsForModule(moduleId)
                }
            }
        }

        // 6. Recorrer los MÓDULOS VIEJOS (de la BD) para Borrar los que ya no existen
        for (oldModule in oldModules) {
            if (!newModulesMap.containsKey(oldModule.title)) {
                // --- CASO C: MÓDULO ELIMINADO (Borrar) ---
                // Este módulo estaba en la BD pero ya no viene en el JSON
                Log.w("RepoUpdate", "Borrando módulo obsoleto: ${oldModule.title}")

                // Al borrar el módulo, 'onDelete = CASCADE' se encargará
                // de borrar todos sus submódulos, preguntas, e intentos
                // (tanto pendientes como completados). Esto es correcto,
                // ya que el módulo ya no existe.
                studyDao.deleteModuleById(oldModule.id)
            }
        }

        Log.i("RepoUpdate", "¡Actualización de materia (ID: $subjectId) completada!")
    }
}