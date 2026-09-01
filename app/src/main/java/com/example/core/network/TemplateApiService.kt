package com.example.core.network

import com.example.domain.model.TemplateSpec
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

@JsonClass(generateAdapter = true)
data class RemoteTemplateManifestItem(
    val id: String,
    val version: Int,
    val schemaVersion: Int,
    val name: String,
    val description: String,
    val isPremium: Boolean,
    val isAtsFriendly: Boolean,
    val category: String,
    val previewImageUrl: String? = null,
    val specUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class RemoteTemplateManifestResponse(
    val templates: List<RemoteTemplateManifestItem>,
    val serverTime: Long = System.currentTimeMillis()
)

interface TemplateApiService {

    @GET("api/v1/templates")
    suspend fun getTemplateManifest(): Response<RemoteTemplateManifestResponse>

    @GET("api/v1/templates/{id}")
    suspend fun getTemplateSpec(@Path("id") templateId: String): Response<TemplateSpec>
}
