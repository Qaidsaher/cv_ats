package com.example.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ResumeRepository
import com.example.data.repository.TemplateRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.domain.model.Resume
import com.example.domain.model.SampleData
import com.example.domain.model.TemplateSpec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class HomeUiState(
    val resumes: List<Resume> = emptyList(),
    val searchQuery: String = "",
    val filteredResumes: List<Resume> = emptyList(),
    val templates: List<TemplateSpec> = emptyList(),
    val isProUser: Boolean = false,
    val isLoading: Boolean = false,
    val recentlyDeletedResume: Resume? = null
)

class HomeViewModel(
    private val resumeRepository: ResumeRepository,
    private val templateRepository: TemplateRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _recentlyDeletedResume = MutableStateFlow<Resume?>(null)
    val recentlyDeletedResume: StateFlow<Resume?> = _recentlyDeletedResume.asStateFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        resumeRepository.getAllResumes(),
        _searchQuery,
        templateRepository.getAllTemplates(),
        preferencesRepository.isProUser,
        _recentlyDeletedResume
    ) { resumes, query, templates, isPro, deletedResume ->
        val filtered = if (query.isBlank()) {
            resumes
        } else {
            resumes.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.personalInfo.fullName.contains(query, ignoreCase = true) ||
                it.personalInfo.professionalTitle.contains(query, ignoreCase = true)
            }
        }
        HomeUiState(
            resumes = resumes,
            searchQuery = query,
            filteredResumes = filtered,
            templates = templates,
            isProUser = isPro,
            isLoading = false,
            recentlyDeletedResume = deletedResume
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun createResume(
        title: String,
        language: String,
        useSample: Boolean,
        templateId: String = if (language == "ar") "arabic_professional" else "modern_green",
        onCreated: (String) -> Unit
    ) {
        viewModelScope.launch {
            val newResume = if (useSample) {
                if (language == "ar") {
                    SampleData.createArabicSampleResume(templateId).copy(
                        id = UUID.randomUUID().toString(),
                        title = title.ifBlank { "سيرة ذاتية - مهندس برمجيات" }
                    )
                } else {
                    SampleData.createEnglishSampleResume(templateId).copy(
                        id = UUID.randomUUID().toString(),
                        title = title.ifBlank { "Software Engineer Resume" }
                    )
                }
            } else {
                Resume(
                    id = UUID.randomUUID().toString(),
                    title = title.ifBlank { if (language == "ar") "سيرة ذاتية جديدة" else "New Resume" },
                    templateId = templateId,
                    language = language,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            }

            resumeRepository.saveResume(newResume)
            onCreated(newResume.id)
        }
    }

    fun duplicateResume(resumeId: String, onDuplicated: (String) -> Unit) {
        viewModelScope.launch {
            val newId = resumeRepository.duplicateResume(resumeId)
            if (newId != null) {
                onDuplicated(newId)
            }
        }
    }

    fun renameResume(resumeId: String, newTitle: String) {
        viewModelScope.launch {
            resumeRepository.renameResume(resumeId, newTitle)
        }
    }

    fun deleteResume(resume: Resume) {
        viewModelScope.launch {
            _recentlyDeletedResume.value = resume
            resumeRepository.deleteResume(resume.id)
        }
    }

    fun undoDelete() {
        val deleted = _recentlyDeletedResume.value ?: return
        viewModelScope.launch {
            resumeRepository.saveResume(deleted)
            _recentlyDeletedResume.value = null
        }
    }
}
