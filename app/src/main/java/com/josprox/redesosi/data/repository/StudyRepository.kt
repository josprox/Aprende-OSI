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


@Singleton
class StudyRepository @Inject constructor(
    private val studyDao: StudyDao,
    private val groqApiService: GroqApiService
) {
    // Se mantienen los nombres de tus funciones
    fun getAllSubjects() = studyDao.getAllSubjects()
    fun getModulesForSubject(subjectId: Int): Flow<List<ModuleEntity>> = studyDao.getModulesForSubject(subjectId)
    fun getSubmodulesForModule(moduleId: Int): Flow<List<SubmoduleEntity>> = studyDao.getSubmodulesForModule(moduleId)

    suspend fun getOrCreateQuestionsForModule(moduleId: Int): List<QuestionEntity> {
        // 1. Borra las preguntas anteriores para forzar la regeneración.
        Log.d("StudyRepository", "Borrando preguntas viejas para el módulo $moduleId.")
        studyDao.deleteQuestionsForModule(moduleId)

        // 2. Genera siempre nuevas preguntas con la IA
        Log.d("StudyRepository", "Generando nuevo test con la API para el módulo $moduleId.")
        val submodules = studyDao.getSubmodulesForModule(moduleId).first()
        val content = submodules.joinToString("\n\n") { "## ${it.title}\n${it.contentMd}" }

        if (content.isBlank()) {
            Log.w("StudyRepository", "El contenido para generar preguntas está vacío para el módulo $moduleId.")
            return emptyList()
        }

        // 3. Llama al servicio para obtener las preguntas, las guarda y las devuelve.
        // (Asumiendo que groqApiService.generateQuestions ahora hace el parsing y devuelve una List<QuestionEntity>)
        val newQuestions = groqApiService.generateQuestions(content, moduleId)
        if (newQuestions.isNotEmpty()) {
            studyDao.insertQuestions(newQuestions)
            Log.d("StudyRepository", "Nuevas ${newQuestions.size} preguntas guardadas en la BD.")
        }
        return newQuestions
    }
}

