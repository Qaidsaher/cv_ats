package com.example.feature.templates

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.TemplateSpec

/**
 * Compact horizontal template selector bar displayed on preview / editor bottom sheets
 */
@Composable
fun AtsTemplateSelectorBar(
    templates: List<TemplateSpec>,
    selectedTemplateId: String,
    onSelectTemplate: (String) -> Unit,
    onOpenFullSelector: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.ats_template_selector_title),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.clickable { onOpenFullSelector() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "View All Layouts",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(templates, key = { it.id }) { template ->
                val isSelected = template.id == selectedTemplateId
                AtsTemplateCompactCard(
                    template = template,
                    isSelected = isSelected,
                    onClick = { onSelectTemplate(template.id) },
                    modifier = Modifier.testTag("ats_template_chip_${template.id}")
                )
            }
        }
    }
}

/**
 * Compact template item with a miniature wireframe layout and ATS indicator
 */
@Composable
fun AtsTemplateCompactCard(
    template: TemplateSpec,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
        label = "border_color"
    )
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = modifier
            .width(130.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Miniature layout wireframe
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF9FAFB))
                    .border(0.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                    .padding(5.dp)
            ) {
                AtsLayoutWireframe(template = template)

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(18.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = template.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (template.isAtsFriendly) {
                    Text(
                        text = "ATS 100%",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D)
                    )
                } else {
                    Text(
                        text = "Visual Pro",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (template.isPremium) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "Pro",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

/**
 * Full-featured ATS Template & Layout Styles Bottom Sheet Selector
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AtsTemplateSelectorModalSheet(
    templates: List<TemplateSpec>,
    selectedTemplateId: String,
    onSelectTemplate: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategoryFilter by remember { mutableStateOf("all") }

    val categories = listOf(
        "all" to stringResource(R.string.ats_filter_all),
        "single_column" to stringResource(R.string.ats_filter_classic),
        "modern" to stringResource(R.string.ats_filter_modern),
        "minimal" to stringResource(R.string.ats_filter_minimal),
        "executive" to stringResource(R.string.ats_filter_executive),
        "arabic" to stringResource(R.string.ats_filter_arabic)
    )

    val filteredTemplates = remember(templates, selectedCategoryFilter) {
        when (selectedCategoryFilter) {
            "all" -> templates
            "single_column" -> templates.filter { it.layout.type == "single_column" }
            "modern" -> templates.filter { it.category == "modern" || it.id.contains("modern") || it.layout.sectionHeaderStyle.contains("pill") }
            "minimal" -> templates.filter { it.category == "minimal" || it.id.contains("minimal") }
            "executive" -> templates.filter { it.category == "professional" || it.id.contains("executive") }
            "arabic" -> templates.filter { it.supportsRtl || it.category == "arabic" || it.id.contains("arabic") }
            else -> templates
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = stringResource(R.string.ats_template_selector_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.ats_template_selector_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ATS Information Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.ats_why_title),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.ats_why_desc),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { (key, label) ->
                    val isSelected = selectedCategoryFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = key },
                        label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // List of ATS Layouts
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                items(filteredTemplates, key = { it.id }) { template ->
                    val isSelected = template.id == selectedTemplateId
                    AtsTemplateDetailedCard(
                        template = template,
                        isSelected = isSelected,
                        onSelect = {
                            onSelectTemplate(template.id)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("ats_template_card_${template.id}")
                    )
                }
            }
        }
    }
}

/**
 * Detailed card for ATS template selection in full modal
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AtsTemplateDetailedCard(
    template: TemplateSpec,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
        label = "border_color"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Miniature Schematic Preview
            Box(
                modifier = Modifier
                    .size(width = 68.dp, height = 90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF9FAFB))
                    .border(0.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                    .padding(5.dp)
            ) {
                AtsLayoutWireframe(template = template)
            }

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (template.isPremium) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "PRO",
                                color = Color(0xFFB45309),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Feature tags
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (template.isAtsFriendly) {
                        AtsPillTag(text = "100% ATS", color = Color(0xFF166534), bgColor = Color(0xFFDCFCE7))
                    }
                    val layoutLabel = when (template.layout.type) {
                        "single_column" -> "Single Column"
                        "sidebar_start", "sidebar_end" -> "Two Column"
                        "header_split" -> "Executive Header"
                        else -> "Single Column"
                    }
                    AtsPillTag(text = layoutLabel, color = MaterialTheme.colorScheme.primary, bgColor = MaterialTheme.colorScheme.primaryContainer)

                    if (template.supportsRtl) {
                        AtsPillTag(text = "RTL Arabic", color = Color(0xFF0F766E), bgColor = Color(0xFFCCFBF1))
                    }
                }
            }

            // Radio/Selection Icon
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        CircleShape
                    )
                    .border(
                        1.5.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AtsPillTag(text: String, color: Color, bgColor: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
        )
    }
}

/**
 * Procedural schematic wireframe that visualizes the layout structure
 */
@Composable
fun AtsLayoutWireframe(
    template: TemplateSpec,
    modifier: Modifier = Modifier
) {
    val isTwoColumn = template.layout.type == "sidebar_start" || template.layout.type == "sidebar_end"
    val isSidebarStart = template.layout.type == "sidebar_start"
    val isHeaderSplit = template.layout.type == "header_split"
    val isCentered = template.layout.headerStyle == "centered_clean"

    val accentColor = remember(template.colors.accent) {
        parseHexToColor(template.colors.accent, Color(0xFF0D5C75))
    }
    val primaryColor = remember(template.colors.primary) {
        parseHexToColor(template.colors.primary, Color(0xFF1E293B))
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(2.5.dp)
    ) {
        // Header Wireframe
        if (isHeaderSplit) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(primaryColor, RoundedCornerShape(2.dp))
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(2.dp)
                        .background(Color.White)
                )
            }
        } else {
            Column(
                horizontalAlignment = if (isCentered) Alignment.CenterHorizontally else Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Name bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (isCentered) 0.7f else 0.5f)
                        .height(3.dp)
                        .background(primaryColor, RoundedCornerShape(1.dp))
                )
                Spacer(modifier = Modifier.height(1.dp))
                // Title & Contact bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (isCentered) 0.5f else 0.35f)
                        .height(1.5.dp)
                        .background(accentColor, RoundedCornerShape(1.dp))
                )
            }
        }

        // Header Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.8.dp)
                .background(primaryColor.copy(alpha = 0.5f))
        )

        // Body Structure
        if (isTwoColumn) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                if (isSidebarStart) {
                    SidebarWireframeColumn(accentColor = accentColor, modifier = Modifier.weight(0.35f))
                    MainContentWireframeColumn(primaryColor = primaryColor, modifier = Modifier.weight(0.65f))
                } else {
                    MainContentWireframeColumn(primaryColor = primaryColor, modifier = Modifier.weight(0.65f))
                    SidebarWireframeColumn(accentColor = accentColor, modifier = Modifier.weight(0.35f))
                }
            }
        } else {
            // Pure Single Column ATS Wireframe
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.5.dp)
            ) {
                // Section 1: Summary / Experience
                SectionWireframe(titleWidth = 0.4f, barColor = primaryColor, accentColor = accentColor, itemsCount = 2)
                // Section 2: Education / Skills
                SectionWireframe(titleWidth = 0.35f, barColor = primaryColor, accentColor = accentColor, itemsCount = 2)
            }
        }
    }
}

