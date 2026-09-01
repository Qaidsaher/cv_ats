package com.example.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Resume

@Entity(tableName = "resumes")
data class ResumeEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val templateId: String,
    val language: String,
    val createdAt: Long,
    val updatedAt: Long,
    val resumeJson: String
)

@Entity(tableName = "cached_templates")
data class TemplateCacheEntity(
    @PrimaryKey
    val id: String,
    val version: Int,
    val schemaVersion: Int,
    val name: String,
    val category: String,
    val isPremium: Boolean,
    val isAtsFriendly: Boolean,
    val templateJson: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "skills",
    indices = [
        androidx.room.Index(value = ["resumeId"]),
        androidx.room.Index(value = ["category"])
    ]
)
data class SkillEntity(
    @PrimaryKey
    val id: String,
    val resumeId: String,
    val name: String,
    val category: String = "Technical",
    val level: String = "Advanced",
    val sortOrder: Int = 0
)
