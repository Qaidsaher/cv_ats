package com.example.feature.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ResumeRepository
import com.example.data.repository.TemplateRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.domain.model.TemplateSpec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TemplatesUiState(
    val selectedCategory: String = "all",
    val allTemplates: List<TemplateSpec> = emptyList(),
    val filteredTemplates: List<TemplateSpec> = emptyList(),
    val favoriteTemplateIds: Set<String> = emptySet(),
    val isProUser: Boolean = false,
    val isLoading: Boolean = false
)

class TemplatesViewModel(
    private val templateRepository: TemplateRepository,
    private val resumeRepository: ResumeRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val uiState: StateFlow<TemplatesUiState> = combine(
        templateRepository.getAllTemplates(),
        _selectedCategory,
        preferencesRepository.favoriteTemplates,
        preferencesRepository.isProUser
    ) { templates, category, favorites, isPro ->
        val filtered = when (category) {
            "all" -> templates
            "ats" -> templates.filter { it.isAtsFriendly }
            "professional" -> templates.filter { it.category.equals("professional", ignoreCase = true) }
            "modern" -> templates.filter { it.category.equals("modern", ignoreCase = true) }
            "arabic" -> templates.filter { it.category.equals("arabic", ignoreCase = true) || it.supportsRtl }
            "minimal" -> templates.filter { it.category.equals("minimal", ignoreCase = true) }
            "premium" -> templates.filter { it.isPremium }
            "favorites" -> templates.filter { favorites.contains(it.id) }
            else -> templates
        }
        TemplatesUiState(
            selectedCategory = category,
            allTemplates = templates,
            filteredTemplates = filtered,
            favoriteTemplateIds = favorites,
            isProUser = isPro,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TemplatesUiState(isLoading = true)
    )

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleFavorite(templateId: String) {
        viewModelScope.launch {
            preferencesRepository.toggleFavoriteTemplate(templateId)
        }
    }

    fun applyTemplateToResume(resumeId: String, templateId: String, onApplied: () -> Unit) {
        viewModelScope.launch {
            resumeRepository.updateTemplate(resumeId, templateId)
            onApplied()
        }
    }
}