@Composable
private fun SectionWireframe(
    titleWidth: Float,
    barColor: Color,
    accentColor: Color,
    itemsCount: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(1.5.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(titleWidth)
                    .height(2.2.dp)
                    .background(barColor, RoundedCornerShape(1.dp))
            )
            Spacer(modifier = Modifier.width(2.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(0.5.dp)
                    .background(barColor.copy(alpha = 0.2f))
            )
        }

        repeat(itemsCount) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(1.5.dp)
                        .background(accentColor, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(1.2.dp)
                        .background(Color(0xFF94A3B8), RoundedCornerShape(0.5.dp))
                )
            }
        }
    }
}

@Composable
private fun SidebarWireframeColumn(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9), RoundedCornerShape(2.dp))
            .padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth(0.6f).height(2.dp).background(accentColor))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFCBD5E1)))
        Box(modifier = Modifier.fillMaxWidth(0.8f).height(1.dp).background(Color(0xFFCBD5E1)))
        Spacer(modifier = Modifier.height(1.dp))
        Box(modifier = Modifier.fillMaxWidth(0.6f).height(2.dp).background(accentColor))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFCBD5E1)))
    }
}

@Composable
private fun MainContentWireframeColumn(
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth(0.5f).height(2.2.dp).background(primaryColor))
        Box(modifier = Modifier.fillMaxWidth().height(1.2.dp).background(Color(0xFF94A3B8)))
        Box(modifier = Modifier.fillMaxWidth(0.9f).height(1.2.dp).background(Color(0xFF94A3B8)))
        Spacer(modifier = Modifier.height(2.dp))
        Box(modifier = Modifier.fillMaxWidth(0.4f).height(2.2.dp).background(primaryColor))
        Box(modifier = Modifier.fillMaxWidth().height(1.2.dp).background(Color(0xFF94A3B8)))
    }
}

/**
 * Active ATS layout banner used in editor / header
 */
@Composable
fun AtsActiveLayoutBanner(
    template: TemplateSpec?,
    onSwitchLayout: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSwitchLayout)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.ats_current_layout),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (template?.isAtsFriendly == true) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "ATS Verified",
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Text(
                        text = template?.name ?: "ATS Classic",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onSwitchLayout)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ats_switch_style),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

private fun parseHexToColor(hex: String?, fallback: Color): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        val cleanHex = if (hex.startsWith("#")) hex.substring(1) else hex
        if (cleanHex.length == 6) {
            Color(android.graphics.Color.parseColor("#$cleanHex"))
        } else if (cleanHex.length == 8) {
            Color(android.graphics.Color.parseColor("#$cleanHex"))
        } else fallback
    } catch (e: Exception) {
        fallback
    }
}
