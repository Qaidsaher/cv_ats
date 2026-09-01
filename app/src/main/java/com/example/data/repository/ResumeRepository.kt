package com.example.data.repository

import com.example.core.common.JsonParser
import com.example.core.database.ResumeDao
import com.example.core.database.ResumeEntity
import com.example.core.database.SkillDao
import com.example.core.database.SkillEntity
import com.example.domain.model.Resume
import com.example.domain.model.Skill
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
    fun getSkillsForResume(resumeId: String): Flow<List<Skill>>
    suspend fun saveSkill(resumeId: String, skill: Skill)
    suspend fun deleteSkill(skillId: String)
}

class ResumeRepositoryImpl(
    private val resumeDao: ResumeDao,
    private val skillDao: SkillDao? = null
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

        // Also sync skills table in Room database
        skillDao?.let { dao ->
            dao.deleteSkillsForResume(resume.id)
            if (updatedResume.skills.isNotEmpty()) {
                val skillEntities = updatedResume.skills.mapIndexed { index, s ->
                    SkillEntity(
                        id = s.id,
                        resumeId = updatedResume.id,
                        name = s.name,
                        category = s.category,
                        level = s.level,
                        sortOrder = if (s.sortOrder != 0) s.sortOrder else index
                    )
                }
                dao.insertSkills(skillEntities)
            }
        }
    }

    override suspend fun deleteResume(id: String) {
        skillDao?.deleteSkillsForResume(id)
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

    override fun getSkillsForResume(resumeId: String): Flow<List<Skill>> {
        return (skillDao?.getSkillsForResume(resumeId) ?: resumeDao.getResumeByIdFlow(resumeId).map { emptyList() }).map { list ->
            list.map { entity ->
                Skill(
                    id = entity.id,
                    name = entity.name,
                    category = entity.category,
                    level = entity.level,
                    sortOrder = entity.sortOrder
                )
            }
        }
    }

    override suspend fun saveSkill(resumeId: String, skill: Skill) {
        skillDao?.insertSkill(
            SkillEntity(
                id = skill.id,
                resumeId = resumeId,
                name = skill.name,
                category = skill.category,
                level = skill.level,
                sortOrder = skill.sortOrder
            )
        )
    }

    override suspend fun deleteSkill(skillId: String) {
        skillDao?.deleteSkillById(skillId)
    }
}
