package com.example.core.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.domain.model.Experience
import com.example.domain.model.Resume
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

enum class LinkedInExportFormat(val title: String, val description: String) {
    LINKEDIN_BUILD_FROM_RESUME(
        title = "LinkedIn Profile JSON",
        description = "Optimized for LinkedIn's 'Build from Resume' and Profile Importer"
    ),
    JSON_RESUME_STANDARD(
        title = "JSON Resume (v1.0.0)",
        description = "Universal open standard for developer portfolios and ATS tools"
    ),
    HYBRID_ALL_IN_ONE(
        title = "All-in-One Dual Schema",
        description = "Includes both LinkedIn direct mapping and standard JSON Resume format"
    )
}

/**
 * Exporter responsible for converting Resume models into LinkedIn 'Build from Resume'
 * and standard JSON Resume (jsonresume.org) schemas.
 */
object LinkedInJsonExporter {

    /**
     * Converts a domain [Resume] to a formatted JSON string according to [format].
     */
    fun exportToJson(resume: Resume, format: LinkedInExportFormat = LinkedInExportFormat.LINKEDIN_BUILD_FROM_RESUME): String {
        val root = when (format) {
            LinkedInExportFormat.LINKEDIN_BUILD_FROM_RESUME -> buildLinkedInProfileJson(resume)
            LinkedInExportFormat.JSON_RESUME_STANDARD -> buildJsonResumeStandard(resume)
            LinkedInExportFormat.HYBRID_ALL_IN_ONE -> buildHybridJson(resume)
        }
        return root.toString(2)
    }

