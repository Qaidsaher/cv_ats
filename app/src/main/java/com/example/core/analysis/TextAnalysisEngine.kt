package com.example.core.analysis

import com.example.domain.model.Resume
import java.util.Locale
import java.util.UUID

enum class IssueType {
    SPELLING,
    GRAMMAR,
    PUNCTUATION,
    STYLE_ACTION_VERB,
    REPEATED_WORD,
    CAPITALIZATION
}

enum class IssueSeverity {
    ERROR,      // Red
    WARNING,    // Amber
    SUGGESTION  // Blue/Teal
}

data class TextIssue(
    val id: String = UUID.randomUUID().toString(),
    val sectionKey: String,         // "personal", "summary", "experience", "education", "skills", "projects", "custom"
    val targetId: String? = null,   // Specific entity ID (e.g., experience id)
    val targetField: String,        // "summary", "jobTitle", "description", "projectName", "degree", etc.
    val sectionTitle: String,       // Human readable title: "Professional Summary" or "Experience (Google)"
    val type: IssueType,
    val severity: IssueSeverity,
    val originalText: String,
    val suggestedText: String,
    val explanation: String,
    val startIndex: Int = 0,
    val endIndex: Int = 0,
    val contextSnippet: String = ""
)

data class AnalysisReport(
    val totalIssues: Int,
    val issues: List<TextIssue>,
    val spellingIssues: List<TextIssue>,
    val grammarIssues: List<TextIssue>,
    val styleIssues: List<TextIssue>,
    val score: Int,                 // 0 to 100
    val wordCount: Int,
    val characterCount: Int,
    val readingTimeSeconds: Int
)

object TextAnalysisEngine {

    // Common Resume & Professional spelling typos with exact replacements
    private val SPELLING_DICTIONARY: Map<String, String> = mapOf(
        "managment" to "management",
        "experiance" to "experience",
        "proffessional" to "professional",
        "acheived" to "achieved",
        "acheivement" to "achievement",
        "acheive" to "achieve",
        "reponsible" to "responsible",
        "responsability" to "responsibility",
        "responsibilites" to "responsibilities",
        "developped" to "developed",
        "develper" to "developer",
        "develpment" to "development",
        "implment" to "implement",
        "implmentation" to "implementation",
        "maintenence" to "maintenance",
        "maintainance" to "maintenance",
        "oppurtunity" to "opportunity",
        "oppertunity" to "opportunity",
        "curiculum" to "curriculum",
        "succesful" to "successful",
        "succesfully" to "successfully",
        "recieved" to "received",
        "seperate" to "separate",
        "seperated" to "separated",
        "definately" to "definitely",
        "occured" to "occurred",
        "accomodate" to "accommodate",
        "accomodation" to "accommodation",
        "untill" to "until",
        "alot" to "a lot",
        "skillset" to "skill set",
        "independant" to "independent",
        "enviroment" to "environment",
        "knowlege" to "knowledge",
        "recommand" to "recommend",
        "recommandation" to "recommendation",
        "calender" to "calendar",
        "buisness" to "business",
        "commited" to "committed",
        "supervison" to "supervision",
        "colaborate" to "collaborate",
        "colaboration" to "collaboration",
        "colaborated" to "collaborated",
        "optmize" to "optimize",
        "optmized" to "optimized",
        "optmization" to "optimization",
        "techology" to "technology",
        "enginer" to "engineer",
        "enginering" to "engineering",
        "coordinationg" to "coordinating",
        "comunication" to "communication",
        "goverment" to "government",
        "persue" to "pursue",
        "referance" to "reference",
        "certifcate" to "certificate",
        "certifcation" to "certification",
        "requirment" to "requirement",
        "requirments" to "requirements",
        "analize" to "analyze",
        "organise" to "organize",
        "incharge" to "in charge",
        "teh" to "the",
        "adn" to "and",
        "wiht" to "with",
        "thier" to "their",
        "recieve" to "receive",
        "peice" to "piece",
        "liason" to "liaison",
        "wierd" to "weird",
        "millenium" to "millennium",
        "privilege" to "privilege",
        "privelege" to "privilege",
        "priviledge" to "privilege",
        "occurence" to "occurrence",
        "harrass" to "harass",
        "guarentee" to "guarantee",
        "garantee" to "guarantee",
        "fourty" to "forty",
        "embarass" to "embarrass",
        "dissappoint" to "disappoint",
        "concious" to "conscious",
        "collegue" to "colleague",
        "collegues" to "colleagues",
        "begining" to "beginning",
        "adress" to "address",
        "adresses" to "addresses",
        "aquiring" to "acquiring",
        "aquire" to "acquire",
        "accross" to "across"
    )

