package com.example.feature.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.core.designsystem.components.AppTextField
import com.example.core.designsystem.components.AppTopBar
import com.example.domain.model.Certificate
import com.example.domain.model.CustomSection
import com.example.domain.model.CustomSectionItem
import com.example.domain.model.Education
import com.example.domain.model.Experience
import com.example.domain.model.LanguageSkill
import com.example.domain.model.PersonalInformation
import com.example.domain.model.Project
import com.example.domain.model.Reference
import com.example.domain.model.Skill
import java.util.UUID

@Composable
fun PersonalInfoEditorScreen(
    viewModel: ResumeEditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val personalInfo = uiState.resume?.personalInfo ?: PersonalInformation()

    // Photo picker launcher (Google Play policy compliant zero-permission Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updatePersonalInfo(personalInfo.copy(profilePhotoUri = uri.toString()))
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.editor_section_personal),
                onBackClick = onNavigateBack,
                isSaving = uiState.isSaving,
                hasSaved = uiState.hasSaved
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Profile Photo Picker Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        if (!personalInfo.profilePhotoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = personalInfo.profilePhotoUri,
                                contentDescription = "Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Add Photo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Text(
                                text = if (personalInfo.profilePhotoUri == null) stringResource(R.string.field_profile_photo) else stringResource(R.string.field_change_photo)
                            )
                        }

                        if (personalInfo.profilePhotoUri != null) {
                            TextButton(
                                onClick = { viewModel.updatePersonalInfo(personalInfo.copy(profilePhotoUri = null)) }
                            ) {
                                Text(
                                    text = stringResource(R.string.field_remove_photo),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Form Fields
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(
                    value = personalInfo.firstName,
                    onValueChange = { viewModel.updatePersonalInfo(personalInfo.copy(firstName = it)) },
                    label = stringResource(R.string.field_first_name),
                    modifier = Modifier.weight(1f),
                    testTag = "input_first_name"
                )
                AppTextField(
                    value = personalInfo.lastName,
                    onValueChange = { viewModel.updatePersonalInfo(personalInfo.copy(lastName = it)) },
                    label = stringResource(R.string.field_last_name),
                    modifier = Modifier.weight(1f),
                    testTag = "input_last_name"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = personalInfo.professionalTitle,
                onValueChange = { viewModel.updatePersonalInfo(personalInfo.copy(professionalTitle = it)) },
                label = stringResource(R.string.field_job_title),
                leadingIcon = Icons.Default.Work,
                testTag = "input_job_title"
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = personalInfo.email,
                onValueChange = { viewModel.updatePersonalInfo(personalInfo.copy(email = it)) },
                label = stringResource(R.string.field_email),
                leadingIcon = Icons.Default.Email,
                testTag = "input_email"
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = personalInfo.phone,
                onValueChange = { viewModel.updatePersonalInfo(personalInfo.copy(phone = it)) },
                label = stringResource(R.string.field_phone),
                leadingIcon = Icons.Default.Phone,
                testTag = "input_phone"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(
                    value = personalInfo.city,
                    onValueChange = { viewModel.updatePersonalInfo(personalInfo.copy(city = it)) },
                    label = stringResource(R.string.field_city),
                    modifier = Modifier.weight(1f),
                    testTag = "input_city"
                )
                AppTextField(
                    value = personalInfo.country,
                    onValueChange = { viewModel.updatePersonalInfo(personalInfo.copy(country = it)) },
                    label = stringResource(R.string.field_country),
                    modifier = Modifier.weight(1f),
                    testTag = "input_country"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = personalInfo.address,
                onValueChange = { viewModel.updatePersonalInfo(personalInfo.copy(address = it)) },
                label = stringResource(R.string.field_address),
                leadingIcon = Icons.Default.LocationOn,
                testTag = "input_address"
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = personalInfo.linkedIn,
                onValueChange = { viewModel.updatePersonalInfo(personalInfo.copy(linkedIn = it)) },
                label = stringResource(R.string.field_linkedin),
                testTag = "input_linkedin"
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = personalInfo.gitHub,
                onValueChange = { viewModel.updatePersonalInfo(personalInfo.copy(gitHub = it)) },
                label = stringResource(R.string.field_github),
                testTag = "input_github"
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = personalInfo.website,
                onValueChange = { viewModel.updatePersonalInfo(personalInfo.copy(website = it)) },
                label = stringResource(R.string.field_website),
                testTag = "input_website"
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SummaryEditorScreen(
    viewModel: ResumeEditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val summary = uiState.resume?.summary ?: ""

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.editor_section_summary),
                onBackClick = onNavigateBack,
                isSaving = uiState.isSaving,
                hasSaved = uiState.hasSaved
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Write a concise summary highlighting your years of experience, core expertise, and key achievements.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = summary,
                onValueChange = { viewModel.updateSummary(it) },
                label = stringResource(R.string.field_summary),
                singleLine = false,
                minLines = 8,
                maxLines = 14,
                testTag = "input_summary"
            )

            InlineTextAnalysisHelper(
                text = summary,
                onApplySuggestion = { original, replacement ->
                    viewModel.updateSummary(summary.replace(original, replacement))
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${summary.length} characters • ~${summary.split("\\s+".toRegex()).filter { it.isNotBlank() }.size} words",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun ExperienceEditorScreen(
    viewModel: ResumeEditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val experiences = uiState.resume?.experiences ?: emptyList()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.editor_section_experience),
                onBackClick = onNavigateBack,
                isSaving = uiState.isSaving,
                hasSaved = uiState.hasSaved
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            WorkExperienceTimelineComponent(
                experiences = experiences,
                onUpdateExperiences = { viewModel.updateExperiences(it) }
            )
        }
    }
}

@Composable
fun EducationEditorScreen(
    viewModel: ResumeEditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val educations = uiState.resume?.educations ?: emptyList()

    var editingEducation by remember { mutableStateOf<Education?>(null) }
    var isNewEducation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.editor_section_education),
                onBackClick = onNavigateBack,
                isSaving = uiState.isSaving,
                hasSaved = uiState.hasSaved
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingEducation = Education(id = UUID.randomUUID().toString())
                    isNewEducation = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_education")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Education")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (educations.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text(
                    text = "No education added yet. Tap + to add.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(educations, key = { it.id }) { edu ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${edu.degree}${if (edu.fieldOfStudy.isNotBlank()) " in ${edu.fieldOfStudy}" else ""}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = edu.institution.ifBlank { "University / School" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Row {
                                    IconButton(onClick = {
                                        editingEducation = edu
                                        isNewEducation = false
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = {
                                        viewModel.updateEducations(educations.filter { it.id != edu.id })
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                            Text(
                                text = "${edu.startDate} - ${if (edu.currentlyStudying) "Present" else edu.endDate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (editingEducation != null) {
        EducationEditDialog(
            education = editingEducation!!,
            onDismiss = { editingEducation = null },
            onSave = { updated ->
                val newList = if (isNewEducation) {
                    educations + updated
                } else {
                    educations.map { if (it.id == updated.id) updated else it }
                }
                viewModel.updateEducations(newList)
                editingEducation = null
            }
        )
    }
}

@Composable
private fun EducationEditDialog(
    education: Education,
    onDismiss: () -> Unit,
    onSave: (Education) -> Unit
) {
    var degree by remember { mutableStateOf(education.degree) }
    var fieldOfStudy by remember { mutableStateOf(education.fieldOfStudy) }
    var institution by remember { mutableStateOf(education.institution) }
    var location by remember { mutableStateOf(education.location) }
    var startDate by remember { mutableStateOf(education.startDate) }
    var endDate by remember { mutableStateOf(education.endDate) }
    var currentlyStudying by remember { mutableStateOf(education.currentlyStudying) }
    var description by remember { mutableStateOf(education.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.editor_section_education), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                AppTextField(
                    value = degree,
                    onValueChange = { degree = it },
                    label = stringResource(R.string.field_degree),
                    placeholder = "e.g. Bachelor of Science",
                    testTag = "input_edu_degree"
                )
                AppTextField(
                    value = fieldOfStudy,
                    onValueChange = { fieldOfStudy = it },
                    label = stringResource(R.string.field_field_of_study),
                    placeholder = "e.g. Computer Science",
                    testTag = "input_edu_field"
                )
                AppTextField(
                    value = institution,
                    onValueChange = { institution = it },
                    label = stringResource(R.string.field_institution),
                    testTag = "input_edu_institution"
                )
                AppTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = stringResource(R.string.field_location),
                    testTag = "input_edu_location"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = stringResource(R.string.field_start_date),
                        modifier = Modifier.weight(1f),
                        testTag = "input_edu_start_date"
                    )
                    if (!currentlyStudying) {
                        AppTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = stringResource(R.string.field_end_date),
                            modifier = Modifier.weight(1f),
                            testTag = "input_edu_end_date"
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { currentlyStudying = !currentlyStudying }
                ) {
                    Checkbox(
                        checked = currentlyStudying,
                        onCheckedChange = { currentlyStudying = it }
                    )
                    Text(stringResource(R.string.field_currently_studying))
                }
                AppTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = stringResource(R.string.field_description),
                    singleLine = false,
                    minLines = 3,
                    testTag = "input_edu_description"
                )

                InlineTextAnalysisHelper(
                    text = description,
                    onApplySuggestion = { original, replacement ->
                        description = description.replace(original, replacement)
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        education.copy(
                            degree = degree,
                            fieldOfStudy = fieldOfStudy,
                            institution = institution,
                            location = location,
                            startDate = startDate,
                            endDate = if (currentlyStudying) "" else endDate,
                            currentlyStudying = currentlyStudying,
                            description = description
                        )
                    )
                },
                modifier = Modifier.testTag("save_education_button")
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
fun SkillsEditorScreen(
    viewModel: ResumeEditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val skills = uiState.resume?.skills ?: emptyList()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.skills_dynamic_title),
                onBackClick = onNavigateBack,
                isSaving = uiState.isSaving,
                hasSaved = uiState.hasSaved
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            DynamicSkillsSectionComponent(
                skills = skills,
                onUpdateSkills = { viewModel.updateSkills(it) }
            )
        }
    }
}

@Composable
fun LanguagesEditorScreen(
    viewModel: ResumeEditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val languages = uiState.resume?.languages ?: emptyList()

    var newLangName by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf("Fluent") }
    val levels = listOf("Native", "Fluent", "Intermediate", "Basic")

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.editor_section_languages),
                onBackClick = onNavigateBack,
                isSaving = uiState.isSaving,
                hasSaved = uiState.hasSaved
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AppTextField(
                    value = newLangName,
                    onValueChange = { newLangName = it },
                    label = stringResource(R.string.field_language_name),
                    placeholder = "e.g. Arabic, English",
                    modifier = Modifier.weight(1f),
                    testTag = "input_new_language_name"
                )
                Button(
                    onClick = {
                        if (newLangName.isNotBlank()) {
                            val newLang = LanguageSkill(
                                id = UUID.randomUUID().toString(),
                                name = newLangName.trim(),
                                level = selectedLevel
                            )
                            viewModel.updateLanguages(languages + newLang)
                            newLangName = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("button_add_language")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                levels.forEach { level ->
                    FilterChip(
                        selected = selectedLevel == level,
                        onClick = { selectedLevel = level },
                        label = { Text(level, fontSize = 12.sp) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(languages, key = { it.id }) { lang ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Text(
                                    text = lang.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = lang.level,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = {
                                viewModel.updateLanguages(languages.filter { it.id != lang.id })
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectsEditorScreen(
    viewModel: ResumeEditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val projects = uiState.resume?.projects ?: emptyList()

    var editingProject by remember { mutableStateOf<Project?>(null) }
    var isNewProject by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.editor_section_projects),
                onBackClick = onNavigateBack,
                isSaving = uiState.isSaving,
                hasSaved = uiState.hasSaved
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingProject = Project(id = UUID.randomUUID().toString())
                    isNewProject = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_project")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Project")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (projects.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text(
                    text = "No projects added yet. Tap + to add.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(projects, key = { it.id }) { proj ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = proj.name.ifBlank { "Untitled Project" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (proj.role.isNotBlank()) {
                                        Text(
                                            text = proj.role,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Row {
                                    IconButton(onClick = {
                                        editingProject = proj
                                        isNewProject = false
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = {
                                        viewModel.updateProjects(projects.filter { it.id != proj.id })
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                            if (proj.technologies.isNotBlank()) {
                                Text(
                                    text = "Tech: ${proj.technologies}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingProject != null) {
        var name by remember { mutableStateOf(editingProject!!.name) }
        var role by remember { mutableStateOf(editingProject!!.role) }
        var technologies by remember { mutableStateOf(editingProject!!.technologies) }
        var url by remember { mutableStateOf(editingProject!!.url) }
        var description by remember { mutableStateOf(editingProject!!.description) }

        AlertDialog(
            onDismissRequest = { editingProject = null },
            title = { Text(stringResource(R.string.editor_section_projects), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    AppTextField(value = name, onValueChange = { name = it }, label = stringResource(R.string.field_project_name))
                    AppTextField(value = role, onValueChange = { role = it }, label = stringResource(R.string.field_project_role))
                    AppTextField(value = technologies, onValueChange = { technologies = it }, label = stringResource(R.string.field_technologies))
                    AppTextField(value = url, onValueChange = { url = it }, label = stringResource(R.string.field_project_url))
                    AppTextField(value = description, onValueChange = { description = it }, label = stringResource(R.string.field_description), singleLine = false, minLines = 3)
                    InlineTextAnalysisHelper(
                        text = description,
                        onApplySuggestion = { original, replacement ->
                            description = description.replace(original, replacement)
                        }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val updated = editingProject!!.copy(
                        name = name,
                        role = role,
                        technologies = technologies,
                        url = url,
                        description = description
                    )
                    val newList = if (isNewProject) projects + updated else projects.map { if (it.id == updated.id) updated else it }
                    viewModel.updateProjects(newList)
                    editingProject = null
                }) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { editingProject = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
fun CertificatesEditorScreen(
    viewModel: ResumeEditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val certificates = uiState.resume?.certificates ?: emptyList()

    var editingCert by remember { mutableStateOf<Certificate?>(null) }
    var isNewCert by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.editor_section_certificates),
                onBackClick = onNavigateBack,
                isSaving = uiState.isSaving,
                hasSaved = uiState.hasSaved
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingCert = Certificate(id = UUID.randomUUID().toString())
                    isNewCert = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Certificate")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (certificates.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text(
                    text = "No certificates added yet. Tap + to add.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(certificates, key = { it.id }) { cert ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cert.name.ifBlank { "Certificate" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = cert.issuer,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Row {
                                    IconButton(onClick = {
                                        editingCert = cert
                                        isNewCert = false
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = {
                                        viewModel.updateCertificates(certificates.filter { it.id != cert.id })
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingCert != null) {
        var name by remember { mutableStateOf(editingCert!!.name) }
        var issuer by remember { mutableStateOf(editingCert!!.issuer) }
        var issueDate by remember { mutableStateOf(editingCert!!.issueDate) }
        var credentialId by remember { mutableStateOf(editingCert!!.credentialId) }

        AlertDialog(
            onDismissRequest = { editingCert = null },
            title = { Text(stringResource(R.string.editor_section_certificates), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppTextField(value = name, onValueChange = { name = it }, label = stringResource(R.string.field_cert_name))
                    AppTextField(value = issuer, onValueChange = { issuer = it }, label = stringResource(R.string.field_cert_issuer))
                    AppTextField(value = issueDate, onValueChange = { issueDate = it }, label = stringResource(R.string.field_cert_date))
                    AppTextField(value = credentialId, onValueChange = { credentialId = it }, label = stringResource(R.string.field_cert_id))
                }
            },
            confirmButton = {
                Button(onClick = {
                    val updated = editingCert!!.copy(name = name, issuer = issuer, issueDate = issueDate, credentialId = credentialId)
                    val newList = if (isNewCert) certificates + updated else certificates.map { if (it.id == updated.id) updated else it }
                    viewModel.updateCertificates(newList)
                    editingCert = null
                }) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCert = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
fun ReferencesEditorScreen(
    viewModel: ResumeEditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val references = uiState.resume?.references ?: emptyList()

    var editingRef by remember { mutableStateOf<Reference?>(null) }
    var isNewRef by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.editor_section_references),
                onBackClick = onNavigateBack,
                isSaving = uiState.isSaving,
                hasSaved = uiState.hasSaved
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingRef = Reference(id = UUID.randomUUID().toString())
                    isNewRef = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Reference")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (references.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text(
                    text = "No references added yet. Tap + to add.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(references, key = { it.id }) { ref ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ref.name.ifBlank { "Reference Name" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${ref.jobTitle} at ${ref.company}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Row {
                                IconButton(onClick = {
                                    editingRef = ref
                                    isNewRef = false
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = {
                                    viewModel.updateReferences(references.filter { it.id != ref.id })
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingRef != null) {
        var name by remember { mutableStateOf(editingRef!!.name) }
        var jobTitle by remember { mutableStateOf(editingRef!!.jobTitle) }
        var company by remember { mutableStateOf(editingRef!!.company) }
        var email by remember { mutableStateOf(editingRef!!.email) }
        var phone by remember { mutableStateOf(editingRef!!.phone) }

        AlertDialog(
            onDismissRequest = { editingRef = null },
            title = { Text(stringResource(R.string.editor_section_references), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppTextField(value = name, onValueChange = { name = it }, label = stringResource(R.string.field_ref_name))
                    AppTextField(value = jobTitle, onValueChange = { jobTitle = it }, label = stringResource(R.string.field_ref_title))
                    AppTextField(value = company, onValueChange = { company = it }, label = stringResource(R.string.field_ref_company))
                    AppTextField(value = email, onValueChange = { email = it }, label = stringResource(R.string.field_email))
                    AppTextField(value = phone, onValueChange = { phone = it }, label = stringResource(R.string.field_phone))
                }
            },
            confirmButton = {
                Button(onClick = {
                    val updated = editingRef!!.copy(name = name, jobTitle = jobTitle, company = company, email = email, phone = phone)
                    val newList = if (isNewRef) references + updated else references.map { if (it.id == updated.id) updated else it }
                    viewModel.updateReferences(newList)
                    editingRef = null
                }) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { editingRef = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
fun CustomSectionsEditorScreen(
    viewModel: ResumeEditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val customSections = uiState.resume?.customSections ?: emptyList()

    var showAddDialog by remember { mutableStateOf(false) }
    var sectionTitle by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.editor_section_custom),
                onBackClick = onNavigateBack,
                isSaving = uiState.isSaving,
                hasSaved = uiState.hasSaved
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Section")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (customSections.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text(
                    text = "No custom sections. Tap + to create one (e.g. Publications, Volunteer, Hobbies).",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(customSections, key = { it.id }) { section ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = section.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = {
                                    viewModel.updateCustomSections(customSections.filter { it.id != section.id })
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Custom Section", fontWeight = FontWeight.Bold) },
            text = {
                AppTextField(
                    value = sectionTitle,
                    onValueChange = { sectionTitle = it },
                    label = "Section Title",
                    placeholder = "e.g. Volunteer Experience, Publications"
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (sectionTitle.isNotBlank()) {
                        val newSection = CustomSection(
                            id = UUID.randomUUID().toString(),
                            title = sectionTitle.trim()
                        )
                        viewModel.updateCustomSections(customSections + newSection)
                        sectionTitle = ""
                        showAddDialog = false
                    }
                }) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
