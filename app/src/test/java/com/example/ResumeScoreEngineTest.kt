package com.example

import com.example.core.analysis.ResumeScoreEngine
import com.example.core.analysis.ResumeScoreGrade
import com.example.core.analysis.TipPriority
import com.example.domain.model.Certificate
import com.example.domain.model.Education
import com.example.domain.model.Experience
import com.example.domain.model.LanguageSkill
import com.example.domain.model.PersonalInformation
import com.example.domain.model.Resume
import com.example.domain.model.Skill
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResumeScoreEngineTest {

    @Test
    fun testEmptyResumeScoresZeroAndHasAllMissingTips() {
        val emptyResume = Resume(
            id = "test-empty",
            title = "Empty Resume",
            personalInfo = PersonalInformation(
                firstName = "",
                lastName = "",
                professionalTitle = "",
                email = "",
                phone = "",
                address = "",
                city = "",
                country = "",
                linkedIn = "",
                gitHub = "",
                website = ""
            ),
            summary = "",
            experiences = emptyList(),
            educations = emptyList(),
            skills = emptyList()
        )

        val report = ResumeScoreEngine.evaluateResume(emptyResume)

        assertEquals(0, report.overallScore)
        assertEquals(ResumeScoreGrade.NEEDS_WORK, report.grade)
        assertTrue("Should have missing items", report.missingItems.isNotEmpty())
        assertEquals(0, report.completedItems.size)

        // Verify high-priority tips exist
        val highPriorityTips = report.tips.filter { it.priority == TipPriority.HIGH }
        assertTrue("Must suggest adding full name", highPriorityTips.any { it.sectionKey == "personal" })
        assertTrue("Must suggest adding work experience", highPriorityTips.any { it.sectionKey == "experience" })
        assertTrue("Must suggest adding skills", highPriorityTips.any { it.sectionKey == "skills" })
        assertTrue("Must suggest adding education", highPriorityTips.any { it.sectionKey == "education" })
    }

    @Test
    fun testProgressiveScoreIncreaseAsSectionsAreCompleted() {
        var currentResume = Resume(
            id = "test-prog",
            title = "Progressive Resume",
            personalInfo = PersonalInformation(
                firstName = "Sarah",
                lastName = "Connor",
                professionalTitle = "Cybersecurity Specialist",
                email = "sarah@connor.tech",
                phone = "+1 555-0199",
                city = "Los Angeles",
                country = "USA",
                linkedIn = "linkedin.com/in/sarahconnor"
            )
        )

        var report = ResumeScoreEngine.evaluateResume(currentResume)
        // Personal info complete = 25 points
        assertEquals(25, report.overallScore)

        // Add summary with > 25 words and impact keywords
        currentResume = currentResume.copy(
            summary = "Dedicated cybersecurity specialist with over 8 years of professional experience leading comprehensive threat analysis, managing incident response teams, and architecting resilient enterprise zero-trust security perimeters."
        )
        report = ResumeScoreEngine.evaluateResume(currentResume)
        // Personal (25) + Summary (15) = 40 points
        assertEquals(40, report.overallScore)

        // Add work experience with bullet points and metrics
        currentResume = currentResume.copy(
            experiences = listOf(
                Experience(
                    id = "exp-1",
                    company = "Cyberdyne Corp",
                    jobTitle = "Lead Security Analyst",
                    startDate = "2020",
                    endDate = "Present",
                    description = "Protected core infrastructure.\n• Reduced network security breaches by 45% using proactive audits.\n• Managed a team of 6 engineers across 3 continents."
                )
            )
        )
        report = ResumeScoreEngine.evaluateResume(currentResume)
        // 40 + Experience (25) = 65 points
        assertEquals(65, report.overallScore)
        assertEquals(ResumeScoreGrade.GOOD_START, report.grade)

        // Add education
        currentResume = currentResume.copy(
            educations = listOf(
                Education(
                    id = "edu-1",
                    institution = "MIT",
                    degree = "B.S. in Computer Science",
                    fieldOfStudy = "Information Security",
                    startDate = "2014",
                    endDate = "2018"
                )
            )
        )
        report = ResumeScoreEngine.evaluateResume(currentResume)
        // 65 + Education (15) = 80 points
        assertEquals(80, report.overallScore)
        assertEquals(ResumeScoreGrade.STRONG_RESUME, report.grade)

        // Add skills (5+ categorized)
        currentResume = currentResume.copy(
            skills = listOf(
                Skill(id = "s1", name = "Network Security", level = "Expert", category = "Technical"),
                Skill(id = "s2", name = "Penetration Testing", level = "Advanced", category = "Technical"),
                Skill(id = "s3", name = "Incident Response", level = "Expert", category = "Technical"),
                Skill(id = "s4", name = "Cryptography", level = "Intermediate", category = "Technical"),
                Skill(id = "s5", name = "Team Leadership", level = "Advanced", category = "Soft Skills")
            )
        )
        report = ResumeScoreEngine.evaluateResume(currentResume)
        // 80 + Skills (15) = 95 points
        assertEquals(95, report.overallScore)
        assertEquals(ResumeScoreGrade.ALL_STAR, report.grade)

        // Add bonus certifications
        currentResume = currentResume.copy(
            certificates = listOf(
                Certificate(id = "c1", name = "CISSP", issuer = "ISC2", issueDate = "2022")
            ),
            languages = listOf(
                LanguageSkill(id = "l1", name = "English", level = "Native")
            )
        )
        report = ResumeScoreEngine.evaluateResume(currentResume)
        // 95 + Bonus (5) = 100 points
        assertEquals(100, report.overallScore)
        assertEquals(ResumeScoreGrade.ALL_STAR, report.grade)
        assertTrue("No missing items for 100% resume", report.missingItems.isEmpty())
        assertTrue("No tips for 100% resume", report.tips.isEmpty())
    }

    @Test
    fun testCategoryScoreBreakdowns() {
        val resume = Resume(
            id = "test-breakdown",
            title = "Test Resume",
            personalInfo = PersonalInformation(
                firstName = "Alex",
                lastName = "Mercer",
                professionalTitle = "DevOps Engineer",
                email = "alex@test.com",
                phone = "1234567890",
                city = "New York",
                linkedIn = "linkedin.com/in/alex"
            ),
            skills = listOf(
                Skill(id = "s1", name = "Docker", level = "Expert", category = "DevOps"),
                Skill(id = "s2", name = "Kubernetes", level = "Expert", category = "DevOps"),
                Skill(id = "s3", name = "Terraform", level = "Intermediate", category = "DevOps")
            )
        )

        val report = ResumeScoreEngine.evaluateResume(resume)

        val personalBreakdown = report.sectionBreakdowns.first { it.sectionKey == "personal" }
        assertEquals(25, personalBreakdown.currentPoints)
        assertEquals(25, personalBreakdown.maxPoints)
        assertEquals(100, personalBreakdown.percentage)

        val skillsBreakdown = report.sectionBreakdowns.first { it.sectionKey == "skills" }
        assertEquals(10, skillsBreakdown.currentPoints) // Has 3 skills + categorized, but not 5+ yet
        assertEquals(15, skillsBreakdown.maxPoints)

        // Verify tip to expand skills exists
        assertTrue(report.tips.any { it.id == "tip_sk_rich" })
    }
}
