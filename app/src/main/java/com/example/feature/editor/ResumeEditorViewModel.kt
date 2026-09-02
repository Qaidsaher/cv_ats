package com.example.feature.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.analysis.AnalysisReport
import com.example.core.analysis.IssueType
import com.example.core.analysis.ResumeScoreEngine
import com.example.core.analysis.ResumeScoreGrade
import com.example.core.analysis.ResumeScoreReport
import com.example.core.analysis.TextAnalysisEngine
import com.example.core.analysis.TextIssue
import com.example.data.repository.ResumeRepository
import com.example.data.repository.TemplateRepository
import com.example.domain.model.Certificate
import com.example.domain.model.CustomSection
import com.example.domain.model.Education
import com.example.domain.model.Experience
import com.example.domain.model.LanguageSkill
import com.example.domain.model.PersonalInformation
import com.example.domain.model.Project
import com.example.domain.model.Reference
import com.example.domain.model.Resume
import com.example.domain.model.Skill
import com.example.domain.model.TemplateSpec
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditorUiState(
    val resume: Resume? = null,
    val template: TemplateSpec? = null,
    val allTemplates: List<TemplateSpec> = emptyList(),
    val showTemplateSelector: Boolean = false,
    val showAnalysisSheet: Boolean = false,
    val analysisReport: AnalysisReport = AnalysisReport(0, emptyList(), emptyList(), emptyList(), emptyList(), 100, 0, 0, 0),
    val dismissedIssueIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val hasSaved: Boolean = false,
    val errorMessage: String? = null
) {
    val scoreReport: ResumeScoreReport
        get() = if (resume != null) {
            ResumeScoreEngine.evaluateResume(resume)
        } else {
            ResumeScoreReport(
                overallScore = 0,
                grade = ResumeScoreGrade.NEEDS_WORK,
                sectionBreakdowns = emptyList(),
                tips = emptyList(),
                completedItems = emptyList(),
                missingItems = emptyList()
            )
        }

    val activeIssues: List<TextIssue>
        get() = analysisReport.issues.filter { it.id !in dismissedIssueIds }
    
    val activeSpellingIssues: List<TextIssue>
        get() = analysisReport.spellingIssues.filter { it.id !in dismissedIssueIds }

    val activeGrammarIssues: List<TextIssue>
        get() = analysisReport.grammarIssues.filter { it.id !in dismissedIssueIds }

    val activeStyleIssues: List<TextIssue>
        get() = analysisReport.styleIssues.filter { it.id !in dismissedIssueIds }
}

