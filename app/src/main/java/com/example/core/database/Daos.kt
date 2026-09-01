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
