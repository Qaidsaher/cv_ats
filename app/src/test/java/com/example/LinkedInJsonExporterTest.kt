package com.example

import com.example.core.export.LinkedInExportFormat
import com.example.core.export.LinkedInJsonExporter
import com.example.domain.model.Certificate
import com.example.domain.model.Education
import com.example.domain.model.Experience
import com.example.domain.model.LanguageSkill
import com.example.domain.model.PersonalInformation
import com.example.domain.model.Project
import com.example.domain.model.Resume
import com.example.domain.model.Skill
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LinkedInJsonExporterTest {

    private val sampleResume = Resume(
        id = "test-resume-123",
        title = "Senior Android Engineer",
        personalInfo = PersonalInformation(
            firstName = "Sarah",
            lastName = "Connor",
            professionalTitle = "Senior Android Architect",
            email = "sarah.connor@example.com",
            phone = "+1 555-0199",
            city = "San Francisco",
            country = "United States",
            linkedIn = "https://linkedin.com/in/sarahconnor",
            gitHub = "https://github.com/sarahconnor",
            website = "https://sarahconnor.dev"
        ),
        summary = "Results-driven Android Engineer with 8+ years experience building mission-critical apps.",
        experiences = listOf(
            Experience(
                company = "Cyberdyne Systems",
                jobTitle = "Lead Mobile Architect",
                location = "San Francisco, CA",
                startDate = "2021-03",
                endDate = "",
                currentlyWorking = true,
                description = "Spearheaded mobile architecture.",
                bullets = listOf(
                    "Architected scalable Compose design system",
                    "Mentored team of 6 engineers"
                )
            ),
            Experience(
                company = "Tech Dynamics",
                jobTitle = "Senior Software Engineer",
                location = "San Jose, CA",
                startDate = "2018-01",
                endDate = "2021-02",
                currentlyWorking = false,
                description = "Developed Kotlin backend and client apps."
            )
        ),
        educations = listOf(
            Education(
                institution = "Stanford University",
                degree = "Bachelor of Science",
                fieldOfStudy = "Computer Science",
                startDate = "2014-09",
                endDate = "2018-06"
            )
        ),
        skills = listOf(
            Skill(name = "Kotlin", level = "Expert", category = "Technical"),
            Skill(name = "Jetpack Compose", level = "Expert", category = "Technical"),
            Skill(name = "Technical Leadership", level = "Advanced", category = "Soft Skills")
        ),
        languages = listOf(
            LanguageSkill(name = "English", level = "Native")
        ),
        certificates = listOf(
            Certificate(name = "Google Cloud Professional", issuer = "Google", issueDate = "2022-04")
        ),
        projects = listOf(
            Project(name = "OpenSource App", description = "High performance client", technologies = "Kotlin, Compose")
        )
    )

    @Test
    fun testBuildLinkedInProfileJson_containsRequiredFields() {
        val jsonObj = LinkedInJsonExporter.buildLinkedInProfileJson(sampleResume)

        assertEquals("Sarah", jsonObj.getString("firstName"))
        assertEquals("Connor", jsonObj.getString("lastName"))
        assertEquals("Senior Android Architect", jsonObj.getString("headline"))
        assertTrue(jsonObj.getString("summary").contains("Results-driven"))

        // Positions
        val positions = jsonObj.getJSONArray("positions")
        assertEquals(2, positions.length())

        val firstPos = positions.getJSONObject(0)
        assertEquals("Cyberdyne Systems", firstPos.getString("companyName"))
        assertEquals("Lead Mobile Architect", firstPos.getString("title"))
        assertTrue(firstPos.getBoolean("isCurrent"))

        val bullets = firstPos.getJSONArray("bulletPoints")
        assertEquals(2, bullets.length())
        assertEquals("Architected scalable Compose design system", bullets.getString(0))

        // Educations
        val educations = jsonObj.getJSONArray("educations")
        assertEquals(1, educations.length())
        assertEquals("Stanford University", educations.getJSONObject(0).getString("schoolName"))

        // Skills
        val skills = jsonObj.getJSONArray("skills")
        assertEquals(3, skills.length())
        assertEquals("Kotlin", skills.getJSONObject(0).getString("name"))
    }

    @Test
    fun testBuildJsonResumeStandard_conformsToStandard() {
        val jsonObj = LinkedInJsonExporter.buildJsonResumeStandard(sampleResume)

        assertTrue(jsonObj.has("\$schema"))
        assertTrue(jsonObj.has("basics"))
        assertTrue(jsonObj.has("work"))
        assertTrue(jsonObj.has("education"))
        assertTrue(jsonObj.has("skills"))

        val basics = jsonObj.getJSONObject("basics")
        assertEquals("Sarah Connor", basics.getString("name"))
        assertEquals("sarah.connor@example.com", basics.getString("email"))

        val profiles = basics.getJSONArray("profiles")
        assertTrue(profiles.length() >= 2)

        val work = jsonObj.getJSONArray("work")
        assertEquals(2, work.length())
        assertEquals("Cyberdyne Systems", work.getJSONObject(0).getString("name"))
    }

    @Test
    fun testExportToJsonString_producesValidFormattedJson() {
        val jsonString = LinkedInJsonExporter.exportToJson(sampleResume, LinkedInExportFormat.LINKEDIN_BUILD_FROM_RESUME)
        assertNotNull(jsonString)
        val parsed = JSONObject(jsonString)
        assertEquals("Sarah", parsed.getString("firstName"))
    }
}
