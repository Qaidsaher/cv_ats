package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ResumeDao {

    @Query("SELECT * FROM resumes ORDER BY updatedAt DESC")
    fun getAllResumes(): Flow<List<ResumeEntity>>

    @Query("SELECT * FROM resumes WHERE id = :id LIMIT 1")
    fun getResumeByIdFlow(id: String): Flow<ResumeEntity?>

    @Query("SELECT * FROM resumes WHERE id = :id LIMIT 1")
    suspend fun getResumeById(id: String): ResumeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResume(resume: ResumeEntity)

    @Update
    suspend fun updateResume(resume: ResumeEntity)

    @Query("DELETE FROM resumes WHERE id = :id")
    suspend fun deleteResumeById(id: String)

    @Query("SELECT COUNT(*) FROM resumes")
    suspend fun getResumeCount(): Int
}

@Dao
interface TemplateCacheDao {

    @Query("SELECT * FROM cached_templates ORDER BY name ASC")
    fun getAllCachedTemplates(): Flow<List<TemplateCacheEntity>>

    @Query("SELECT * FROM cached_templates WHERE id = :id LIMIT 1")
    suspend fun getTemplateById(id: String): TemplateCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTemplate(template: TemplateCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTemplates(templates: List<TemplateCacheEntity>)

    @Query("DELETE FROM cached_templates WHERE id = :id")
    suspend fun deleteTemplateById(id: String)
}

@Dao
interface SkillDao {

    @Query("SELECT * FROM skills WHERE resumeId = :resumeId ORDER BY sortOrder ASC, name ASC")
    fun getSkillsForResume(resumeId: String): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE resumeId = :resumeId AND category = :category ORDER BY sortOrder ASC, name ASC")
    fun getSkillsForResumeByCategory(resumeId: String, category: String): Flow<List<SkillEntity>>

    @Query("SELECT DISTINCT category FROM skills WHERE resumeId = :resumeId ORDER BY category ASC")
    fun getCategoriesForResume(resumeId: String): Flow<List<String>>

    @Query("SELECT * FROM skills WHERE resumeId = :resumeId ORDER BY sortOrder ASC, name ASC")
    suspend fun getSkillsForResumeDirect(resumeId: String): List<SkillEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: SkillEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<SkillEntity>)

    @Update
    suspend fun updateSkill(skill: SkillEntity)

    @Query("DELETE FROM skills WHERE id = :id")
    suspend fun deleteSkillById(id: String)

    @Query("DELETE FROM skills WHERE resumeId = :resumeId")
    suspend fun deleteSkillsForResume(resumeId: String)
}
