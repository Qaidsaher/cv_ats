package com.example.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.billing.BillingManager
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val appLanguage: String = "system",
    val appTheme: String = "system",
    val defaultResumeLang: String = "en",
    val defaultPageFormat: String = "A4",
    val isProUser: Boolean = false,
    val infoMessage: String? = null
)

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val billingManager: BillingManager
) : ViewModel() {

    private val _infoMessage = MutableStateFlow<String?>(null)
    val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.appLanguage,
        preferencesRepository.appTheme,
        preferencesRepository.defaultResumeLang,
        combine(
            preferencesRepository.defaultPageFormat,
            billingManager.isProUser,
            _infoMessage
        ) { pageFormat, isPro, msg -> Triple(pageFormat, isPro, msg) }
    ) { lang, theme, resumeLang, (pageFormat, isPro, msg) ->
        SettingsUiState(
            appLanguage = lang,
            appTheme = theme,
            defaultResumeLang = resumeLang,
            defaultPageFormat = pageFormat,
            isProUser = isPro,
            infoMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setAppLanguage(language: String) {
        viewModelScope.launch {
            preferencesRepository.setAppLanguage(language)
        }
    }

    fun setAppTheme(theme: String) {
        viewModelScope.launch {
            preferencesRepository.setAppTheme(theme)
        }
    }

    fun setDefaultResumeLang(lang: String) {
        viewModelScope.launch {
            preferencesRepository.setDefaultResumeLang(lang)
        }
    }

    fun setDefaultPageFormat(format: String) {
        viewModelScope.launch {
            preferencesRepository.setDefaultPageFormat(format)
        }
    }

    fun restorePurchases() {
        billingManager.restorePurchases { success, message ->
            _infoMessage.value = message
        }
    }

    fun clearInfoMessage() {
        _infoMessage.value = null
    }
}
