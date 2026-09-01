package com.example.feature.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiPeople
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.domain.model.Skill
import java.util.UUID

// Standard Skill Categories
object SkillCategories {
    const val TECHNICAL = "Technical Skills"
    const val SOFT = "Soft Skills"
    const val TOOLS = "Tools & Frameworks"
    const val LEADERSHIP = "Leadership & Management"
    const val LANGUAGES = "Languages"
    const val OTHER = "Other / Custom"

    val ALL_DEFAULT = listOf(TECHNICAL, SOFT, TOOLS, LEADERSHIP, OTHER)

    fun getCategoryColor(category: String): Color {
        return when (category.lowercase()) {
            "technical skills", "technical" -> Color(0xFF2563EB) // Blue
            "soft skills", "soft" -> Color(0xFF059669)          // Green / Emerald
            "tools & frameworks", "tools" -> Color(0xFFD97706)  // Amber
            "leadership & management", "leadership" -> Color(0xFF7C3AED) // Purple
            "languages" -> Color(0xFF0D9488)                   // Teal
            else -> Color(0xFF4B5563)                          // Slate
        }
    }

    fun getCategoryIcon(category: String): ImageVector {
        return when (category.lowercase()) {
            "technical skills", "technical" -> Icons.Default.Code
            "soft skills", "soft" -> Icons.Default.Psychology
            "tools & frameworks", "tools" -> Icons.Default.Tune
            "leadership & management", "leadership" -> Icons.Default.Groups
            "languages" -> Icons.Default.Category
            else -> Icons.Default.Folder
        }
    }
}

// Recommended Quick-Add Presets
object SkillPresets {
    val TECHNICAL_PRESETS = listOf(
        "Kotlin", "Jetpack Compose", "Android SDK", "Coroutines & Flow",
        "Room Database", "REST & GraphQL APIs", "Git & GitHub", "Clean Architecture",
        "Unit & UI Testing", "CI/CD & DevOps", "System Design", "SQL / PostgreSQL",
        "Python", "Java", "Docker", "Firebase", "TypeScript", "React"
    )

    val SOFT_PRESETS = listOf(
        "Team Leadership", "Cross-Functional Collaboration", "Problem Solving",
        "Effective Communication", "Agile & Scrum Methodologies", "Time Management",
        "Critical Thinking", "Technical Mentorship", "Stakeholder Management",
        "Adaptability & Resilience", "Conflict Resolution", "Strategic Planning"
    )

    val TOOLS_PRESETS = listOf(
        "Android Studio", "GitLab CI", "Jira & Confluence", "Postman",
        "Figma", "Firebase Console", "GitHub Actions", "Docker Compose",
        "Gradle & KSP", "Fastlane", "Grafana", "Google Cloud Platform"
    )
}

