package com.josprox.redesosi.data.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Clase de datos (POJO) que combina un intento de examen (TestAttemptEntity)
 * con el título del módulo correspondiente (ModuleEntity).
 * Esto es útil para mostrar el nombre del módulo en las listas de "Test" y "Calificación".
 */
data class TestAttemptWithModule(
    @Embedded
    val attempt: TestAttemptEntity,

    @ColumnInfo(name = "title") // Nombre de la columna en ModuleEntity
    val moduleTitle: String
)

@Dao
interface StudyDao {

    // --- Inserts (Contenido de Estudio) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModules(modules: List<ModuleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmodules(submodules: List<SubmoduleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    // --- Inserts y Updates (Progreso del Usuario) ---

    /**
     * Inserta un nuevo intento de examen y devuelve su ID único (Long).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestAttempt(attempt: TestAttemptEntity): Long

    /**
     * Actualiza un intento de examen existente (ej. para marcarlo como "COMPLETED").
     */
    @Update
    suspend fun updateTestAttempt(attempt: TestAttemptEntity)

    /**
     * Inserta la respuesta específica de un usuario a una pregunta.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAnswer(answer: UserAnswerEntity)


    // --- Queries (Contenido de Estudio) ---
    @Query("SELECT * FROM subjects")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM modules WHERE subjectId = :subjectId")
    fun getModulesForSubject(subjectId: Int): Flow<List<ModuleEntity>>

    @Query("SELECT * FROM submodules WHERE moduleId = :moduleId ORDER BY id ASC")
    fun getSubmodulesForModule(moduleId: Int): Flow<List<SubmoduleEntity>>

    @Query("SELECT * FROM questions WHERE moduleId = :moduleId")
    suspend fun getQuestionsForModule(moduleId: Int): List<QuestionEntity>

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun getSubjectCount(): Int

    // Borra todas las preguntas asociadas a un módulo específico
    @Query("DELETE FROM questions WHERE moduleId = :moduleId")
    suspend fun deleteQuestionsForModule(moduleId: Int)


    // --- Queries (Progreso del Usuario) ---

    /**
     * Obtiene todos los intentos PENDIENTES, unidos con el nombre del módulo.
     * Para la pantalla "Test".
     */
    @Query("""
        SELECT t.*, m.title 
        FROM test_attempts as t 
        INNER JOIN modules as m ON t.moduleId = m.id 
        WHERE t.status = 'PENDING' 
        ORDER BY t.timestamp DESC
    """)
    fun getPendingTestsWithModule(): Flow<List<TestAttemptWithModule>>

    /**
     * Obtiene todos los intentos COMPLETADOS, unidos con el nombre del módulo.
     * Para la pantalla "Calificación".
     */
    @Query("""
        SELECT t.*, m.title 
        FROM test_attempts as t 
        INNER JOIN modules as m ON t.moduleId = m.id 
        WHERE t.status = 'COMPLETED' 
        ORDER BY t.timestamp DESC
    """)
    fun getCompletedTestsWithModule(): Flow<List<TestAttemptWithModule>>

    /**
     * Obtiene todas las respuestas de un intento específico.
     * (Útil para la pantalla de revisión o para resumir un test).
     */
    @Query("SELECT * FROM user_answers WHERE testAttemptId = :testAttemptId")
    suspend fun getUserAnswersForAttempt(testAttemptId: Long): List<UserAnswerEntity>

    /**
     * Busca un intento pendiente para un módulo específico.
     * (Útil para saber si resumir un test o empezar uno nuevo).
     */
    @Query("SELECT * FROM test_attempts WHERE moduleId = :moduleId AND status = 'PENDING' LIMIT 1")
    suspend fun getPendingTestForModule(moduleId: Int): TestAttemptEntity?

    /**
     * Obtiene un intento de examen específico por su ID.
     */
    @Query("SELECT * FROM test_attempts WHERE id = :attemptId LIMIT 1") // <-- AÑADE ESTA FUNCIÓN
    suspend fun getTestAttemptById(attemptId: Long): TestAttemptEntity?

    /**
     * Obtiene las preguntas de un módulo en su orden original (por ID).
     * Esto es crucial para que el 'currentQuestionIndex' siempre coincida.
     */
    @Query("SELECT * FROM questions WHERE moduleId = :moduleId ORDER BY id ASC") // <-- AÑADE ESTA FUNCIÓN
    suspend fun getOriginalQuestionsForModule(moduleId: Int): List<QuestionEntity>

    /**
     * Obtiene un módulo específico por su ID.
     */
    @Query("SELECT * FROM modules WHERE id = :moduleId LIMIT 1")
    suspend fun getModuleById(moduleId: Int): ModuleEntity?

    /**
     * Borra TODOS los intentos de examen (pendientes y completados)
     * asociados a un módulo.
     */
    @Query("DELETE FROM test_attempts WHERE moduleId = :moduleId")
    suspend fun deleteAttemptsForModule(moduleId: Int)

}