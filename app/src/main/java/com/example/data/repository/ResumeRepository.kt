package com.example.data.repository

import com.example.core.common.JsonParser
import com.example.core.database.ResumeDao
import com.example.core.database.ResumeEntity
import com.example.domain.model.Resume
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

interface ResumeRepository {
    fun getAllResumes(): Flow<List<Resume>>
    fun getResumeById(id: String): Flow<Resume?>
    suspend fun getResumeByIdDirect(id: String): Resume?
    suspend fun saveResume(resume: Resume)
    suspend fun deleteResume(id: String)
    suspend fun duplicateResume(id: String): String?
    suspend fun renameResume(id: String, newTitle: String)
    suspend fun updateTemplate(id: String, templateId: String)
}

class ResumeRepositoryImpl(
    private val resumeDao: ResumeDao
) : ResumeRepository {

    override fun getAllResumes(): Flow<List<Resume>> {
        return resumeDao.getAllResumes().map { entities ->
            entities.mapNotNull { entity ->
                JsonParser.jsonToResume(entity.resumeJson)
            }
        }
    }

    override fun getResumeById(id: String): Flow<Resume?> {
        return resumeDao.getResumeByIdFlow(id).map { entity ->
            entity?.let { JsonParser.jsonToResume(it.resumeJson) }
        }
    }

    override suspend fun getResumeByIdDirect(id: String): Resume? {
        val entity = resumeDao.getResumeById(id) ?: return null
        return JsonParser.jsonToResume(entity.resumeJson)
    }

    override suspend fun saveResume(resume: Resume) {
        val updatedResume = resume.copy(updatedAt = System.currentTimeMillis())
        val json = JsonParser.resumeToJson(updatedResume)
        val entity = ResumeEntity(
            id = updatedResume.id,
            title = updatedResume.title,
            templateId = updatedResume.templateId,
            language = updatedResume.language,
            createdAt = updatedResume.createdAt,
            updatedAt = updatedResume.updatedAt,
            resumeJson = json
        )
        resumeDao.insertResume(entity)
    }

    override suspend fun deleteResume(id: String) {
        resumeDao.deleteResumeById(id)
    }

    override suspend fun duplicateResume(id: String): String? {
        val original = getResumeByIdDirect(id) ?: return null
        val newId = UUID.randomUUID().toString()
        val duplicated = original.copy(
            id = newId,
            title = "${original.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        saveResume(duplicated)
        return newId
    }

    override suspend fun renameResume(id: String, newTitle: String) {
        val original = getResumeByIdDirect(id) ?: return
        val updated = original.copy(title = newTitle, updatedAt = System.currentTimeMillis())
        saveResume(updated)
    }

    override suspend fun updateTemplate(id: String, templateId: String) {
        val original = getResumeByIdDirect(id) ?: return
        val updated = original.copy(templateId = templateId, updatedAt = System.currentTimeMillis())
        saveResume(updated)
    }
}
