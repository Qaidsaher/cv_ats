package com.example.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.designsystem.components.AppTopBar
import com.example.core.designsystem.components.SectionRowCard
import com.example.domain.model.Resume
import com.example.feature.templates.AtsActiveLayoutBanner
import com.example.feature.templates.AtsTemplateSelectorModalSheet

data class EditorSectionMeta(
    val key: String,
    val titleRes: Int,
    val subtitle: String,
    val icon: ImageVector,
    val isCompleted: (Resume) -> Boolean,
    val itemCount: (Resume) -> Int?
)

@Composable
fun ResumeEditorScreen(
    viewModel: ResumeEditorViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSection: (String) -> Unit,
    onNavigateToPreview: (String) -> Unit,
    onNavigateToExport: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val resume = uiState.resume

    val sectionsMeta = listOf(
        EditorSectionMeta(
            key = "personal",
            titleRes = R.string.editor_section_personal,
            subtitle = "Name, contact, location & photo",
            icon = Icons.Default.Person,
            isCompleted = { it.personalInfo.fullName.isNotBlank() && it.personalInfo.fullName != "Untitled" },
            itemCount = { null }
        ),
        EditorSectionMeta(
            key = "summary",
            titleRes = R.string.editor_section_summary,
            subtitle = "Brief profile & career objectives",
            icon = Icons.Default.ShortText,
            isCompleted = { it.summary.isNotBlank() },
            itemCount = { null }
        ),
        EditorSectionMeta(
            key = "experience",
            titleRes = R.string.editor_section_experience,
            subtitle = "Work history and achievements",
            icon = Icons.Default.Business,
            isCompleted = { it.experiences.isNotEmpty() },
            itemCount = { it.experiences.size }
        ),
        EditorSectionMeta(
            key = "education",
            titleRes = R.string.editor_section_education,
            subtitle = "Degrees, universities & studies",
            icon = Icons.Default.School,
            isCompleted = { it.educations.isNotEmpty() },
            itemCount = { it.educations.size }
        ),
        EditorSectionMeta(
            key = "skills",
            titleRes = R.string.editor_section_skills,
            subtitle = "Core strengths and expertise",
            icon = Icons.Default.Psychology,
            isCompleted = { it.skills.isNotEmpty() },
            itemCount = { it.skills.size }
        ),
        EditorSectionMeta(
            key = "languages",
            titleRes = R.string.editor_section_languages,
            subtitle = "Spoken & written languages",
            icon = Icons.Default.Language,
            isCompleted = { it.languages.isNotEmpty() },
            itemCount = { it.languages.size }
        ),
        EditorSectionMeta(
            key = "projects",
            titleRes = R.string.editor_section_projects,
            subtitle = "Notable software and work projects",
            icon = Icons.Default.Code,
            isCompleted = { it.projects.isNotEmpty() },
            itemCount = { it.projects.size }
        ),
        EditorSectionMeta(
            key = "certificates",
            titleRes = R.string.editor_section_certificates,
            subtitle = "Licenses, certifications & awards",
            icon = Icons.Default.CardMembership,
            isCompleted = { it.certificates.isNotEmpty() },
            itemCount = { it.certificates.size }
        ),
        EditorSectionMeta(
            key = "references",
            titleRes = R.string.editor_section_references,
            subtitle = "Professional endorsements",
            icon = Icons.Default.Group,
            isCompleted = { it.references.isNotEmpty() },
            itemCount = { it.references.size }
        ),
        EditorSectionMeta(
            key = "custom",
            titleRes = R.string.editor_section_custom,
            subtitle = "Custom sections & extra details",
            icon = Icons.Default.DashboardCustomize,
            isCompleted = { it.customSections.isNotEmpty() },
            itemCount = { it.customSections.size }
        )
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = resume?.title ?: stringResource(R.string.editor_title),
                onBackClick = onNavigateBack,
                isSaving = uiState.isSaving,
                hasSaved = uiState.hasSaved,
                actions = {
                    IconButton(
                        onClick = { viewModel.undo() },
                        modifier = Modifier.testTag("editor_undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Undo"
                        )
                    }
                    IconButton(
                        onClick = { resume?.let { onNavigateToPreview(it.id) } },
                        modifier = Modifier.testTag("editor_preview_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Preview"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (resume != null) {
                Surface(
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigateToPreview(resume.id) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("editor_bottom_preview_button")
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.action_preview), fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onNavigateToExport(resume.id) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("editor_bottom_export_button")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.action_export), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                CircularProgressIndicator()
            }
        } else if (resume != null) {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Active ATS layout switcher banner
                item(key = "ats_layout_banner") {
                    AtsActiveLayoutBanner(
                        template = uiState.template,
                        onSwitchLayout = { viewModel.setTemplateSelectorVisible(true) },
                        modifier = Modifier.testTag("editor_switch_ats_layout_banner")
                    )
                }

                items(sectionsMeta, key = { it.key }) { meta ->
                    val isEnabled = resume.sectionVisibility[meta.key] ?: true
                    val completed = meta.isCompleted(resume)
                    val count = meta.itemCount(resume)

                    SectionRowCard(
                        title = stringResource(meta.titleRes),
                        subtitle = meta.subtitle,
                        icon = meta.icon,
                        isCompleted = completed,
                        itemCount = count,
                        isEnabled = isEnabled,
                        onToggleEnable = { enabled ->
                            viewModel.toggleSectionVisibility(meta.key, enabled)
                        },
                        onClick = {
                            onNavigateToSection(meta.key)
                        }
                    )
                }
            }
        }
    }

    if (uiState.showTemplateSelector && resume != null) {
        AtsTemplateSelectorModalSheet(
            templates = uiState.allTemplates,
            selectedTemplateId = resume.templateId,
            onSelectTemplate = { viewModel.selectTemplate(it) },
            onDismiss = { viewModel.setTemplateSelectorVisible(false) }
        )
    }
}
