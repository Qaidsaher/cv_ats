package com.example.feature.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.templates.ResumeDocumentPreview
import com.example.domain.model.SampleData
import com.example.domain.model.TemplateSpec

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesGalleryScreen(
    viewModel: TemplatesViewModel,
    targetResumeId: String? = null,
    onTemplateApplied: () -> Unit = {},
    onCreateFromTemplate: (String) -> Unit = {},
    onNavigateToPremium: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTemplateForDetail by remember { mutableStateOf<TemplateSpec?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val categories = listOf(
        "all" to stringResource(R.string.tab_all),
        "ats" to stringResource(R.string.tab_ats),
        "professional" to stringResource(R.string.tab_professional),
        "modern" to stringResource(R.string.tab_modern),
        "arabic" to stringResource(R.string.tab_arabic),
        "minimal" to stringResource(R.string.tab_minimal),
        "premium" to stringResource(R.string.tab_premium),
        "favorites" to stringResource(R.string.tab_favorites)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.templates_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { (key, label) ->
                    val isSelected = uiState.selectedCategory == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setCategory(key) },
                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (uiState.isLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Templates Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.filteredTemplates, key = { it.id }) { template ->
                        TemplateGalleryItem(
                            template = template,
                            isFavorite = uiState.favoriteTemplateIds.contains(template.id),
                            isProUser = uiState.isProUser,
                            onToggleFavorite = { viewModel.toggleFavorite(template.id) },
                            onClick = { selectedTemplateForDetail = template }
                        )
                    }
                }
            }
        }
    }

    // Detail / Preview Bottom Sheet
    if (selectedTemplateForDetail != null) {
        val template = selectedTemplateForDetail!!
        val isLocked = template.isPremium && !uiState.isProUser
        val sampleResume = remember(template.id, template.supportsRtl) {
            if (template.category.equals("arabic", ignoreCase = true) || template.supportsRtl) {
                SampleData.createArabicSampleResume(template.id)
            } else {
                SampleData.createEnglishSampleResume(template.id)
            }
        }

        ModalBottomSheet(
            onDismissRequest = { selectedTemplateForDetail = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = template.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = template.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (template.isPremium) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiary
                        ) {
                            Text(
                                text = "PRO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Miniature live document preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ResumeDocumentPreview(
                        resume = sampleResume,
                        template = template,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isLocked) {
                    Button(
                        onClick = {
                            selectedTemplateForDetail = null
                            onNavigateToPremium()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unlock with Lifetime Pro", fontWeight = FontWeight.Bold)
                    }
                } else if (targetResumeId != null) {
                    Button(
                        onClick = {
                            viewModel.applyTemplateToResume(targetResumeId, template.id) {
                                selectedTemplateForDetail = null
                                onTemplateApplied()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(stringResource(R.string.templates_use_template), fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            selectedTemplateForDetail = null
                            onCreateFromTemplate(template.id)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(stringResource(R.string.home_create_resume), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun TemplateGalleryItem(
    template: TemplateSpec,
    isFavorite: Boolean,
    isProUser: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    val primaryColor = Color(android.graphics.Color.parseColor(template.colors.primary))
    val sidebarColor = Color(android.graphics.Color.parseColor(template.colors.sidebar))

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .testTag("template_card_${template.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Visual Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.78f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            ) {
                // Simplified template graphic layout representation
                Row(modifier = Modifier.fillMaxSize()) {
                    if (template.layout.type == "sidebar_start") {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(36.dp)
                                .background(sidebarColor)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(6.dp)
                    ) {
                        if (template.layout.type == "header_split") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(18.dp)
                                    .background(primaryColor, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(6.dp)
                                    .background(primaryColor, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        repeat(4) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .background(Color(0xFFE5E7EB), RoundedCornerShape(1.dp))
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(35.dp)
                                .height(4.dp)
                                .background(primaryColor.copy(alpha = 0.7f), RoundedCornerShape(1.dp))
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        repeat(5) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .background(Color(0xFFE5E7EB), RoundedCornerShape(1.dp))
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                        }
                    }
                    if (template.layout.type == "sidebar_end") {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(36.dp)
                                .background(sidebarColor)
                        )
                    }
                }

                // Badges on preview
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (template.isAtsFriendly) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF1F6F5C)
                        ) {
                            Text(
                                text = "ATS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (template.supportsRtl) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "AR",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Favorite button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFE53935) else Color(0xFF888888),
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (template.isPremium && !isProUser) {
                    Surface(
                        shape = RoundedCornerShape(bottomStart = 8.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "PRO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = template.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = template.category.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
