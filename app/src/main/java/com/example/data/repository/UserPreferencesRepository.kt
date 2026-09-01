package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_APP_LANGUAGE = stringPreferencesKey("app_language") // "system", "en", "ar"
        val KEY_APP_THEME = stringPreferencesKey("app_theme") // "system", "light", "dark"
        val KEY_DEFAULT_RESUME_LANG = stringPreferencesKey("default_resume_lang") // "en", "ar"
        val KEY_DEFAULT_PAGE_FORMAT = stringPreferencesKey("default_page_format") // "A4", "LETTER"
        val KEY_IS_PRO_USER = booleanPreferencesKey("is_pro_user")
        val KEY_FAVORITE_TEMPLATES = stringSetPreferencesKey("favorite_templates")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val appLanguage: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_APP_LANGUAGE] ?: "system"
    }

    val appTheme: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_APP_THEME] ?: "system"
    }

    val defaultResumeLang: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_RESUME_LANG] ?: "en"
    }

    val defaultPageFormat: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_PAGE_FORMAT] ?: "A4"
    }

    val isProUser: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_PRO_USER] ?: false
    }

    val favoriteTemplates: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_FAVORITE_TEMPLATES] ?: emptySet()
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setAppLanguage(language: String) {
        context.dataStore.edit { it[KEY_APP_LANGUAGE] = language }
    }

    suspend fun setAppTheme(theme: String) {
        context.dataStore.edit { it[KEY_APP_THEME] = theme }
    }

    suspend fun setDefaultResumeLang(lang: String) {
        context.dataStore.edit { it[KEY_DEFAULT_RESUME_LANG] = lang }
    }

    suspend fun setDefaultPageFormat(format: String) {
        context.dataStore.edit { it[KEY_DEFAULT_PAGE_FORMAT] = format }
    }

    suspend fun setProUser(isPro: Boolean) {
        context.dataStore.edit { it[KEY_IS_PRO_USER] = isPro }
    }

    suspend fun toggleFavoriteTemplate(templateId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_FAVORITE_TEMPLATES] ?: emptySet()
            if (current.contains(templateId)) {
                prefs[KEY_FAVORITE_TEMPLATES] = current - templateId
            } else {
                prefs[KEY_FAVORITE_TEMPLATES] = current + templateId
            }
        }
    }
}