    // Weak / Passive Resume phrases mapped to recommended dynamic Action Verbs
    private val WEAK_PHRASES: Map<String, List<String>> = mapOf(
        "was responsible for" to listOf("Spearheaded", "Directed", "Managed", "Led"),
        "were responsible for" to listOf("Spearheaded", "Directed", "Managed", "Led"),
        "is responsible for" to listOf("Oversees", "Directs", "Manages", "Leads"),
        "responsible for" to listOf("Spearheaded", "Directed", "Managed", "Led"),
        "worked on" to listOf("Engineered", "Developed", "Implemented", "Executed"),
        "helped with" to listOf("Assisted in", "Collaborated on", "Facilitated"),
        "assisted with" to listOf("Facilitated", "Collaborated on", "Supported"),
        "handled" to listOf("Coordinated", "Administered", "Streamlined"),
        "did" to listOf("Executed", "Conducted", "Delivered"),
        "tried to" to listOf("Initiated", "Pioneered", "Undertook"),
        "made" to listOf("Architected", "Created", "Formulated", "Constructed"),
        "looked after" to listOf("Maintained", "Supervised", "Guided"),
        "duties included" to listOf("Spearheaded", "Managed", "Executed"),
        "in charge of" to listOf("Headed", "Supervised", "Governed")
    )

    // Buzzword & Cliché detections
    private val CLICHE_PHRASES: Map<String, String> = mapOf(
        "team player" to "Collaborative team member / cross-functional partner",
        "hard-working" to "Dedicated / results-focused",
        "hard working" to "Dedicated / results-focused",
        "go-getter" to "Proactive / initiative-taker",
        "rockstar" to "High-performing specialist",
        "guru" to "Subject matter expert",
        "ninja" to "Proficient specialist",
        "think outside the box" to "Innovate / develop creative solutions",
        "synergy" to "Strategic alignment / collaboration",
        "results-driven" to "Demonstrated track record of delivering measurable outcomes"
    )

    // Arabic Typos & Grammar fixes
    private val ARABIC_TYPOS: Map<String, String> = mapOf(
        "مسؤل" to "مسؤول",
        "مسؤلية" to "مسؤولية",
        "مسؤليات" to "مسؤوليات",
        "انشاء" to "إنشاء",
        "ادارة" to "إدارة",
        "اعداد" to "إعداد",
        "اتقان" to "إتقان",
        "تطوير الويب" to "تطوير تطبيقات الويب",
        "مهارات التواصل" to "مهارات التواصل الفعال"
    )

