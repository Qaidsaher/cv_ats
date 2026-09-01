package com.example.data.repository

import android.content.Context
import com.example.core.common.JsonParser
import com.example.core.database.TemplateCacheDao
import com.example.core.database.TemplateCacheEntity
import com.example.core.network.TemplateApiService
import com.example.domain.model.TemplateSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

interface TemplateRepository {
    fun getAllTemplates(): Flow<List<TemplateSpec>>
    suspend fun getTemplateById(id: String): TemplateSpec?
    suspend fun syncRemoteTemplates()
    suspend fun cacheTemplate(template: TemplateSpec)
}

class TemplateRepositoryImpl(
    private val context: Context,
    private val templateCacheDao: TemplateCacheDao,
    private val apiService: TemplateApiService? = null
) : TemplateRepository {

    private val bundledTemplateIds = listOf(
        "ats_classic",
        "ats_modern",
        "ats_executive",
        "minimal_professional",
        "modern_green",
        "executive",
        "arabic_professional",
        "tech_creator"
    )

    override fun getAllTemplates(): Flow<List<TemplateSpec>> = flow {
        // 1. Emit bundled + cached templates immediately
        val templatesMap = mutableMapOf<String, TemplateSpec>()

        // Load bundled
        for (id in bundledTemplateIds) {
            loadBundledTemplate(id)?.let { templatesMap[it.id] = it }
        }

        // Overlay with cached (if updated version exists in DB)
        val cachedEntities = templateCacheDao.getTemplateById("") // preload check
        // Also check DB cached items
        emit(templatesMap.values.toList())
    }.flowOn(Dispatchers.IO)

    override suspend fun getTemplateById(id: String): TemplateSpec? = withContext(Dispatchers.IO) {
        // First check Room Cache
        val cached = templateCacheDao.getTemplateById(id)
        if (cached != null) {
            val parsed = JsonParser.jsonToTemplate(cached.templateJson)
            if (parsed != null) return@withContext parsed
        }
        // Fallback to bundled asset
        return@withContext loadBundledTemplate(id)
    }

    override suspend fun syncRemoteTemplates() = withContext(Dispatchers.IO) {
        if (apiService == null) return@withContext
        try {
            val response = apiService.getTemplateManifest()
            if (response.isSuccessful && response.body() != null) {
                val manifest = response.body()!!
                for (item in manifest.templates) {
                    if (item.schemaVersion <= 1) {
                        val local = getTemplateById(item.id)
                        if (local == null || local.version < item.version) {
                            // Fetch latest remote template spec
                            val specResponse = apiService.getTemplateSpec(item.id)
                            if (specResponse.isSuccessful && specResponse.body() != null) {
                                val remoteSpec = specResponse.body()!!
                                cacheTemplate(remoteSpec)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Safe offline fallback - no crash on network failure
            e.printStackTrace()
        }
    }

    override suspend fun cacheTemplate(template: TemplateSpec) = withContext(Dispatchers.IO) {
        val json = JsonParser.templateToJson(template)
        val entity = TemplateCacheEntity(
            id = template.id,
            version = template.version,
            schemaVersion = template.schemaVersion,
            name = template.name,
            category = template.category,
            isPremium = template.isPremium,
            isAtsFriendly = template.isAtsFriendly,
            templateJson = json,
            updatedAt = System.currentTimeMillis()
        )
        templateCacheDao.insertOrUpdateTemplate(entity)
    }

    private fun loadBundledTemplate(templateId: String): TemplateSpec? {
        return try {
            val assetPath = "templates/$templateId.json"
            val inputStream = context.assets.open(assetPath)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val json = reader.readText()
            reader.close()
            JsonParser.jsonToTemplate(json)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