class ResumeEditorViewModel(
    private val resumeRepository: ResumeRepository,
    private val resumeId: String,
    private val templateRepository: TemplateRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var autoSaveJob: Job? = null
    private val undoStack = ArrayDeque<Resume>()

    init {
        loadResume()
    }

    private fun loadResume() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val res = resumeRepository.getResumeByIdDirect(resumeId)
            if (res != null) {
                val currentTemplate = templateRepository?.getTemplateById(res.templateId)
                val report = TextAnalysisEngine.analyzeResume(res)
                _uiState.value = _uiState.value.copy(
                    resume = res,
                    template = currentTemplate,
                    analysisReport = report,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Resume not found"
                )
            }

            templateRepository?.getAllTemplates()?.collect { list ->
                _uiState.value = _uiState.value.copy(allTemplates = list)
            }
        }
    }

    fun selectTemplate(templateId: String) {
        val current = _uiState.value.resume ?: return
        pushUndoState(current)
        val updated = current.copy(templateId = templateId)
        viewModelScope.launch {
            val newTemplate = templateRepository?.getTemplateById(templateId)
            _uiState.value = _uiState.value.copy(
                resume = updated,
                template = newTemplate ?: _uiState.value.template
            )
            scheduleAutoSave(updated)
        }
    }

    fun setTemplateSelectorVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showTemplateSelector = visible)
    }

    fun setAnalysisSheetVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showAnalysisSheet = visible)
    }

    fun dismissIssue(issueId: String) {
        _uiState.value = _uiState.value.copy(
            dismissedIssueIds = _uiState.value.dismissedIssueIds + issueId
        )
    }

    fun applyIssueFix(issue: TextIssue) {
        val current = _uiState.value.resume ?: return
        pushUndoState(current)

        val updatedResume = when (issue.sectionKey) {
            "personal" -> {
                val pi = current.personalInfo
                val updatedPi = when (issue.targetField) {
                    "professionalTitle" -> pi.copy(professionalTitle = replaceInText(pi.professionalTitle, issue.originalText, issue.suggestedText))
                    "address" -> pi.copy(address = replaceInText(pi.address, issue.originalText, issue.suggestedText))
                    else -> pi
                }
                current.copy(personalInfo = updatedPi)
            }
            "summary" -> {
                current.copy(summary = replaceInText(current.summary, issue.originalText, issue.suggestedText))
            }
            "experience" -> {
                val updatedExps = current.experiences.map { exp ->
                    if (exp.id == issue.targetId) {
                        when (issue.targetField) {
                            "jobTitle" -> exp.copy(jobTitle = replaceInText(exp.jobTitle, issue.originalText, issue.suggestedText))
                            "description" -> exp.copy(description = replaceInText(exp.description, issue.originalText, issue.suggestedText))
                            else -> exp
                        }
                    } else exp
                }
                current.copy(experiences = updatedExps)
            }
            "education" -> {
                val updatedEdus = current.educations.map { edu ->
                    if (edu.id == issue.targetId) {
                        when (issue.targetField) {
                            "degree" -> edu.copy(degree = replaceInText(edu.degree, issue.originalText, issue.suggestedText))
                            "fieldOfStudy" -> edu.copy(fieldOfStudy = replaceInText(edu.fieldOfStudy, issue.originalText, issue.suggestedText))
                            "description" -> edu.copy(description = replaceInText(edu.description, issue.originalText, issue.suggestedText))
                            else -> edu
                        }
                    } else edu
                }
                current.copy(educations = updatedEdus)
            }
            "projects" -> {
                val updatedProjs = current.projects.map { proj ->
                    if (proj.id == issue.targetId) {
                        when (issue.targetField) {
                            "name" -> proj.copy(name = replaceInText(proj.name, issue.originalText, issue.suggestedText))
                            "role" -> proj.copy(role = replaceInText(proj.role, issue.originalText, issue.suggestedText))
                            "description" -> proj.copy(description = replaceInText(proj.description, issue.originalText, issue.suggestedText))
                            else -> proj
                        }
                    } else proj
                }
                current.copy(projects = updatedProjs)
            }
            "custom" -> {
                val updatedCustom = current.customSections.map { cs ->
                    val updatedItems = cs.items.map { item ->
                        if (item.id == issue.targetId) {
                            when (issue.targetField) {
                                "title" -> item.copy(title = replaceInText(item.title, issue.originalText, issue.suggestedText))
                                "subtitle" -> item.copy(subtitle = replaceInText(item.subtitle, issue.originalText, issue.suggestedText))
                                "description" -> item.copy(description = replaceInText(item.description, issue.originalText, issue.suggestedText))
                                else -> item
                            }
                        } else item
                    }
                    cs.copy(items = updatedItems)
                }
                current.copy(customSections = updatedCustom)
            }
            else -> current
        }

        val newReport = TextAnalysisEngine.analyzeResume(updatedResume)
        _uiState.value = _uiState.value.copy(
            resume = updatedResume,
            analysisReport = newReport
        )
        scheduleAutoSave(updatedResume)
    }

    fun applyAllSpellingFixes() {
        val current = _uiState.value.resume ?: return
        val spellingIssues = _uiState.value.activeSpellingIssues
        if (spellingIssues.isEmpty()) return

        pushUndoState(current)
        var updatedResume = current

        spellingIssues.forEach { issue ->
            updatedResume = when (issue.sectionKey) {
                "personal" -> {
                    val pi = updatedResume.personalInfo
                    val updatedPi = when (issue.targetField) {
                        "professionalTitle" -> pi.copy(professionalTitle = replaceInText(pi.professionalTitle, issue.originalText, issue.suggestedText))
                        "address" -> pi.copy(address = replaceInText(pi.address, issue.originalText, issue.suggestedText))
                        else -> pi
                    }
                    updatedResume.copy(personalInfo = updatedPi)
                }
                "summary" -> {
                    updatedResume.copy(summary = replaceInText(updatedResume.summary, issue.originalText, issue.suggestedText))
                }
                "experience" -> {
                    val updatedExps = updatedResume.experiences.map { exp ->
                        if (exp.id == issue.targetId) {
                            when (issue.targetField) {
                                "jobTitle" -> exp.copy(jobTitle = replaceInText(exp.jobTitle, issue.originalText, issue.suggestedText))
                                "description" -> exp.copy(description = replaceInText(exp.description, issue.originalText, issue.suggestedText))
                                else -> exp
                            }
                        } else exp
                    }
                    updatedResume.copy(experiences = updatedExps)
                }
                "education" -> {
                    val updatedEdus = updatedResume.educations.map { edu ->
                        if (edu.id == issue.targetId) {
                            when (issue.targetField) {
                                "degree" -> edu.copy(degree = replaceInText(edu.degree, issue.originalText, issue.suggestedText))
                                "fieldOfStudy" -> edu.copy(fieldOfStudy = replaceInText(edu.fieldOfStudy, issue.originalText, issue.suggestedText))
                                "description" -> edu.copy(description = replaceInText(edu.description, issue.originalText, issue.suggestedText))
                                else -> edu
                            }
                        } else edu
                    }
                    updatedResume.copy(educations = updatedEdus)
                }
                "projects" -> {
                    val updatedProjs = updatedResume.projects.map { proj ->
                        if (proj.id == issue.targetId) {
                            when (issue.targetField) {
                                "name" -> proj.copy(name = replaceInText(proj.name, issue.originalText, issue.suggestedText))
                                "role" -> proj.copy(role = replaceInText(proj.role, issue.originalText, issue.suggestedText))
                                "description" -> proj.copy(description = replaceInText(proj.description, issue.originalText, issue.suggestedText))
                                else -> proj
                            }
                        } else proj
                    }
                    updatedResume.copy(projects = updatedProjs)
                }
                "custom" -> {
                    val updatedCustom = updatedResume.customSections.map { cs ->
                        val updatedItems = cs.items.map { item ->
                            if (item.id == issue.targetId) {
                                when (issue.targetField) {
                                    "title" -> item.copy(title = replaceInText(item.title, issue.originalText, issue.suggestedText))
                                    "subtitle" -> item.copy(subtitle = replaceInText(item.subtitle, issue.originalText, issue.suggestedText))
                                    "description" -> item.copy(description = replaceInText(item.description, issue.originalText, issue.suggestedText))
                                    else -> item
                                }
                            } else item
                        }
                        cs.copy(items = updatedItems)
                    }
                    updatedResume.copy(customSections = updatedCustom)
                }
                else -> updatedResume
            }
        }

        val newReport = TextAnalysisEngine.analyzeResume(updatedResume)
        _uiState.value = _uiState.value.copy(
            resume = updatedResume,
            analysisReport = newReport
        )
        scheduleAutoSave(updatedResume)
    }

    private fun replaceInText(target: String, original: String, replacement: String): String {
        return target.replace(original, replacement)
    }

    private fun pushUndoState(current: Resume) {
        if (undoStack.size > 20) undoStack.removeFirst()
        undoStack.addLast(current)
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeLast()
            _uiState.value = _uiState.value.copy(resume = previous)
            scheduleAutoSave(previous)
        }
    }

    fun updatePersonalInfo(personalInfo: PersonalInformation) {
        val current = _uiState.value.resume ?: return
        pushUndoState(current)
        val updated = current.copy(personalInfo = personalInfo)
        _uiState.value = _uiState.value.copy(resume = updated)
        scheduleAutoSave(updated)
    }

    fun updateSummary(summary: String) {
        val current = _uiState.value.resume ?: return
        pushUndoState(current)
        val updated = current.copy(summary = summary)
        _uiState.value = _uiState.value.copy(resume = updated)
        scheduleAutoSave(updated)
    }

    fun updateExperiences(experiences: List<Experience>) {
        val current = _uiState.value.resume ?: return
        pushUndoState(current)
        val updated = current.copy(experiences = experiences)
        _uiState.value = _uiState.value.copy(resume = updated)
        scheduleAutoSave(updated)
    }

    fun updateEducations(educations: List<Education>) {
        val current = _uiState.value.resume ?: return
        pushUndoState(current)
        val updated = current.copy(educations = educations)
        _uiState.value = _uiState.value.copy(resume = updated)
        scheduleAutoSave(updated)
    }

    fun updateSkills(skills: List<Skill>) {
        val current = _uiState.value.resume ?: return
        pushUndoState(current)
        val updated = current.copy(skills = skills)
        _uiState.value = _uiState.value.copy(resume = updated)
        scheduleAutoSave(updated)
    }

    fun updateLanguages(languages: List<LanguageSkill>) {
        val current = _uiState.value.resume ?: return
        pushUndoState(current)
        val updated = current.copy(languages = languages)
        _uiState.value = _uiState.value.copy(resume = updated)
        scheduleAutoSave(updated)
    }

    fun updateProjects(projects: List<Project>) {
        val current = _uiState.value.resume ?: return
        pushUndoState(current)
        val updated = current.copy(projects = projects)
        _uiState.value = _uiState.value.copy(resume = updated)
        scheduleAutoSave(updated)
    }

    fun updateCertificates(certificates: List<Certificate>) {
        val current = _uiState.value.resume ?: return
        pushUndoState(current)
        val updated = current.copy(certificates = certificates)
        _uiState.value = _uiState.value.copy(resume = updated)
        scheduleAutoSave(updated)
    }

    fun updateReferences(references: List<Reference>) {
        val current = _uiState.value.resume ?: return
        pushUndoState(current)
        val updated = current.copy(references = references)
        _uiState.value = _uiState.value.copy(resume = updated)
        scheduleAutoSave(updated)
    }

    fun updateCustomSections(customSections: List<CustomSection>) {
        val current = _uiState.value.resume ?: return
        pushUndoState(current)
        val updated = current.copy(customSections = customSections)
        _uiState.value = _uiState.value.copy(resume = updated)
        scheduleAutoSave(updated)
    }

    fun toggleSectionVisibility(sectionKey: String, isVisible: Boolean) {
        val current = _uiState.value.resume ?: return
        pushUndoState(current)
        val updatedMap = current.sectionVisibility.toMutableMap().apply {
            put(sectionKey, isVisible)
        }
        val updated = current.copy(sectionVisibility = updatedMap)
        _uiState.value = _uiState.value.copy(resume = updated)
        scheduleAutoSave(updated)
    }

    fun reorderSections(newOrder: List<String>) {
        val current = _uiState.value.resume ?: return
        pushUndoState(current)
        val updated = current.copy(sectionOrder = newOrder)
        _uiState.value = _uiState.value.copy(resume = updated)
        scheduleAutoSave(updated)
    }

    private fun scheduleAutoSave(resume: Resume) {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, hasSaved = false)
            delay(500) // 500ms debounce
            resumeRepository.saveResume(resume)
            _uiState.value = _uiState.value.copy(isSaving = false, hasSaved = true)
            delay(1500)
            _uiState.value = _uiState.value.copy(hasSaved = false)
        }
    }
}
