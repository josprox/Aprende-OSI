package com.josprox.redesosi.data.repository

import com.josprox.redesosi.data.database.QuestionEntity
import com.josprox.redesosi.data.database.StudyDao
import com.josprox.redesosi.data.network.GroqApiService
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudyRepository @Inject constructor(
    private val studyDao: StudyDao,
    private val groqApiService: GroqApiService
) {
    fun getAllSubjects() = studyDao.getAllSubjects()
    fun getModulesForSubject(subjectId: Int) = studyDao.getModulesForSubject(subjectId)
    fun getSubmodulesForModule(moduleId: Int) = studyDao.getSubmodulesForModule(moduleId)

    suspend fun getOrCreateQuestionsForModule(moduleId: Int): List<QuestionEntity> {
        val existingQuestions = studyDao.getQuestionsForModule(moduleId)
        if (existingQuestions.isNotEmpty()) {
            return existingQuestions
        }

        // Si no hay preguntas, las generamos con la IA
        val submodules = studyDao.getSubmodulesForModule(moduleId).first()
        val content = submodules.joinToString("\n\n") { "## ${it.title}\n${it.contentMd}" }

        if (content.isBlank()) return emptyList()

        val newQuestions = groqApiService.generateQuestions(content, moduleId)
        if (newQuestions.isNotEmpty()) {
            studyDao.insertQuestions(newQuestions)
        }
        return newQuestions
    }
}