    /**
     * Builds LinkedIn's direct Profile and 'Build from Resume' JSON structure.
     */
    fun buildLinkedInProfileJson(resume: Resume): JSONObject {
        val personal = resume.personalInfo
        val root = JSONObject()

        // Core Profile Headers
        root.put("firstName", personal.firstName.trim())
        root.put("lastName", personal.lastName.trim())
        if (personal.middleName.isNotBlank()) {
            root.put("middleName", personal.middleName.trim())
        }
        root.put("headline", personal.professionalTitle.ifBlank { resume.title }.trim())
        root.put("summary", resume.summary.trim())
        root.put("industryName", "Information Technology & Services")
        root.put("locationName", personal.locationFormatted)
        root.put("geoCountryName", personal.country.ifBlank { "United States" })

        // Contact Information
        val contactInfo = JSONObject().apply {
            put("email", personal.email.trim())
            put("phone", personal.phone.trim())
            put("address", personal.address.trim())
            put("city", personal.city.trim())
            put("country", personal.country.trim())

            val websites = JSONArray()
            if (personal.website.isNotBlank()) {
                websites.put(JSONObject().apply {
                    put("type", "PERSONAL")
                    put("url", personal.website.trim())
                })
            }
            if (personal.linkedIn.isNotBlank()) {
                websites.put(JSONObject().apply {
                    put("type", "LINKEDIN")
                    put("url", personal.linkedIn.trim())
                })
            }
            if (personal.gitHub.isNotBlank()) {
                websites.put(JSONObject().apply {
                    put("type", "GITHUB")
                    put("url", personal.gitHub.trim())
                })
            }
            if (personal.portfolio.isNotBlank()) {
                websites.put(JSONObject().apply {
                    put("type", "PORTFOLIO")
                    put("url", personal.portfolio.trim())
                })
            }
            put("websites", websites)
        }
        root.put("contactInfo", contactInfo)

        // Work Experience / Positions (Mapped to LinkedIn Positions schema)
        val positionsArray = JSONArray()
        resume.experiences.forEach { exp ->
            val pos = JSONObject().apply {
                put("title", exp.jobTitle.trim())
                put("companyName", exp.company.trim())
                put("location", exp.location.trim())
                put("isCurrent", exp.currentlyWorking)

                val startDates = parseDateToMonthYear(exp.startDate)
                put("startDate", JSONObject().apply {
                    put("month", startDates.first)
                    put("year", startDates.second)
                })

                if (!exp.currentlyWorking && exp.endDate.isNotBlank()) {
                    val endDates = parseDateToMonthYear(exp.endDate)
                    put("endDate", JSONObject().apply {
                        put("month", endDates.first)
                        put("year", endDates.second)
                    })
                }

                put("description", exp.description.trim())

                // Bulleted achievements
                val bulletsArray = JSONArray()
                exp.effectiveBullets.forEach { bulletsArray.put(it) }
                put("bulletPoints", bulletsArray)
            }
            positionsArray.put(pos)
        }
        root.put("positions", positionsArray)

        // Education / Educations (Mapped to LinkedIn Education schema)
        val educationsArray = JSONArray()
        resume.educations.forEach { edu ->
            val educationObj = JSONObject().apply {
                put("schoolName", edu.institution.trim())
                put("degreeName", edu.degree.trim())
                put("fieldOfStudy", edu.fieldOfStudy.trim())

                val startDates = parseDateToMonthYear(edu.startDate)
                put("startDate", JSONObject().apply {
                    put("month", startDates.first)
                    put("year", startDates.second)
                })

                if (!edu.currentlyStudying && edu.endDate.isNotBlank()) {
                    val endDates = parseDateToMonthYear(edu.endDate)
                    put("endDate", JSONObject().apply {
                        put("month", endDates.first)
                        put("year", endDates.second)
                    })
                }

                if (edu.description.isNotBlank()) {
                    put("description", edu.description.trim())
                    put("activities", edu.description.trim())
                }
            }
            educationsArray.put(educationObj)
        }
        root.put("educations", educationsArray)

        // Skills (Grouped with category tags)
        val skillsArray = JSONArray()
        resume.skills.forEach { skill ->
            val skillObj = JSONObject().apply {
                put("name", skill.name.trim())
                put("level", skill.level)
                put("category", skill.category)
            }
            skillsArray.put(skillObj)
        }
        root.put("skills", skillsArray)

        // Languages
        val languagesArray = JSONArray()
        resume.languages.forEach { lang ->
            val langObj = JSONObject().apply {
                put("name", lang.name.trim())
                put("proficiency", lang.level)
            }
            languagesArray.put(langObj)
        }
        root.put("languages", languagesArray)

        // Certifications & Licenses
        val certsArray = JSONArray()
        resume.certificates.forEach { cert ->
            val certObj = JSONObject().apply {
                put("name", cert.name.trim())
                put("authority", cert.issuer.trim())
                if (cert.issueDate.isNotBlank()) {
                    val dates = parseDateToMonthYear(cert.issueDate)
                    put("startDate", JSONObject().apply {
                        put("month", dates.first)
                        put("year", dates.second)
                    })
                }
                if (cert.expirationDate.isNotBlank()) {
                    val dates = parseDateToMonthYear(cert.expirationDate)
                    put("endDate", JSONObject().apply {
                        put("month", dates.first)
                        put("year", dates.second)
                    })
                }
                if (cert.credentialId.isNotBlank()) {
                    put("licenseNumber", cert.credentialId.trim())
                }
                if (cert.credentialUrl.isNotBlank()) {
                    put("url", cert.credentialUrl.trim())
                }
            }
            certsArray.put(certObj)
        }
        root.put("certifications", certsArray)

        // Projects
        val projectsArray = JSONArray()
        resume.projects.forEach { proj ->
            val projObj = JSONObject().apply {
                put("title", proj.name.trim())
                put("role", proj.role.trim())
                put("description", proj.description.trim())
                if (proj.url.isNotBlank()) {
                    put("url", proj.url.trim())
                }
                if (proj.technologies.isNotBlank()) {
                    put("technologies", proj.technologies.trim())
                }
                if (proj.startDate.isNotBlank()) {
                    val dates = parseDateToMonthYear(proj.startDate)
                    put("startDate", JSONObject().apply {
                        put("month", dates.first)
                        put("year", dates.second)
                    })
                }
                if (proj.endDate.isNotBlank()) {
                    val dates = parseDateToMonthYear(proj.endDate)
                    put("endDate", JSONObject().apply {
                        put("month", dates.first)
                        put("year", dates.second)
                    })
                }
            }
            projectsArray.put(projObj)
        }
        root.put("projects", projectsArray)

        return root
    }