/**
 * Modern Dynamic Skills Section UI Component
 * Supports adding, editing, removing, categorization (Technical, Soft, Tools, etc.),
 * proficiency metering, quick-add preset suggestions, search, and grouped category view.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DynamicSkillsSectionComponent(
    skills: List<Skill>,
    onUpdateSkills: (List<Skill>) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String>("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSkill by remember { mutableStateOf<Skill?>(null) }
    var isGroupedView by remember { mutableStateOf(true) }

    // Unique categories currently in the resume
    val dynamicCategories = remember(skills) {
        val existing = skills.map { it.category.ifBlank { SkillCategories.TECHNICAL } }.distinct()
        (listOf("All") + SkillCategories.ALL_DEFAULT + existing).distinct()
    }

    // Filtered list based on search and category
    val filteredSkills = remember(skills, searchQuery, selectedCategoryFilter) {
        skills.filter { skill ->
            val matchesCategory = selectedCategoryFilter == "All" ||
                    skill.category.equals(selectedCategoryFilter, ignoreCase = true) ||
                    (selectedCategoryFilter == SkillCategories.TECHNICAL && skill.category.isBlank())
            val matchesSearch = searchQuery.isBlank() ||
                    skill.name.contains(searchQuery, ignoreCase = true) ||
                    skill.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    // Grouped by category map
    val groupedSkills = remember(filteredSkills) {
        filteredSkills.groupBy { it.category.ifBlank { SkillCategories.TECHNICAL } }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Search and View Toggle Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.skills_search_placeholder), fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_search_skills")
            )

            // Add Skill Button
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                modifier = Modifier.testTag("btn_add_skill_dialog")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Add", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            dynamicCategories.forEach { category ->
                val isSelected = selectedCategoryFilter.equals(category, ignoreCase = true)
                val count = if (category == "All") skills.size else skills.count { it.category.equals(category, ignoreCase = true) }
                val catColor = if (category == "All") MaterialTheme.colorScheme.primary else SkillCategories.getCategoryColor(category)

                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategoryFilter = category },
                    label = {
                        Text(
                            text = if (category == "All") "All ($count)" else "$category ($count)",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        if (category != "All") {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(catColor)
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = catColor.copy(alpha = 0.15f),
                        selectedLabelColor = catColor
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("filter_chip_skill_$category")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Preset Suggestions Banner
        QuickSkillPresetsSection(
            existingSkills = skills,
            onAddSkill = { presetName, presetCategory ->
                val newSkill = Skill(
                    id = UUID.randomUUID().toString(),
                    name = presetName,
                    category = presetCategory,
                    level = "Advanced"
                )
                onUpdateSkills(skills + newSkill)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Summary Counts Card
        SkillsSummaryBanner(
            totalSkills = skills.size,
            technicalCount = skills.count { it.category.equals(SkillCategories.TECHNICAL, ignoreCase = true) || it.category.isBlank() },
            softCount = skills.count { it.category.equals(SkillCategories.SOFT, ignoreCase = true) },
            toolsCount = skills.count { it.category.equals(SkillCategories.TOOLS, ignoreCase = true) },
            otherCount = skills.count {
                !it.category.equals(SkillCategories.TECHNICAL, ignoreCase = true) &&
                !it.category.equals(SkillCategories.SOFT, ignoreCase = true) &&
                !it.category.equals(SkillCategories.TOOLS, ignoreCase = true) &&
                it.category.isNotBlank()
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Skills List / Grouped View
        if (filteredSkills.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No skills found matching \"$searchQuery\"" else stringResource(R.string.skills_empty_category),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            groupedSkills.forEach { (category, skillsInCategory) ->
                val catColor = SkillCategories.getCategoryColor(category)
                val catIcon = SkillCategories.getCategoryIcon(category)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Category Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(catColor.copy(alpha = 0.12f))
                                ) {
                                    Icon(
                                        imageVector = catIcon,
                                        contentDescription = null,
                                        tint = catColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = catColor
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = catColor.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "${skillsInCategory.size} skill${if (skillsInCategory.size > 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = catColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Skills chips in this category
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            skillsInCategory.forEach { skill ->
                                SkillItemChip(
                                    skill = skill,
                                    categoryColor = catColor,
                                    onEdit = { editingSkill = skill },
                                    onDelete = {
                                        onUpdateSkills(skills.filter { it.id != skill.id })
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddDialog) {
        SkillEditDialog(
            initialSkill = null,
            defaultCategory = if (selectedCategoryFilter != "All") selectedCategoryFilter else SkillCategories.TECHNICAL,
            onDismiss = { showAddDialog = false },
            onSave = { newSkill ->
                onUpdateSkills(skills + newSkill)
                showAddDialog = false
            }
        )
    }

    if (editingSkill != null) {
        SkillEditDialog(
            initialSkill = editingSkill,
            defaultCategory = editingSkill!!.category,
            onDismiss = { editingSkill = null },
            onSave = { updated ->
                onUpdateSkills(skills.map { if (it.id == updated.id) updated else it })
                editingSkill = null
            }
        )
    }
}

/**
 * Individual skill pill chip with proficiency bar and quick action buttons
 */
