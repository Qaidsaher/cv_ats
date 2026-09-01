package com.example.core.common

import com.example.domain.model.Resume
import com.example.domain.model.TemplateSpec
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object JsonParser {
    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val resumeAdapter = moshi.adapter(Resume::class.java)
    private val templateAdapter = moshi.adapter(TemplateSpec::class.java)

    fun resumeToJson(resume: Resume): String {
        return resumeAdapter.toJson(resume)
    }

    fun jsonToResume(json: String): Resume? {
        return try {
            resumeAdapter.fromJson(json)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun templateToJson(template: TemplateSpec): String {
        return templateAdapter.toJson(template)
    }

    fun jsonToTemplate(json: String): TemplateSpec? {
        return try {
            val parsed = templateAdapter.fromJson(json)
            if (parsed != null && parsed.isSupportedSchema) {
                parsed
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
