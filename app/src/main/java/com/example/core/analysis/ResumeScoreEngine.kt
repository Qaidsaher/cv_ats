package com.example.core.analysis

import com.example.domain.model.Resume

enum class ResumeScoreGrade(val minScore: Int, val labelResId: Int, val hexColor: String) {
    NEEDS_WORK(0, com.example.R.string.score_grade_needs_work, "#DC2626"),       // Red
    GOOD_START(50, com.example.R.string.score_grade_good_start, "#D97706"),     // Amber
    STRONG_RESUME(75, com.example.R.string.score_grade_strong, "#0D9488"),      // Teal
    ALL_STAR(90, com.example.R.string.score_grade_all_star, "#16A34A")          // Emerald Green
}

enum class TipPriority {
    HIGH,
    MEDIUM,
    LOW
}

data class ResumeScoreTip(
    val id: String,
    val sectionKey: String,
    val title: String,
    val description: String,
    val priority: TipPriority,
    val potentialScoreGain: Int,
    val actionLabel: String
)

data class SectionScoreBreakdown(
    val sectionKey: String,
    val categoryTitle: String,
    val currentPoints: Int,
    val maxPoints: Int,
    val percentage: Int = if (maxPoints > 0) ((currentPoints.toFloat() / maxPoints) * 100).toInt() else 0,
    val itemsSummary: String
)

data class ResumeScoreCheckItem(
    val id: String,
    val sectionKey: String,
    val title: String,
    val points: Int,
    val isCompleted: Boolean,
    val hint: String
)

data class ResumeScoreReport(
    val overallScore: Int, // 0 to 100
    val grade: ResumeScoreGrade,
    val sectionBreakdowns: List<SectionScoreBreakdown>,
    val tips: List<ResumeScoreTip>,
    val completedItems: List<ResumeScoreCheckItem>,
    val missingItems: List<ResumeScoreCheckItem>
)

object ResumeScoreEngine {