@Composable
fun SkillItemChip(
    skill: Skill,
    categoryColor: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val levelFraction = when (skill.level.lowercase()) {
        "beginner" -> 0.33f
        "intermediate" -> 0.66f
        "advanced" -> 0.85f
        "expert", "master" -> 1.0f
        else -> 0.75f
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = androidx.compose.foundation.BorderStroke(1.dp, categoryColor.copy(alpha = 0.25f)),
        modifier = modifier
            .clickable { onEdit() }
            .testTag("chip_skill_${skill.name.replace(" ", "_")}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 6.dp, bottom = 6.dp)
        ) {
            Column {
                Text(
                    text = skill.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = skill.level,
                        style = MaterialTheme.typography.labelSmall,
                        color = categoryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // Visual mini meter
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(categoryColor.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(levelFraction)
                                .fillMaxSize()
                                .background(categoryColor)
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * Summary badge banner displaying skill breakdown counts
 */
@Composable
private fun SkillsSummaryBanner(
    totalSkills: Int,
    technicalCount: Int,
    softCount: Int,
    toolsCount: Int,
    otherCount: Int
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
        ) {
            SkillCountStat(label = "Total", count = totalSkills, color = MaterialTheme.colorScheme.primary)
            SkillCountStat(label = "Technical", count = technicalCount, color = Color(0xFF2563EB))
            SkillCountStat(label = "Soft Skills", count = softCount, color = Color(0xFF059669))
            SkillCountStat(label = "Tools", count = toolsCount, color = Color(0xFFD97706))
        }
    }
}

@Composable
private fun SkillCountStat(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}

/**
 * Quick Add Preset Suggestions horizontal chip cloud
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickSkillPresetsSection(
    existingSkills: List<Skill>,
    onAddSkill: (name: String, category: String) -> Unit
) {
    val existingNames = remember(existingSkills) { existingSkills.map { it.name.lowercase().trim() }.toSet() }
    var selectedPresetTab by remember { mutableStateOf("Tech") }

    val currentPresets = when (selectedPresetTab) {
        "Tech" -> SkillPresets.TECHNICAL_PRESETS.map { it to SkillCategories.TECHNICAL }
        "Soft" -> SkillPresets.SOFT_PRESETS.map { it to SkillCategories.SOFT }
        else -> SkillPresets.TOOLS_PRESETS.map { it to SkillCategories.TOOLS }
    }.filter { (name, _) -> !existingNames.contains(name.lowercase().trim()) }

    if (currentPresets.isNotEmpty()) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(R.string.skills_add_quick_preset),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Preset Category Tabs
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Tech", "Soft", "Tools").forEach { tab ->
                            val isSelected = selectedPresetTab == tab
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier.clickable { selectedPresetTab = tab }
                            ) {
                                Text(
                                    text = tab,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    currentPresets.take(10).forEach { (presetName, category) ->
                        val catColor = SkillCategories.getCategoryColor(category)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, catColor.copy(alpha = 0.3f)),
                            modifier = Modifier.clickable { onAddSkill(presetName, category) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = catColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = presetName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog for adding or editing a skill with Category selector & Proficiency levels
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillEditDialog(
    initialSkill: Skill?,
    defaultCategory: String,
    onDismiss: () -> Unit,
    onSave: (Skill) -> Unit
) {
    var skillName by remember { mutableStateOf(initialSkill?.name ?: "") }
    var selectedCategory by remember {
        mutableStateOf(initialSkill?.category?.ifBlank { defaultCategory } ?: defaultCategory)
    }
    var customCategoryText by remember { mutableStateOf("") }
    var isCustomCategory by remember {
        mutableStateOf(
            !SkillCategories.ALL_DEFAULT.contains(selectedCategory) && selectedCategory.isNotBlank()
        )
    }
    var selectedLevel by remember { mutableStateOf(initialSkill?.level ?: "Advanced") }
    val levels = listOf("Beginner", "Intermediate", "Advanced", "Expert")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialSkill == null) stringResource(R.string.skills_add_dialog_title) else stringResource(R.string.skills_edit_dialog_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Skill Name Input
                OutlinedTextField(
                    value = skillName,
                    onValueChange = { skillName = it },
                    label = { Text(stringResource(R.string.field_skill_name)) },
                    placeholder = { Text("e.g. Jetpack Compose, Leadership") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_dialog_skill_name")
                )

                // Category Selection
                Text(
                    text = stringResource(R.string.skills_category_label),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                // Standard Categories Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    SkillCategories.ALL_DEFAULT.forEach { category ->
                        val isSelected = !isCustomCategory && selectedCategory.equals(category, ignoreCase = true)
                        val catColor = SkillCategories.getCategoryColor(category)

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                isCustomCategory = false
                                selectedCategory = category
                            },
                            label = { Text(category, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = catColor.copy(alpha = 0.2f),
                                selectedLabelColor = catColor
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    FilterChip(
                        selected = isCustomCategory,
                        onClick = { isCustomCategory = true },
                        label = { Text("+ Custom", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                if (isCustomCategory) {
                    OutlinedTextField(
                        value = customCategoryText,
                        onValueChange = { customCategoryText = it },
                        label = { Text("Custom Category Name") },
                        placeholder = { Text("e.g. Cloud Architecture") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Proficiency Level
                Text(
                    text = stringResource(R.string.field_skill_level),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    levels.forEach { level ->
                        FilterChip(
                            selected = selectedLevel == level,
                            onClick = { selectedLevel = level },
                            label = { Text(level, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (skillName.isNotBlank()) {
                        val finalCategory = if (isCustomCategory && customCategoryText.isNotBlank()) {
                            customCategoryText.trim()
                        } else {
                            selectedCategory
                        }
                        val skill = Skill(
                            id = initialSkill?.id ?: UUID.randomUUID().toString(),
                            name = skillName.trim(),
                            category = finalCategory,
                            level = selectedLevel,
                            sortOrder = initialSkill?.sortOrder ?: 0
                        )
                        onSave(skill)
                    }
                },
                enabled = skillName.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_save_dialog_skill")
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
