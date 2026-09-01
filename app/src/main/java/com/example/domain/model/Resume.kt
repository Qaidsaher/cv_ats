package com.example.domain.model

import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class Resume(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "My Resume",
    val templateId: String = "modern_green",
    val language: String = "en", // "en" or "ar"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val personalInfo: PersonalInformation = PersonalInformation(),
    val summary: String = "",
    val experiences: List<Experience> = emptyList(),
    val educations: List<Education> = emptyList(),
    val skills: List<Skill> = emptyList(),
    val languages: List<LanguageSkill> = emptyList(),
    val projects: List<Project> = emptyList(),
    val certificates: List<Certificate> = emptyList(),
    val references: List<Reference> = emptyList(),
    val customSections: List<CustomSection> = emptyList(),
    val sectionOrder: List<String> = DEFAULT_SECTION_ORDER,
    val sectionVisibility: Map<String, Boolean> = DEFAULT_SECTION_VISIBILITY,
    val customization: TemplateCustomization = TemplateCustomization()
) {
    companion object {
        val DEFAULT_SECTION_ORDER = listOf(
            "personal",
            "summary",
            "experience",
            "education",
            "skills",
            "languages",
            "projects",
            "certificates",
            "references",
            "custom"
        )

        val DEFAULT_SECTION_VISIBILITY = mapOf(
            "personal" to true,
            "summary" to true,
            "experience" to true,
            "education" to true,
            "skills" to true,
            "languages" to true,
            "projects" to true,
            "certificates" to true,
            "references" to true,
            "custom" to true
        )
    }

    val isRtl: Boolean
        get() = language.equals("ar", ignoreCase = true)
}

@JsonClass(generateAdapter = true)
data class PersonalInformation(
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val professionalTitle: String = "",
    val profilePhotoUri: String? = null,
    val email: String = "",
    val phone: String = "",
    val city: String = "",
    val country: String = "",
    val address: String = "",
    val website: String = "",
    val linkedIn: String = "",
    val gitHub: String = "",
    val portfolio: String = ""
) {
    val fullName: String
        get() = listOf(firstName, middleName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { "Untitled" }

    val locationFormatted: String
        get() = listOf(city, country).filter { it.isNotBlank() }.joinToString(", ")
}

@JsonClass(generateAdapter = true)
data class Experience(
    val id: String = UUID.randomUUID().toString(),
    val company: String = "",
    val jobTitle: String = "",
    val location: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val currentlyWorking: Boolean = false,
    val description: String = "",
    val bullets: List<String> = emptyList(),
    val sortOrder: Int = 0
) {
    /**
     * Resolves all bullet points for this experience role.
     * If explicit bullets list is populated, uses that.
     * Otherwise splits description by newlines/bullet markers.
     */
    val effectiveBullets: List<String>
        get() {
            if (bullets.isNotEmpty()) {
                return bullets.map { it.trim() }.filter { it.isNotBlank() }
            }
            if (description.isBlank()) return emptyList()
            return description.lines()
                .map { line ->
                    line.trim()
                        .removePrefix("•")
                        .removePrefix("-")
                        .removePrefix("*")
                        .trim()
                }
                .filter { it.isNotBlank() }
        }

    val formattedDateRange: String
        get() {
            val end = if (currentlyWorking) "Present" else endDate.ifBlank { "Present" }
            val start = startDate.ifBlank { "" }
            return if (start.isNotBlank()) "$start – $end" else end
        }
}

@JsonClass(generateAdapter = true)
data class Education(
    val id: String = UUID.randomUUID().toString(),
    val institution: String = "",
    val degree: String = "",
    val fieldOfStudy: String = "",
    val location: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val currentlyStudying: Boolean = false,
    val description: String = "",
    val sortOrder: Int = 0
)

@JsonClass(generateAdapter = true)
data class Skill(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val level: String = "Advanced", // Beginner, Intermediate, Advanced, Expert
    val category: String = "Technical", // Technical, Soft Skills, Tools & Frameworks, Languages, Leadership & Management, etc.
    val sortOrder: Int = 0
)

@JsonClass(generateAdapter = true)
data class LanguageSkill(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val level: String = "Fluent", // Native, Fluent, Intermediate, Basic
    val sortOrder: Int = 0
)

@JsonClass(generateAdapter = true)
data class Project(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val role: String = "",
    val description: String = "",
    val url: String = "",
    val technologies: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val sortOrder: Int = 0
)

@JsonClass(generateAdapter = true)
data class Certificate(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val issuer: String = "",
    val issueDate: String = "",
    val expirationDate: String = "",
    val credentialId: String = "",
    val credentialUrl: String = "",
    val sortOrder: Int = 0
)

@JsonClass(generateAdapter = true)
data class Reference(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val jobTitle: String = "",
    val company: String = "",
    val phone: String = "",
    val email: String = "",
    val sortOrder: Int = 0
)

@JsonClass(generateAdapter = true)
data class CustomSection(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Custom Section",
    val items: List<CustomSectionItem> = emptyList(),
    val sortOrder: Int = 0
)

@JsonClass(generateAdapter = true)
data class CustomSectionItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val subtitle: String = "",
    val date: String = "",
    val description: String = ""
)

@JsonClass(generateAdapter = true)
data class TemplateCustomization(
    val primaryColorHex: String? = null,
    val secondaryColorHex: String? = null,
    val accentColorHex: String? = null,
    val fontFamily: String = "default",
    val fontSizeScale: Float = 1.0f,
    val lineSpacingScale: Float = 1.0f,
    val showPhoto: Boolean = true,
    val pageFormat: String = "A4" // "A4" or "LETTER"
)