    fun evaluateResume(resume: Resume): ResumeScoreReport {
        val checkItems = mutableListOf<ResumeScoreCheckItem>()
        val tips = mutableListOf<ResumeScoreTip>()

        // ----------------------------------------------------
        // 1. Personal & Contact Information (25 Points)
        // ----------------------------------------------------
        val pi = resume.personalInfo
        val hasValidName = pi.fullName.isNotBlank() &&
                !pi.fullName.equals("Untitled", ignoreCase = true) &&
                !pi.fullName.equals("New Resume", ignoreCase = true)
        
        checkItems.add(
            ResumeScoreCheckItem(
                id = "pi_name",
                sectionKey = "personal",
                title = "Full Name & Professional Identity",
                points = 5,
                isCompleted = hasValidName,
                hint = "Enter your first and last name so recruiters know who you are."
            )
        )
        if (!hasValidName) {
            tips.add(
                ResumeScoreTip(
                    id = "tip_pi_name",
                    sectionKey = "personal",
                    title = "Add your full name",
                    description = "A complete, clear full name is essential for recruiter identification.",
                    priority = TipPriority.HIGH,
                    potentialScoreGain = 5,
                    actionLabel = "Add Name"
                )
            )
        }

        val hasTitle = pi.professionalTitle.isNotBlank()
        checkItems.add(
            ResumeScoreCheckItem(
                id = "pi_title",
                sectionKey = "personal",
                title = "Target Job Title / Headline",
                points = 5,
                isCompleted = hasTitle,
                hint = "e.g., Senior Android Engineer or Product Marketing Lead."
            )
        )
        if (!hasTitle) {
            tips.add(
                ResumeScoreTip(
                    id = "tip_pi_title",
                    sectionKey = "personal",
                    title = "Add a professional title",
                    description = "Specifying your target role (e.g. Mobile Developer) aligns with ATS keywords.",
                    priority = TipPriority.HIGH,
                    potentialScoreGain = 5,
                    actionLabel = "Add Title"
                )
            )
        }

        val hasContact = pi.email.isNotBlank() && pi.phone.isNotBlank()
        checkItems.add(
            ResumeScoreCheckItem(
                id = "pi_contact",
                sectionKey = "personal",
                title = "Email & Phone Number",
                points = 5,
                isCompleted = hasContact,
                hint = "Ensure both a reachable email address and mobile phone number are listed."
            )
        )
        if (!hasContact) {
            tips.add(
                ResumeScoreTip(
                    id = "tip_pi_contact",
                    sectionKey = "personal",
                    title = "Add direct contact channels",
                    description = "Recruiters require both an email and phone number to schedule interview rounds.",
                    priority = TipPriority.HIGH,
                    potentialScoreGain = 5,
                    actionLabel = "Add Contact"
                )
            )
        }

        val hasLocation = pi.city.isNotBlank() || pi.country.isNotBlank() || pi.address.isNotBlank()
        checkItems.add(
            ResumeScoreCheckItem(
                id = "pi_location",
                sectionKey = "personal",
                title = "Location (City & Country)",
                points = 5,
                isCompleted = hasLocation,
                hint = "Specify your current metropolitan location for local or remote matching."
            )
        )
        if (!hasLocation) {
            tips.add(
                ResumeScoreTip(
                    id = "tip_pi_location",
                    sectionKey = "personal",
                    title = "Add your location / city",
                    description = "Location helps recruiters filter candidates within target hiring regions.",
                    priority = TipPriority.MEDIUM,
                    potentialScoreGain = 5,
                    actionLabel = "Add Location"
                )
            )
        }

        val hasOnlinePresence = pi.linkedIn.isNotBlank() || pi.gitHub.isNotBlank() || pi.website.isNotBlank()
        checkItems.add(
            ResumeScoreCheckItem(
                id = "pi_links",
                sectionKey = "personal",
                title = "LinkedIn Profile or Portfolio URL",
                points = 5,
                isCompleted = hasOnlinePresence,
                hint = "Include a LinkedIn profile link, GitHub handle, or personal portfolio."
            )
        )
        if (!hasOnlinePresence) {
            tips.add(
                ResumeScoreTip(
                    id = "tip_pi_links",
                    sectionKey = "personal",
                    title = "Add LinkedIn or Portfolio link",
                    description = "Candidates with active LinkedIn profiles receive 71% more recruiter callbacks.",
                    priority = TipPriority.MEDIUM,
                    potentialScoreGain = 5,
                    actionLabel = "Add Links"
                )
            )
        }

        // ----------------------------------------------------
        // 2. Professional Summary (15 Points)
        // ----------------------------------------------------
        val summaryText = resume.summary.trim()
        val hasSummary = summaryText.isNotBlank()
        checkItems.add(
            ResumeScoreCheckItem(
                id = "sum_presence",
                sectionKey = "summary",
                title = "Executive Summary Section",
                points = 5,
                isCompleted = hasSummary,
                hint = "A 2-4 sentence overview of your career background and key capabilities."
            )
        )
        if (!hasSummary) {
            tips.add(
                ResumeScoreTip(
                    id = "tip_sum_presence",
                    sectionKey = "summary",
                    title = "Write a professional summary",
                    description = "Hook hiring managers immediately with a 2-3 sentence overview of your career highlights.",
                    priority = TipPriority.HIGH,
                    potentialScoreGain = 15,
                    actionLabel = "Write Summary"
                )
            )
        }

        val wordCount = if (hasSummary) summaryText.split("\\s+".toRegex()).size else 0
        val hasSubstantiveSummary = wordCount >= 25
        checkItems.add(
            ResumeScoreCheckItem(
                id = "sum_depth",
                sectionKey = "summary",
                title = "Summary Depth (25+ words)",
                points = 5,
                isCompleted = hasSubstantiveSummary,
                hint = "Aim for 30-50 words detailing core domain expertise and notable achievements."
            )
        )
        if (hasSummary && !hasSubstantiveSummary) {
            tips.add(
                ResumeScoreTip(
                    id = "tip_sum_depth",
                    sectionKey = "summary",
                    title = "Expand your summary",
                    description = "Your summary is currently very brief ($wordCount words). Expand to at least 25-35 words with key competencies.",
                    priority = TipPriority.MEDIUM,
                    potentialScoreGain = 5,
                    actionLabel = "Expand Summary"
                )
            )
        }

        // Summary Impact Words check (e.g., years of experience, leadership, specialized terms)
        val hasImpactfulSummary = hasSubstantiveSummary && (
                summaryText.contains("year", ignoreCase = true) ||
                summaryText.contains("experience", ignoreCase = true) ||
                summaryText.contains("expert", ignoreCase = true) ||
                summaryText.contains("lead", ignoreCase = true) ||
                summaryText.contains("deliver", ignoreCase = true) ||
                summaryText.contains("manage", ignoreCase = true) ||
                summaryText.contains("architect", ignoreCase = true) ||
                summaryText.contains("develop", ignoreCase = true) ||
                summaryText.contains("specializ", ignoreCase = true)
        )
        checkItems.add(
            ResumeScoreCheckItem(
                id = "sum_impact",
                sectionKey = "summary",
                title = "Summary Value Proposition & Keywords",
                points = 5,
                isCompleted = hasImpactfulSummary,
                hint = "Highlight years of experience, core technical specialties, or leadership focus."
            )
        )

        // ----------------------------------------------------
        // 3. Work Experience (25 Points)
        // ----------------------------------------------------
        val experiences = resume.experiences
        val hasAnyExperience = experiences.isNotEmpty()
        checkItems.add(
            ResumeScoreCheckItem(
                id = "exp_presence",
                sectionKey = "experience",
                title = "Work Experience Entries",
                points = 10,
                isCompleted = hasAnyExperience,
                hint = "List past or present employment roles with company name and job title."
            )
        )
        if (!hasAnyExperience) {
            tips.add(
                ResumeScoreTip(
                    id = "tip_exp_presence",
                    sectionKey = "experience",
                    title = "Add work experience history",
                    description = "Work history is the most weighted component by recruiters and ATS parsers.",
                    priority = TipPriority.HIGH,
                    potentialScoreGain = 25,
                    actionLabel = "Add Experience"
                )
            )
        }

        val hasValidRoles = hasAnyExperience && experiences.all { it.company.isNotBlank() && it.jobTitle.isNotBlank() }
        checkItems.add(
            ResumeScoreCheckItem(
                id = "exp_valid_roles",
                sectionKey = "experience",
                title = "Complete Company & Job Title Details",
                points = 5,
                isCompleted = hasValidRoles,
                hint = "Ensure all experience entries have both company name and official title."
            )
        )

        // Check for bulleted responsibilities or detailed descriptions
        val hasBulletPoints = hasAnyExperience && experiences.any {
            it.bullets.isNotEmpty() || it.description.lines().size >= 2 || it.description.contains("•") || it.description.contains("-")
        }
        checkItems.add(
            ResumeScoreCheckItem(
                id = "exp_bullets",
                sectionKey = "experience",
                title = "Bulleted Key Accomplishments",
                points = 5,
                isCompleted = hasBulletPoints,
                hint = "Break down responsibilities into 2-4 scannable bullet points per job."
            )
        )
        if (hasAnyExperience && !hasBulletPoints) {
            tips.add(
                ResumeScoreTip(
                    id = "tip_exp_bullets",
                    sectionKey = "experience",
                    title = "Use bullet points for work roles",
                    description = "Bulleted accomplishments increase readability by 60% compared to dense paragraphs.",
                    priority = TipPriority.HIGH,
                    potentialScoreGain = 5,
                    actionLabel = "Add Bullets"
                )
            )
        }

        // Check for quantifiable metrics (%, $, +, numbers, k, M)
        val metricRegex = Regex("""(\d+[%kKmM$+]|\b\d+\b|percent|\$|\+)""")
        val hasMetricsInExp = hasAnyExperience && experiences.any { exp ->
            metricRegex.containsMatchIn(exp.description) || exp.bullets.any { metricRegex.containsMatchIn(it) }
        }
        checkItems.add(
            ResumeScoreCheckItem(
                id = "exp_metrics",
                sectionKey = "experience",
                title = "Quantifiable Results & Metrics",
                points = 5,
                isCompleted = hasMetricsInExp,
                hint = "Quantify impact with numbers, percentages, budget sizes, or team counts (e.g. +35% growth)."
            )
        )
        if (hasAnyExperience && !hasMetricsInExp) {
            tips.add(
                ResumeScoreTip(
                    id = "tip_exp_metrics",
                    sectionKey = "experience",
                    title = "Add measurable metrics (%, $, numbers)",
                    description = "Quantifying results (e.g. 'boosted sales by 25%', 'managed team of 6') makes achievements concrete.",
                    priority = TipPriority.MEDIUM,
                    potentialScoreGain = 5,
                    actionLabel = "Add Metrics"
                )
            )
        }

        // ----------------------------------------------------
        // 4. Education & Academics (15 Points)
        // ----------------------------------------------------
        val educations = resume.educations
        val hasEducation = educations.isNotEmpty()
        checkItems.add(
            ResumeScoreCheckItem(
                id = "edu_presence",
                sectionKey = "education",
                title = "Education & Degree Entry",
                points = 5,
                isCompleted = hasEducation,
                hint = "List your highest level of completed or current academic study."
            )
        )
        if (!hasEducation) {
            tips.add(
                ResumeScoreTip(
                    id = "tip_edu_presence",
                    sectionKey = "education",
                    title = "Add your education credentials",
                    description = "Recruiters and automated filters verify degree requirements.",
                    priority = TipPriority.HIGH,
                    potentialScoreGain = 15,
                    actionLabel = "Add Education"
                )
            )
        }

        val hasInstitutionAndDegree = hasEducation && educations.all { it.institution.isNotBlank() && it.degree.isNotBlank() }
        checkItems.add(
            ResumeScoreCheckItem(
                id = "edu_details",
                sectionKey = "education",
                title = "Degree & University Details",
                points = 5,
                isCompleted = hasInstitutionAndDegree,
                hint = "Specify university name, exact degree type (e.g. B.S., M.A.), and major."
            )
        )

        val hasDatesOrField = hasEducation && educations.any { it.fieldOfStudy.isNotBlank() || it.endDate.isNotBlank() }
        checkItems.add(
            ResumeScoreCheckItem(
                id = "edu_dates",
                sectionKey = "education",
                title = "Field of Study & Graduation Year",
                points = 5,
                isCompleted = hasDatesOrField,
                hint = "Include graduation date or expected year for academic timeline clarity."
            )
        )

        // ----------------------------------------------------
        // 5. Skills & Strengths (15 Points)
        // ----------------------------------------------------
        val skills = resume.skills
        val skillsCount = skills.size
        val hasBaseSkills = skillsCount >= 3
        checkItems.add(
            ResumeScoreCheckItem(
                id = "sk_base",
                sectionKey = "skills",
                title = "Core Skills (At least 3 skills)",
                points = 5,
                isCompleted = hasBaseSkills,
                hint = "List at least 3 relevant skills aligned with your target industry."
            )
        )
        if (!hasBaseSkills) {
            tips.add(
                ResumeScoreTip(
                    id = "tip_sk_base",
                    sectionKey = "skills",
                    title = "Add core skills (at least 3-5)",
                    description = "Skills are scanned directly by ATS keyword matching systems.",
                    priority = TipPriority.HIGH,
                    potentialScoreGain = 10,
                    actionLabel = "Add Skills"
                )
            )
        }

        val hasRichSkills = skillsCount >= 5
        checkItems.add(
            ResumeScoreCheckItem(
                id = "sk_rich",
                sectionKey = "skills",
                title = "Diverse Skill Portfolio (5+ skills)",
                points = 5,
                isCompleted = hasRichSkills,
                hint = "Include 5 to 10 skills covering both technical tools and interpersonal strengths."
            )
        )
        if (hasBaseSkills && !hasRichSkills) {
            tips.add(
                ResumeScoreTip(
                    id = "tip_sk_rich",
                    sectionKey = "skills",
                    title = "Expand to 5+ diverse skills",
                    description = "Top candidates include 6-10 skills to match varying job requisition descriptions.",
                    priority = TipPriority.MEDIUM,
                    potentialScoreGain = 5,
                    actionLabel = "Add More Skills"
                )
            )
        }

        val hasCategorizedOrLevels = skills.isNotEmpty() && skills.any { it.category.isNotBlank() || it.level.isNotBlank() }
        checkItems.add(
            ResumeScoreCheckItem(
                id = "sk_categories",
                sectionKey = "skills",
                title = "Skill Categorization & Proficiency",
                points = 5,
                isCompleted = hasCategorizedOrLevels,
                hint = "Organize skills into categories (e.g. Technical, Soft Skills, Tools) with proficiency levels."
            )
        )
        if (skills.isNotEmpty() && !hasCategorizedOrLevels) {
            tips.add(
                ResumeScoreTip(
                    id = "tip_sk_categories",
                    sectionKey = "skills",
                    title = "Organize skills by category",
                    description = "Tagging skills with categories (Technical, Design, Soft Skills) improves ATS parsing.",
                    priority = TipPriority.LOW,
                    potentialScoreGain = 5,
                    actionLabel = "Categorize"
                )
            )
        }

        // ----------------------------------------------------
        // 6. Bonus Highlights (5 Points)
        // ----------------------------------------------------
        val hasBonusAssets = resume.certificates.isNotEmpty() ||
                resume.projects.isNotEmpty() ||
                resume.languages.isNotEmpty() ||
                resume.references.isNotEmpty() ||
                resume.customSections.isNotEmpty()

        checkItems.add(
            ResumeScoreCheckItem(
                id = "bonus_highlights",
                sectionKey = "bonus",
                title = "Certifications, Languages, or Projects",
                points = 5,
                isCompleted = hasBonusAssets,
                hint = "Add licenses, spoken languages, software projects, or certifications to stand out."
            )
        )
        if (!hasBonusAssets) {
            tips.add(
                ResumeScoreTip(
                    id = "tip_bonus_highlights",
                    sectionKey = "certificates",
                    title = "Add certifications or languages",
                    description = "Industry certifications and multilingual proficiencies differentiate your profile.",
                    priority = TipPriority.LOW,
                    potentialScoreGain = 5,
                    actionLabel = "Add Certifications"
                )
            )
        }

        // ----------------------------------------------------
        // Calculate Totals & Section Breakdowns
        // ----------------------------------------------------
        val completedItems = checkItems.filter { it.isCompleted }
        val missingItems = checkItems.filter { !it.isCompleted }
        val totalEarned = completedItems.sumOf { it.points }
        val overallScore = totalEarned.coerceIn(0, 100)

        val grade = when {
            overallScore >= ResumeScoreGrade.ALL_STAR.minScore -> ResumeScoreGrade.ALL_STAR
            overallScore >= ResumeScoreGrade.STRONG_RESUME.minScore -> ResumeScoreGrade.STRONG_RESUME
            overallScore >= ResumeScoreGrade.GOOD_START.minScore -> ResumeScoreGrade.GOOD_START
            else -> ResumeScoreGrade.NEEDS_WORK
        }

        // Group into 6 Section Score Breakdowns
        val sectionBreakdowns = listOf(
            computeSectionBreakdown("personal", "Contact & Profile", checkItems, "${if (hasValidName) pi.fullName else "Incomplete"}"),
            computeSectionBreakdown("summary", "Summary", checkItems, "${wordCount} words"),
            computeSectionBreakdown("experience", "Work History", checkItems, "${experiences.size} roles"),
            computeSectionBreakdown("education", "Education", checkItems, "${educations.size} entries"),
            computeSectionBreakdown("skills", "Skills", checkItems, "${skills.size} skills"),
            computeSectionBreakdown("bonus", "Bonus Highlights", checkItems, "${if (hasBonusAssets) "Active" else "None"}")
        )

        // Sort tips by priority: HIGH -> MEDIUM -> LOW
        val sortedTips = tips.sortedWith(
            compareBy<ResumeScoreTip> {
                when (it.priority) {
                    TipPriority.HIGH -> 0
                    TipPriority.MEDIUM -> 1
                    TipPriority.LOW -> 2
                }
            }.thenByDescending { it.potentialScoreGain }
        )

        return ResumeScoreReport(
            overallScore = overallScore,
            grade = grade,
            sectionBreakdowns = sectionBreakdowns,
            tips = sortedTips,
            completedItems = completedItems,
            missingItems = missingItems
        )
    }

    private fun computeSectionBreakdown(
        sectionKey: String,
        categoryTitle: String,
        allItems: List<ResumeScoreCheckItem>,
        summary: String
    ): SectionScoreBreakdown {
        val sectionItems = allItems.filter { it.sectionKey == sectionKey }
        val maxPoints = sectionItems.sumOf { it.points }
        val currentPoints = sectionItems.filter { it.isCompleted }.sumOf { it.points }
        return SectionScoreBreakdown(
            sectionKey = sectionKey,
            categoryTitle = categoryTitle,
            currentPoints = currentPoints,
            maxPoints = maxPoints,
            itemsSummary = summary
        )
    }
}