    /**
     * Builds official JSON Resume schema v1.0.0
     * (https://raw.githubusercontent.com/jsonresume/resume-schema/v1.0.0/schema.json)
     */
    fun buildJsonResumeStandard(resume: Resume): JSONObject {
        val personal = resume.personalInfo
        val root = JSONObject()

        root.put("\$schema", "https://raw.githubusercontent.com/jsonresume/resume-schema/v1.0.0/schema.json")

        // Basics
        val basics = JSONObject().apply {
            put("name", personal.fullName)
            put("label", personal.professionalTitle.ifBlank { resume.title })
            put("image", personal.profilePhotoUri ?: "")
            put("email", personal.email)
            put("phone", personal.phone)
            put("url", personal.website)
            put("summary", resume.summary)

            val location = JSONObject().apply {
                put("address", personal.address)
                put("postalCode", "")
                put("city", personal.city)
                put("countryCode", personal.country)
                put("region", personal.city)
            }
            put("location", location)

            val profiles = JSONArray()
            if (personal.linkedIn.isNotBlank()) {
                profiles.put(JSONObject().apply {
                    put("network", "LinkedIn")
                    put("username", extractUsername(personal.linkedIn))
                    put("url", personal.linkedIn)
                })
            }
            if (personal.gitHub.isNotBlank()) {
                profiles.put(JSONObject().apply {
                    put("network", "GitHub")
                    put("username", extractUsername(personal.gitHub))
                    put("url", personal.gitHub)
                })
            }
            if (personal.portfolio.isNotBlank()) {
                profiles.put(JSONObject().apply {
                    put("network", "Portfolio")
                    put("username", personal.fullName)
                    put("url", personal.portfolio)
                })
            }
            put("profiles", profiles)
        }
        root.put("basics", basics)

        // Work
        val workArray = JSONArray()
        resume.experiences.forEach { exp ->
            val workObj = JSONObject().apply {
                put("name", exp.company)
                put("position", exp.jobTitle)
                put("url", "")
                put("startDate", formatDateIso(exp.startDate))
                put("endDate", if (exp.currentlyWorking) "" else formatDateIso(exp.endDate))
                put("summary", exp.description)
                put("location", exp.location)

                val highlights = JSONArray()
                exp.effectiveBullets.forEach { highlights.put(it) }
                put("highlights", highlights)
            }
            workArray.put(workObj)
        }
        root.put("work", workArray)

        // Education
        val eduArray = JSONArray()
        resume.educations.forEach { edu ->
            val eduObj = JSONObject().apply {
                put("institution", edu.institution)
                put("url", "")
                put("area", edu.fieldOfStudy)
                put("studyType", edu.degree)
                put("startDate", formatDateIso(edu.startDate))
                put("endDate", if (edu.currentlyStudying) "" else formatDateIso(edu.endDate))
                put("score", "")
                val courses = JSONArray()
                if (edu.description.isNotBlank()) {
                    courses.put(edu.description)
                }
                put("courses", courses)
            }
            eduArray.put(eduObj)
        }
        root.put("education", eduArray)

        // Skills (Aggregated with keywords)
        val skillsArray = JSONArray()
        // Group by category if possible
        val categorized = resume.skills.groupBy { it.category }
        if (categorized.isNotEmpty()) {
            categorized.forEach { (category, list) ->
                val skillGroup = JSONObject().apply {
                    put("name", category)
                    put("level", list.firstOrNull()?.level ?: "Advanced")
                    val keywords = JSONArray()
                    list.forEach { keywords.put(it.name) }
                    put("keywords", keywords)
                }
                skillsArray.put(skillGroup)
            }
        } else {
            resume.skills.forEach { s ->
                val skillObj = JSONObject().apply {
                    put("name", s.name)
                    put("level", s.level)
                    put("keywords", JSONArray().put(s.category))
                }
                skillsArray.put(skillObj)
            }
        }
        root.put("skills", skillsArray)

        // Languages
        val langArray = JSONArray()
        resume.languages.forEach { lang ->
            val langObj = JSONObject().apply {
                put("language", lang.name)
                put("fluency", lang.level)
            }
            langArray.put(langObj)
        }
        root.put("languages", langArray)

        // Certificates
        val certsArray = JSONArray()
        resume.certificates.forEach { cert ->
            val certObj = JSONObject().apply {
                put("name", cert.name)
                put("date", formatDateIso(cert.issueDate))
                put("issuer", cert.issuer)
                put("url", cert.credentialUrl)
            }
            certsArray.put(certObj)
        }
        root.put("certificates", certsArray)

        // Projects
        val projArray = JSONArray()
        resume.projects.forEach { proj ->
            val projObj = JSONObject().apply {
                put("name", proj.name)
                put("description", proj.description)
                put("highlights", JSONArray())
                val keywords = JSONArray()
                proj.technologies.split(",", ";").map { it.trim() }.filter { it.isNotBlank() }.forEach {
                    keywords.put(it)
                }
                put("keywords", keywords)
                put("startDate", formatDateIso(proj.startDate))
                put("endDate", formatDateIso(proj.endDate))
                put("url", proj.url)
                put("roles", JSONArray().put(proj.role))
            }
            projArray.put(projObj)
        }
        root.put("projects", projArray)

        // References
        val refArray = JSONArray()
        resume.references.forEach { ref ->
            val refObj = JSONObject().apply {
                put("name", ref.name)
                put("reference", "${ref.jobTitle} at ${ref.company}. Contact: ${listOf(ref.email, ref.phone).filter { it.isNotBlank() }.joinToString(" | ")}")
            }
            refArray.put(refObj)
        }
        root.put("references", refArray)

        // Meta information
        val meta = JSONObject().apply {
            put("canonical", "https://raw.githubusercontent.com/jsonresume/resume-schema/v1.0.0/schema.json")
            put("version", "v1.0.0")
            put("lastModified", System.currentTimeMillis())
            put("generator", "ResumeCraft Android App")
        }
        root.put("meta", meta)

        return root
    }