    /**
     * Analyze complete resume document and generate structured report
     */
    fun analyzeResume(resume: Resume?): AnalysisReport {
        if (resume == null) {
            return AnalysisReport(
                totalIssues = 0,
                issues = emptyList(),
                spellingIssues = emptyList(),
                grammarIssues = emptyList(),
                styleIssues = emptyList(),
                score = 100,
                wordCount = 0,
                characterCount = 0,
                readingTimeSeconds = 0
            )
        }

        val issues = mutableListOf<TextIssue>()
        var totalChars = 0
        var totalWords = 0

        // Helper function to scan a text block
        fun scan(
            text: String?,
            sectionKey: String,
            sectionTitle: String,
            targetField: String,
            targetId: String? = null
        ) {
            if (text.isNullOrBlank()) return
            totalChars += text.length
            val wordsInText = text.split("\\s+".toRegex()).filter { it.isNotBlank() }
            totalWords += wordsInText.size

            val detected = analyzeSingleText(
                text = text,
                sectionKey = sectionKey,
                sectionTitle = sectionTitle,
                targetField = targetField,
                targetId = targetId
            )
            issues.addAll(detected)
        }

        // 1. Personal Information
        scan(resume.personalInfo.professionalTitle, "personal", "Personal Info", "professionalTitle")
        scan(resume.personalInfo.address, "personal", "Personal Info", "address")

        // 2. Summary
        scan(resume.summary, "summary", "Professional Summary", "summary")

        // 3. Experiences
        resume.experiences.forEach { exp ->
            val expTitle = if (exp.company.isNotBlank()) "Experience (${exp.company})" else "Experience"
            scan(exp.jobTitle, "experience", expTitle, "jobTitle", exp.id)
            scan(exp.description, "experience", expTitle, "description", exp.id)
        }

        // 4. Educations
        resume.educations.forEach { edu ->
            val eduTitle = if (edu.institution.isNotBlank()) "Education (${edu.institution})" else "Education"
            scan(edu.degree, "education", eduTitle, "degree", edu.id)
            scan(edu.fieldOfStudy, "education", eduTitle, "fieldOfStudy", edu.id)
            scan(edu.description, "education", eduTitle, "description", edu.id)
        }

        // 5. Projects
        resume.projects.forEach { proj ->
            val projTitle = if (proj.name.isNotBlank()) "Project (${proj.name})" else "Project"
            scan(proj.name, "projects", projTitle, "name", proj.id)
            scan(proj.description, "projects", projTitle, "description", proj.id)
            scan(proj.role, "projects", projTitle, "role", proj.id)
        }

        // 6. Custom Sections
        resume.customSections.forEach { cs ->
            cs.items.forEach { item ->
                val csTitle = if (cs.title.isNotBlank()) cs.title else "Custom Section"
                scan(item.title, "custom", csTitle, "title", item.id)
                scan(item.subtitle, "custom", csTitle, "subtitle", item.id)
                scan(item.description, "custom", csTitle, "description", item.id)
            }
        }

        val spelling = issues.filter { it.type == IssueType.SPELLING }
        val grammar = issues.filter {
            it.type == IssueType.GRAMMAR ||
            it.type == IssueType.PUNCTUATION ||
            it.type == IssueType.REPEATED_WORD ||
            it.type == IssueType.CAPITALIZATION
        }
        val style = issues.filter { it.type == IssueType.STYLE_ACTION_VERB }

        // Calculate score (penalize errors more than suggestions)
        var deduction = (spelling.size * 5) + (grammar.size * 3) + (style.size * 2)
        val score = (100 - deduction).coerceIn(40, 100)

        // Estimated reading time (~200 words per minute for HR scanning)
        val readingSeconds = ((totalWords / 200.0) * 60).toInt().coerceAtLeast(if (totalWords > 0) 10 else 0)

        return AnalysisReport(
            totalIssues = issues.size,
            issues = issues,
            spellingIssues = spelling,
            grammarIssues = grammar,
            styleIssues = style,
            score = score,
            wordCount = totalWords,
            characterCount = totalChars,
            readingTimeSeconds = readingSeconds
        )
    }

