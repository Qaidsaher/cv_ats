package com.example.feature.home

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.designsystem.components.EmptyStateView
import com.example.domain.model.Resume
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToEditor: (String) -> Unit,
    onNavigateToPreview: (String) -> Unit,
    onNavigateToExport: (String) -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCreateDialog by remember { mutableStateOf(false) }
    var resumeToDelete by remember { mutableStateOf<Resume?>(null) }
    var resumeToRename by remember { mutableStateOf<Resume?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_create_resume")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.home_create_resume),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = innerPadding.calculateBottomPadding() + 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Section
            item {
                HomeHeaderBanner(
                    isPro = uiState.isProUser,
                    onCreateClick = { showCreateDialog = true },
                    onPremiumClick = onNavigateToPremium
                )
            }

            // Search Bar
            if (uiState.resumes.isNotEmpty()) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text(stringResource(R.string.home_search_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_input")
                    )
                }
            }

            // Section Title: My Resumes
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.home_my_resumes),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (uiState.resumes.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${uiState.filteredResumes.size}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Empty State
            if (uiState.resumes.isEmpty() && !uiState.isLoading) {
                item {
                    EmptyStateView(
                        title = stringResource(R.string.home_no_resumes_title),
                        description = stringResource(R.string.home_no_resumes_desc),
                        icon = Icons.Default.Description,
                        actionButtonText = stringResource(R.string.home_create_resume),
                        onActionClick = { showCreateDialog = true }
                    )
                }
            } else {
                // Resume Cards List
                items(uiState.filteredResumes, key = { it.id }) { resume ->
                    ResumeCard(
                        resume = resume,
                        onEdit = { onNavigateToEditor(resume.id) },
                        onPreview = { onNavigateToPreview(resume.id) },
                        onExport = { onNavigateToExport(resume.id) },
                        onDuplicate = {
                            viewModel.duplicateResume(resume.id) { newId ->
                                scope.launch {
                                    snackbarHostState.showSnackbar("Resume duplicated")
                                }
                            }
                        },
                        onRename = { resumeToRename = resume },
                        onDelete = { resumeToDelete = resume }
                    )
                }
            }

            // Popular Templates Carousel
            if (uiState.templates.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.home_explore_templates),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onNavigateToTemplates) {
                            Text(stringResource(R.string.tab_all))
                        }
                    }
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(uiState.templates.take(5)) { template ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier
                                    .width(140.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(onClick = onNavigateToTemplates)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .background(
                                                color = Color(android.graphics.Color.parseColor(template.colors.sidebar)),
                                                shape = RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = Color(android.graphics.Color.parseColor(template.colors.primary)),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = template.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (template.isPremium) "PRO" else "FREE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (template.isPremium) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Create Resume
    if (showCreateDialog) {
        CreateResumeDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, language, useSample ->
                showCreateDialog = false
                viewModel.createResume(title, language, useSample) { newId ->
                    onNavigateToEditor(newId)
                }
            }
        )
    }

    // Dialog: Delete Confirmation
    if (resumeToDelete != null) {
        val toDelete = resumeToDelete!!
        AlertDialog(
            onDismissRequest = { resumeToDelete = null },
            title = { Text(stringResource(R.string.dialog_delete_title)) },
            text = { Text(stringResource(R.string.dialog_delete_msg, toDelete.title)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteResume(toDelete)
                        resumeToDelete = null
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Resume deleted",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.undoDelete()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_resume_button")
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { resumeToDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    // Dialog: Rename
    if (resumeToRename != null) {
        val toRename = resumeToRename!!
        var newTitle by remember { mutableStateOf(toRename.title) }
        AlertDialog(
            onDismissRequest = { resumeToRename = null },
            title = { Text(stringResource(R.string.dialog_rename_title)) },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.dialog_resume_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            viewModel.renameResume(toRename.id, newTitle.trim())
                        }
                        resumeToRename = null
                    }
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { resumeToRename = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun HomeHeaderBanner(
    isPro: Boolean,
    onCreateClick: () -> Unit,
    onPremiumClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary // #2D2A26 Charcoal
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Editorial Tracking Tag
                Text(
                    text = "RESUME CHRONICLE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFC9C2B5),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                if (!isPro) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF423E37),
                        modifier = Modifier.clickable(onClick = onPremiumClick)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Color(0xFFE7E2D9),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PRO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE7E2D9)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.home_welcome_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.home_welcome_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFA69F92),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onCreateClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE7E2D9),
                    contentColor = Color(0xFF1C1B17)
                ),
                modifier = Modifier.testTag("banner_create_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.home_create_resume),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ResumeCard(
    resume: Resume,
    onEdit: () -> Unit,
    onPreview: () -> Unit,
    onExport: () -> Unit,
    onDuplicate: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val formattedDate = remember(resume.updatedAt) {
        val sdf = SimpleDateFormat("MMM d, yyyy • hh:mm a", Locale.getDefault())
        sdf.format(Date(resume.updatedAt))
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onEdit)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                RoundedCornerShape(24.dp)
            )
            .testTag("resume_card_${resume.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Resume icon in soft circular container
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = resume.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = resume.personalInfo.fullName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Language badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = if (resume.isRtl) "AR" else "EN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options"
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_edit)) },
                            onClick = { showMenu = false; onEdit() },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_preview)) },
                            onClick = { showMenu = false; onPreview() },
                            leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_duplicate)) },
                            onClick = { showMenu = false; onDuplicate() },
                            leadingIcon = { Icon(Icons.Default.FolderCopy, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_rename)) },
                            onClick = { showMenu = false; onRename() },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) },
                            onClick = { showMenu = false; onDelete() },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer row with date and quick buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.home_last_edited, formattedDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onPreview,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.action_preview), fontSize = 12.sp)
                    }

                    Button(
                        onClick = onExport,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.action_export), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateResumeDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, language: String, useSample: Boolean) -> Unit
) {
    var resumeName by remember { mutableStateOf("") }
    var selectedLang by remember { mutableStateOf("en") }
    var useSampleData by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_create_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = resumeName,
                    onValueChange = { resumeName = it },
                    label = { Text(stringResource(R.string.dialog_resume_name)) },
                    placeholder = { Text(stringResource(R.string.dialog_resume_name_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_resume_name_input")
                )

                Text(
                    text = stringResource(R.string.dialog_resume_language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedLang = "en" }
                ) {
                    RadioButton(
                        selected = selectedLang == "en",
                        onClick = { selectedLang = "en" }
                    )
                    Text(
                        text = stringResource(R.string.lang_english),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedLang = "ar" }
                ) {
                    RadioButton(
                        selected = selectedLang == "ar",
                        onClick = { selectedLang = "ar" }
                    )
                    Text(
                        text = stringResource(R.string.lang_arabic),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Text(
                    text = stringResource(R.string.dialog_start_option),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { useSampleData = true }
                ) {
                    RadioButton(
                        selected = useSampleData,
                        onClick = { useSampleData = true }
                    )
                    Text(
                        text = stringResource(R.string.dialog_start_sample),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { useSampleData = false }
                ) {
                    RadioButton(
                        selected = !useSampleData,
                        onClick = { useSampleData = false }
                    )
                    Text(
                        text = stringResource(R.string.dialog_start_scratch),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(resumeName, selectedLang, useSampleData) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("dialog_create_confirm_button")
            ) {
                Text(stringResource(R.string.home_create_resume))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
