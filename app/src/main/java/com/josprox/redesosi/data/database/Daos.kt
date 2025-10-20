package com.josprox.redesosi.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {
    // --- Inserts ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModules(modules: List<ModuleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmodules(submodules: List<SubmoduleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    // --- Queries ---
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
}
