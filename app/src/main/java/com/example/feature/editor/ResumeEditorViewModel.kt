package com.example.feature.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ResumeRepository
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditorUiState(
    val resume: Resume? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val hasSaved: Boolean = false,
    val errorMessage: String? = null
)

class ResumeEditorViewModel(
    private val resumeRepository: ResumeRepository,
    private val resumeId: String
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
                _uiState.value = _uiState.value.copy(resume = res, isLoading = false)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Resume not found"
                )
            }
        }
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
