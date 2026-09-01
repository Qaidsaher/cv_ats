package com.example.feature.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.Experience
import java.util.UUID

// Action Verbs for Strong Resume Responsibilities
object ActionVerbs {
    val LEADERSHIP_VERBS = listOf(
        "Spearheaded", "Led", "Architected", "Orchestrated", "Directed", "Mentored", "Championed"
    )
    val EXECUTION_VERBS = listOf(
        "Engineered", "Implemented", "Designed", "Built", "Developed", "Deployed", "Refactored"
    )
    val IMPACT_VERBS = listOf(
        "Accelerated", "Streamlined", "Optimized", "Scaled", "Reduced", "Increased", "Automated"
    )
}

/**
 * Modern Work Experience Timeline Component
 * Features:
 * - Vertical interactive timeline track with milestone icons
 * - Date ranges with duration badge
 * - Bulleted key responsibilities with bullet markers
 * - Action verb suggestions
 * - Reordering and item management
 */
@Composable
fun WorkExperienceTimelineComponent(
    experiences: List<Experience>,
    onUpdateExperiences: (List<Experience>) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingExperience by remember { mutableStateOf<Experience?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Top Header with Add Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WorkHistory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = stringResource(R.string.timeline_role_timeline),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("btn_add_experience")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Add Role", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (experiences.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No work experience added yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Add your professional roles, date ranges, and bulleted responsibilities.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("+ Add First Role")
                    }
                }
            }
        } else {
            // Vertical Timeline Column
            Column(modifier = Modifier.fillMaxWidth()) {
                experiences.forEachIndexed { index, exp ->
                    val isFirst = index == 0
                    val isLast = index == experiences.lastIndex

                    TimelineExperienceNode(
                        experience = exp,
                        isFirst = isFirst,
                        isLast = isLast,
                        onEdit = { editingExperience = exp },
                        onDelete = {
                            onUpdateExperiences(experiences.filter { it.id != exp.id })
                        },
                        onDuplicate = {
                            val copy = exp.copy(
                                id = UUID.randomUUID().toString(),
                                jobTitle = "${exp.jobTitle} (Copy)"
                            )
                            val list = experiences.toMutableList()
                            list.add(index + 1, copy)
                            onUpdateExperiences(list)
                        },
                        onMoveUp = if (index > 0) {
                            {
                                val list = experiences.toMutableList()
                                val temp = list[index]
                                list[index] = list[index - 1]
                                list[index - 1] = temp
                                onUpdateExperiences(list)
                            }
                        } else null,
                        onMoveDown = if (index < experiences.lastIndex) {
                            {
                                val list = experiences.toMutableList()
                                val temp = list[index]
                                list[index] = list[index + 1]
                                list[index + 1] = temp
                                onUpdateExperiences(list)
                            }
                        } else null
                    )
                }
            }
        }
    }

    // Add / Edit Dialogs
    if (showAddDialog) {
        WorkExperienceEditDialog(
            initialExperience = null,
            onDismiss = { showAddDialog = false },
            onSave = { newExp ->
                onUpdateExperiences(experiences + newExp)
                showAddDialog = false
            }
        )
    }

    if (editingExperience != null) {
        WorkExperienceEditDialog(
            initialExperience = editingExperience,
            onDismiss = { editingExperience = null },
            onSave = { updated ->
                onUpdateExperiences(experiences.map { if (it.id == updated.id) updated else it })
                editingExperience = null
            }
        )
    }
}

/**
 * Individual Node on the vertical timeline track
 */
