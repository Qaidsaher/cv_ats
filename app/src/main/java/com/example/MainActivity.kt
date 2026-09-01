package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.billing.BillingManager
import com.example.core.database.ResumeDatabase
import com.example.data.repository.ResumeRepositoryImpl
import com.example.data.repository.TemplateRepositoryImpl
import com.example.data.repository.UserPreferencesRepository
import com.example.domain.model.Resume
import com.example.feature.editor.CertificatesEditorScreen
import com.example.feature.editor.CustomSectionsEditorScreen
import com.example.feature.editor.EducationEditorScreen
import com.example.feature.editor.ExperienceEditorScreen
import com.example.feature.editor.LanguagesEditorScreen
import com.example.feature.editor.PersonalInfoEditorScreen
import com.example.feature.editor.ProjectsEditorScreen
import com.example.feature.editor.ReferencesEditorScreen
import com.example.feature.editor.ResumeEditorScreen
import com.example.feature.editor.ResumeEditorViewModel
import com.example.feature.editor.SkillsEditorScreen
import com.example.feature.editor.SummaryEditorScreen
import com.example.feature.export.ExportScreen
import com.example.feature.export.ExportViewModel
import com.example.feature.home.HomeScreen
import com.example.feature.home.HomeViewModel
import com.example.feature.onboarding.OnboardingScreen
import com.example.feature.preview.CvPreviewScreen
import com.example.feature.preview.PreviewViewModel
import com.example.feature.settings.SettingsScreen
import com.example.feature.settings.SettingsViewModel
import com.example.feature.templates.TemplatesGalleryScreen
import com.example.feature.templates.TemplatesViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appScope = CoroutineScope(Dispatchers.Main)
        val database = ResumeDatabase.getInstance(applicationContext)
        val resumeDao = database.resumeDao()
        val templateCacheDao = database.templateCacheDao()
        val skillDao = database.skillDao()

        val resumeRepository = ResumeRepositoryImpl(resumeDao, skillDao)
        val templateRepository = TemplateRepositoryImpl(applicationContext, templateCacheDao)
        val preferencesRepository = UserPreferencesRepository(applicationContext)
        val billingManager = BillingManager(applicationContext, preferencesRepository, appScope)

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Home Destination
                    composable("home") {
                        val homeViewModel = viewModel<HomeViewModel>(
                            factory = object : ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    return HomeViewModel(
                                        resumeRepository,
                                        templateRepository,
                                        preferencesRepository
                                    ) as T
                                }
                            }
                        )

                        HomeScreen(
                            viewModel = homeViewModel,
                            onNavigateToEditor = { resumeId ->
                                navController.navigate("editor/$resumeId")
                            },
                            onNavigateToPreview = { resumeId ->
                                navController.navigate("preview/$resumeId")
                            },
                            onNavigateToExport = { resumeId ->
                                navController.navigate("export/$resumeId")
                            },
                            onNavigateToTemplates = {
                                navController.navigate("templates")
                            },
                            onNavigateToPremium = {
                                navController.navigate("settings")
                            }
                        )
                    }

                    // Templates Destination
                    composable("templates") {
                        val templatesViewModel = viewModel<TemplatesViewModel>(
                            factory = object : ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    return TemplatesViewModel(
                                        templateRepository,
                                        resumeRepository,
                                        preferencesRepository
                                    ) as T
                                }
                            }
                        )

                        TemplatesGalleryScreen(
                            viewModel = templatesViewModel,
                            onCreateFromTemplate = { templateId ->
                                appScope.launch {
                                    val newResume = Resume(
                                        title = "New Resume",
                                        templateId = templateId
                                    )
                                    resumeRepository.saveResume(newResume)
                                    navController.navigate("editor/${newResume.id}")
                                }
                            },
                            onNavigateToPremium = {
                                navController.navigate("settings")
                            }
                        )
                    }

                    // Resume Editor Main Destination
                    composable(
                        route = "editor/{resumeId}",
                        arguments = listOf(navArgument("resumeId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val resumeId = backStackEntry.arguments?.getString("resumeId") ?: ""
                        val editorViewModel = viewModel<ResumeEditorViewModel>(
                            key = "editor_$resumeId",
                            factory = object : ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    return ResumeEditorViewModel(resumeRepository, resumeId, templateRepository) as T
                                }
                            }
                        )

                        ResumeEditorScreen(
                            viewModel = editorViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToSection = { sectionKey ->
                                navController.navigate("editor/$resumeId/section/$sectionKey")
                            },
                            onNavigateToPreview = {
                                navController.navigate("preview/$resumeId")
                            },
                            onNavigateToExport = {
                                navController.navigate("export/$resumeId")
                            }
                        )
                    }

                    // Editor Section Sub-Screens
                    composable(
                        route = "editor/{resumeId}/section/{sectionKey}",
                        arguments = listOf(
                            navArgument("resumeId") { type = NavType.StringType },
                            navArgument("sectionKey") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val resumeId = backStackEntry.arguments?.getString("resumeId") ?: ""
                        val sectionKey = backStackEntry.arguments?.getString("sectionKey") ?: ""
                        val editorViewModel = viewModel<ResumeEditorViewModel>(
                            key = "editor_$resumeId",
                            factory = object : ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    return ResumeEditorViewModel(resumeRepository, resumeId, templateRepository) as T
                                }
                            }
                        )

                        when (sectionKey) {
                            "personal" -> PersonalInfoEditorScreen(
                                viewModel = editorViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                            "summary" -> SummaryEditorScreen(
                                viewModel = editorViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                            "experience" -> ExperienceEditorScreen(
                                viewModel = editorViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                            "education" -> EducationEditorScreen(
                                viewModel = editorViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                            "skills" -> SkillsEditorScreen(
                                viewModel = editorViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                            "projects" -> ProjectsEditorScreen(
                                viewModel = editorViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                            "languages" -> LanguagesEditorScreen(
                                viewModel = editorViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                            "certificates" -> CertificatesEditorScreen(
                                viewModel = editorViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                            "references" -> ReferencesEditorScreen(
                                viewModel = editorViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                            "custom_sections" -> CustomSectionsEditorScreen(
                                viewModel = editorViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                            else -> navController.popBackStack()
                        }
                    }

                    // Live Preview Destination
                    composable(
                        route = "preview/{resumeId}",
                        arguments = listOf(navArgument("resumeId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val resumeId = backStackEntry.arguments?.getString("resumeId") ?: ""
                        val previewViewModel = viewModel<PreviewViewModel>(
                            key = "preview_$resumeId",
                            factory = object : ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    return PreviewViewModel(
                                        applicationContext,
                                        resumeRepository,
                                        templateRepository,
                                        resumeId
                                    ) as T
                                }
                            }
                        )

                        CvPreviewScreen(
                            viewModel = previewViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToExport = { rId ->
                                navController.navigate("export/$rId")
                            }
                        )
                    }

                    // Dedicated PDF Export Destination
                    composable(
                        route = "export/{resumeId}",
                        arguments = listOf(navArgument("resumeId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val resumeId = backStackEntry.arguments?.getString("resumeId") ?: ""
                        val exportViewModel = viewModel<ExportViewModel>(
                            key = "export_$resumeId",
                            factory = object : ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    val handle = SavedStateHandle(mapOf("resumeId" to resumeId))
                                    return ExportViewModel(
                                        handle,
                                        resumeRepository,
                                        templateRepository,
                                        billingManager,
                                        applicationContext
                                    ) as T
                                }
                            }
                        )

                        ExportScreen(
                            viewModel = exportViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToPremium = {
                                navController.navigate("settings")
                            }
                        )
                    }

                    // Settings Destination
                    composable("settings") {
                        val settingsViewModel = viewModel<SettingsViewModel>(
                            factory = object : ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    return SettingsViewModel(
                                        preferencesRepository,
                                        billingManager
                                    ) as T
                                }
                            }
                        )

                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToPremium = {
                                billingManager.purchaseLifetimePro { _, _ -> }
                            }
                        )
                    }

                    // Onboarding Destination
                    composable("onboarding") {
                        OnboardingScreen(
                            onFinishOnboarding = {
                                navController.navigate("home") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