    /**
     * Builds a comprehensive hybrid JSON that is compatible with both
     * JSON Resume parsers and LinkedIn's direct profile structure.
     */
    fun buildHybridJson(resume: Resume): JSONObject {
        val standard = buildJsonResumeStandard(resume)
        val linkedInDirect = buildLinkedInProfileJson(resume)

        // Embed the direct LinkedIn structure under "linkedInProfile" key
        standard.put("linkedInProfile", linkedInDirect)
        return standard
    }

    /**
     * Gets a professional file name for JSON export
     * e.g. "John_Doe_LinkedIn_Resume.json"
     */
    fun getProfessionalJsonFileName(resume: Resume, format: LinkedInExportFormat): String {
        val fullName = resume.personalInfo.fullName.replace(" ", "_").ifBlank { "Resume" }
        val suffix = when (format) {
            LinkedInExportFormat.LINKEDIN_BUILD_FROM_RESUME -> "LinkedIn_Profile"
            LinkedInExportFormat.JSON_RESUME_STANDARD -> "JSON_Resume"
            LinkedInExportFormat.HYBRID_ALL_IN_ONE -> "Resume_Full"
        }
        return "${fullName}_${suffix}.json"
    }

    /**
     * Copies the formatted JSON string directly to clipboard
     */
    fun copyToClipboard(context: Context, jsonString: String): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Resume JSON", jsonString)
            clipboard.setPrimaryClip(clip)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Shares JSON text or file via Android Share Sheet
     */
    fun shareJson(context: Context, jsonString: String, fileName: String) {
        try {
            // Write to temporary cache file
            val cacheDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(cacheDir, fileName)
            file.writeText(jsonString)

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "$fileName - LinkedIn Resume")
                putExtra(Intent.EXTRA_TEXT, jsonString)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Resume JSON for LinkedIn")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Saves JSON to a selected SAF [destinationUri]
     */
    fun saveJsonToUri(context: Context, jsonString: String, destinationUri: Uri): Boolean {
        return try {
            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream: OutputStream ->
                outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Helper utilities for date conversion
    private fun parseDateToMonthYear(dateStr: String): Pair<Int, Int> {
        if (dateStr.isBlank()) return Pair(1, 2024)
        val clean = dateStr.trim()
        val parts = clean.split("-", "/", " ", ".").filter { it.isNotBlank() }

        if (parts.size >= 2) {
            val p0 = parts[0].toIntOrNull()
            val p1 = parts[1].toIntOrNull()
            if (p0 != null && p1 != null) {
                return if (p0 > 1900) {
                    // YYYY-MM
                    Pair(p1.coerceIn(1, 12), p0)
                } else {
                    // MM-YYYY
                    Pair(p0.coerceIn(1, 12), p1)
                }
            }
        } else if (parts.size == 1) {
            val year = parts[0].toIntOrNull()
            if (year != null && year > 1900) {
                return Pair(1, year)
            }
        }
        return Pair(1, 2024)
    }

    private fun formatDateIso(dateStr: String): String {
        if (dateStr.isBlank()) return ""
        val (month, year) = parseDateToMonthYear(dateStr)
        val monthFormatted = if (month < 10) "0$month" else "$month"
        return "$year-$monthFormatted-01"
    }

    private fun extractUsername(urlOrHandle: String): String {
        return urlOrHandle.removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .removePrefix("linkedin.com/in/")
            .removePrefix("github.com/")
            .trim('/')
    }
}