    /**
     * Analyze a single string value for inline/live checking
     */
    fun analyzeSingleText(
        text: String,
        sectionKey: String = "general",
        sectionTitle: String = "Text Section",
        targetField: String = "content",
        targetId: String? = null
    ): List<TextIssue> {
        if (text.isBlank()) return emptyList()

        val issues = mutableListOf<TextIssue>()

        // 1. Check Spelling Typos
        SPELLING_DICTIONARY.forEach { (typo, correction) ->
            val regex = Regex("\\b(?i)${Regex.escape(typo)}\\b")
            regex.findAll(text).forEach { match ->
                val matchedText = match.value
                val isCapitalized = matchedText.first().isUpperCase()
                val suggested = if (isCapitalized) correction.replaceFirstChar { it.uppercase() } else correction

                issues.add(
                    TextIssue(
                        sectionKey = sectionKey,
                        targetId = targetId,
                        targetField = targetField,
                        sectionTitle = sectionTitle,
                        type = IssueType.SPELLING,
                        severity = IssueSeverity.ERROR,
                        originalText = matchedText,
                        suggestedText = suggested,
                        explanation = "Possible spelling mistake: '$matchedText'. Suggested correction: '$suggested'.",
                        startIndex = match.range.first,
                        endIndex = match.range.last + 1,
                        contextSnippet = extractSnippet(text, match.range.first, match.range.last + 1)
                    )
                )
            }
        }

        // Arabic Spelling Typos
        ARABIC_TYPOS.forEach { (typo, correction) ->
            val regex = Regex(Regex.escape(typo))
            regex.findAll(text).forEach { match ->
                issues.add(
                    TextIssue(
                        sectionKey = sectionKey,
                        targetId = targetId,
                        targetField = targetField,
                        sectionTitle = sectionTitle,
                        type = IssueType.SPELLING,
                        severity = IssueSeverity.ERROR,
                        originalText = match.value,
                        suggestedText = correction,
                        explanation = "تصحيح لغوي مقترح: '$correction'",
                        startIndex = match.range.first,
                        endIndex = match.range.last + 1,
                        contextSnippet = extractSnippet(text, match.range.first, match.range.last + 1)
                    )
                )
            }
        }

        // 2. Check Repeated Adjacent Words ("the the", "in in", "من من")
        val repeatedWordsRegex = Regex("\\b([a-zA-Z\u0600-\u06FF]+)\\s+\\1\\b", RegexOption.IGNORE_CASE)
        repeatedWordsRegex.findAll(text).forEach { match ->
            val singleWord = match.groupValues[1]
            issues.add(
                TextIssue(
                    sectionKey = sectionKey,
                    targetId = targetId,
                    targetField = targetField,
                    sectionTitle = sectionTitle,
                    type = IssueType.REPEATED_WORD,
                    severity = IssueSeverity.WARNING,
                    originalText = match.value,
                    suggestedText = singleWord,
                    explanation = "Repeated word: '${match.value}'. Remove duplicate word.",
                    startIndex = match.range.first,
                    endIndex = match.range.last + 1,
                    contextSnippet = extractSnippet(text, match.range.first, match.range.last + 1)
                )
            )
        }

        // 3. Check Space Before Punctuation ("word ,", "sentence .", "كلمة ،")
        val spaceBeforePunctRegex = Regex("\\s+([,.;:!?،؟])")
        spaceBeforePunctRegex.findAll(text).forEach { match ->
            val punct = match.groupValues[1]
            issues.add(
                TextIssue(
                    sectionKey = sectionKey,
                    targetId = targetId,
                    targetField = targetField,
                    sectionTitle = sectionTitle,
                    type = IssueType.PUNCTUATION,
                    severity = IssueSeverity.WARNING,
                    originalText = match.value,
                    suggestedText = punct,
                    explanation = "Unnecessary space before punctuation '$punct'.",
                    startIndex = match.range.first,
                    endIndex = match.range.last + 1,
                    contextSnippet = extractSnippet(text, match.range.first, match.range.last + 1)
                )
            )
        }

        // 4. Check Double / Duplicate Punctuation (e.g. "..", ",,", "??")
        val doublePunctRegex = Regex("([,!?;:،؟])\\1+")
        doublePunctRegex.findAll(text).forEach { match ->
            val singlePunct = match.value.first().toString()
            issues.add(
                TextIssue(
                    sectionKey = sectionKey,
                    targetId = targetId,
                    targetField = targetField,
                    sectionTitle = sectionTitle,
                    type = IssueType.PUNCTUATION,
                    severity = IssueSeverity.WARNING,
                    originalText = match.value,
                    suggestedText = singlePunct,
                    explanation = "Duplicate punctuation '${match.value}'. Use single '$singlePunct'.",
                    startIndex = match.range.first,
                    endIndex = match.range.last + 1,
                    contextSnippet = extractSnippet(text, match.range.first, match.range.last + 1)
                )
            )
        }

        // 5. Standalone Lowercase 'i' -> 'I'
        val standaloneIRegex = Regex("(?<=\\s|^)i(?=\\s|[,.!?]|$)")
        standaloneIRegex.findAll(text).forEach { match ->
            issues.add(
                TextIssue(
                    sectionKey = sectionKey,
                    targetId = targetId,
                    targetField = targetField,
                    sectionTitle = sectionTitle,
                    type = IssueType.CAPITALIZATION,
                    severity = IssueSeverity.ERROR,
                    originalText = "i",
                    suggestedText = "I",
                    explanation = "Capitalize pronoun 'I'.",
                    startIndex = match.range.first,
                    endIndex = match.range.last + 1,
                    contextSnippet = extractSnippet(text, match.range.first, match.range.last + 1)
                )
            )
        }

        // 6. Weak Resume Phrases & Passive Voice -> Action Verbs
        WEAK_PHRASES.forEach { (weakPhrase, suggestions) ->
            val regex = Regex("\\b(?i)${Regex.escape(weakPhrase)}\\b")
            regex.findAll(text).forEach { match ->
                val bestSuggestion = suggestions.first()
                issues.add(
                    TextIssue(
                        sectionKey = sectionKey,
                        targetId = targetId,
                        targetField = targetField,
                        sectionTitle = sectionTitle,
                        type = IssueType.STYLE_ACTION_VERB,
                        severity = IssueSeverity.SUGGESTION,
                        originalText = match.value,
                        suggestedText = bestSuggestion,
                        explanation = "Weak passive phrase '${match.value}'. Replace with a powerful action verb: ${suggestions.joinToString(", ")}.",
                        startIndex = match.range.first,
                        endIndex = match.range.last + 1,
                        contextSnippet = extractSnippet(text, match.range.first, match.range.last + 1)
                    )
                )
            }
        }

        // 7. Clichés and Buzzwords
        CLICHE_PHRASES.forEach { (cliche, alternative) ->
            val regex = Regex("\\b(?i)${Regex.escape(cliche)}\\b")
            regex.findAll(text).forEach { match ->
                issues.add(
                    TextIssue(
                        sectionKey = sectionKey,
                        targetId = targetId,
                        targetField = targetField,
                        sectionTitle = sectionTitle,
                        type = IssueType.STYLE_ACTION_VERB,
                        severity = IssueSeverity.SUGGESTION,
                        originalText = match.value,
                        suggestedText = alternative.split("/").first().trim(),
                        explanation = "Overused cliché '${match.value}'. Consider using more descriptive language: $alternative.",
                        startIndex = match.range.first,
                        endIndex = match.range.last + 1,
                        contextSnippet = extractSnippet(text, match.range.first, match.range.last + 1)
                    )
                )
            }
        }

        return issues
    }

    private fun extractSnippet(fullText: String, start: Int, end: Int): String {
        val snippetStart = (start - 20).coerceAtLeast(0)
        val snippetEnd = (end + 20).coerceAtMost(fullText.length)
        val prefix = if (snippetStart > 0) "..." else ""
        val suffix = if (snippetEnd < fullText.length) "..." else ""
        return prefix + fullText.substring(snippetStart, snippetEnd).trim() + suffix
    }
}