@Composable
private fun TimelineExperienceNode(
    experience: Experience,
    isFirst: Boolean,
    isLast: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?
) {
    val bullets = experience.effectiveBullets
    val isCurrent = experience.currentlyWorking

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Timeline Track Column (Line + Dot)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            // Top connecting line
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(14.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Milestone Node Icon / Dot
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCurrent) Color(0xFF059669) else MaterialTheme.colorScheme.primary
                    )
            ) {
                Icon(
                    imageVector = if (isCurrent) Icons.Default.Work else Icons.Default.Business,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }

            // Bottom connecting line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Role Content Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isCurrent) Color(0xFF059669).copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
            ),
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp)
                .clickable { onEdit() }
                .testTag("timeline_node_${experience.jobTitle.replace(" ", "_")}")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header (Job Title + Actions)
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = experience.jobTitle.ifBlank { "Untitled Position" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = experience.company.ifBlank { "Company" },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (experience.location.isNotBlank()) {
                                Text(
                                    text = "• ${experience.location}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Action Icons
                    Row {
                        onMoveUp?.let {
                            IconButton(onClick = it, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", modifier = Modifier.size(16.dp))
                            }
                        }
                        onMoveDown?.let {
                            IconButton(onClick = it, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", modifier = Modifier.size(16.dp))
                            }
                        }
                        IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Date Range & Duration Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isCurrent) Color(0xFF059669).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = experience.formattedDateRange,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCurrent) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (isCurrent) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF059669).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Current Role",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF059669),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Bulleted Responsibilities Section
                if (bullets.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        bullets.forEach { bulletText ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "•",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 1.dp)
                                )
                                Text(
                                    text = bulletText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                } else if (experience.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = experience.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * Full Edit Dialog for Work Experience with Date Range & Bulleted Responsibilities Builder
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkExperienceEditDialog(
    initialExperience: Experience?,
    onDismiss: () -> Unit,
    onSave: (Experience) -> Unit
) {
    var jobTitle by remember { mutableStateOf(initialExperience?.jobTitle ?: "") }
    var company by remember { mutableStateOf(initialExperience?.company ?: "") }
    var location by remember { mutableStateOf(initialExperience?.location ?: "") }
    var startDate by remember { mutableStateOf(initialExperience?.startDate ?: "") }
    var endDate by remember { mutableStateOf(initialExperience?.endDate ?: "") }
    var currentlyWorking by remember { mutableStateOf(initialExperience?.currentlyWorking ?: false) }

    // Bullet points list
    val bulletsList = remember {
        mutableStateListOf<String>().apply {
            if (initialExperience != null && initialExperience.bullets.isNotEmpty()) {
                addAll(initialExperience.bullets)
            } else if (initialExperience != null && initialExperience.description.isNotBlank()) {
                addAll(initialExperience.effectiveBullets)
            }
        }
    }

    var newBulletText by remember { mutableStateOf("") }
    var selectedVerbGroup by remember { mutableStateOf("Action") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialExperience == null) "Add Work Experience" else "Edit Work Experience",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Job Title
                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    label = { Text("Job Title") },
                    placeholder = { Text("e.g. Senior Android Engineer") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_exp_title")
                )

                // Company & Location
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company / Organization") },
                    placeholder = { Text("e.g. Google, Spotify") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_exp_company")
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location (Optional)") },
                    placeholder = { Text("e.g. San Francisco, CA or Remote") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Date Range Section
                Text(
                    text = stringResource(R.string.timeline_date_range),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date") },
                        placeholder = { Text("e.g. Mar 2021") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = if (currentlyWorking) "Present" else endDate,
                        onValueChange = { if (!currentlyWorking) endDate = it },
                        enabled = !currentlyWorking,
                        label = { Text("End Date") },
                        placeholder = { Text("e.g. Present") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { currentlyWorking = !currentlyWorking }
                ) {
                    Checkbox(
                        checked = currentlyWorking,
                        onCheckedChange = { currentlyWorking = it }
                    )
                    Text(
                        text = "I currently work here",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Bulleted Key Responsibilities Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.timeline_bullets_title),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${bulletsList.size} bullets",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action Verb Starters Cloud
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Action Verb Starters (Tap to start bullet):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            (ActionVerbs.LEADERSHIP_VERBS.take(3) + ActionVerbs.EXECUTION_VERBS.take(3) + ActionVerbs.IMPACT_VERBS.take(3)).forEach { verb ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                    modifier = Modifier.clickable {
                                        newBulletText = if (newBulletText.isBlank()) "$verb " else "$verb $newBulletText"
                                    }
                                ) {
                                    Text(
                                        text = "+ $verb",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // New Bullet Input Field
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newBulletText,
                        onValueChange = { newBulletText = it },
                        label = { Text("New Bullet Responsibility") },
                        placeholder = { Text(stringResource(R.string.timeline_bullet_hint), fontSize = 11.sp) },
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_new_bullet")
                    )

                    Button(
                        onClick = {
                            if (newBulletText.isNotBlank()) {
                                bulletsList.add(newBulletText.trim())
                                newBulletText = ""
                            }
                        },
                        enabled = newBulletText.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        modifier = Modifier.testTag("btn_add_bullet")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
                    }
                }

                // Existing Bullets List with Edit and Delete
                if (bulletsList.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        bulletsList.forEachIndexed { index, bullet ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                                ) {
                                    Text(
                                        text = "${index + 1}.",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = bullet,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f),
                                        lineHeight = 14.sp
                                    )
                                    IconButton(
                                        onClick = { bulletsList.removeAt(index) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (jobTitle.isNotBlank()) {
                        // Ensure if the user typed something in newBulletText but forgot to hit plus, we include it
                        if (newBulletText.isNotBlank()) {
                            bulletsList.add(newBulletText.trim())
                        }
                        val formattedBullets = bulletsList.toList()
                        val combinedDescription = formattedBullets.joinToString("\n• ") { it }.let {
                            if (it.isNotBlank() && !it.startsWith("• ")) "• $it" else it
                        }

                        val experience = Experience(
                            id = initialExperience?.id ?: UUID.randomUUID().toString(),
                            company = company.trim(),
                            jobTitle = jobTitle.trim(),
                            location = location.trim(),
                            startDate = startDate.trim(),
                            endDate = if (currentlyWorking) "" else endDate.trim(),
                            currentlyWorking = currentlyWorking,
                            description = combinedDescription,
                            bullets = formattedBullets,
                            sortOrder = initialExperience?.sortOrder ?: 0
                        )
                        onSave(experience)
                    }
                },
                enabled = jobTitle.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_save_experience")
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
