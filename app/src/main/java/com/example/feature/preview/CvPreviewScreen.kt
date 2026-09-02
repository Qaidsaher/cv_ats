package com.example.feature.preview

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.designsystem.components.AppTopBar
import com.example.core.templates.ResumeDocumentPreview
import com.example.feature.templates.AtsTemplateSelectorBar
import com.example.feature.templates.AtsTemplateSelectorModalSheet

val COLOR_PALETTES = listOf(
    "#1F6F5C" to "Emerald",
    "#1E3A8A" to "Navy",
    "#831843" to "Crimson",
    "#18181B" to "Charcoal",
    "#9A3412" to "Amber",
    "#065F46" to "Forest",
    "#4C1D95" to "Violet",
    "#374151" to "Slate"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CvPreviewScreen(
    viewModel: PreviewViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToExport: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val resume = uiState.resume
    val template = uiState.template

    var showCustomizeSheet by remember { mutableStateOf(false) }
    var showLinkedInExportSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val linkedInSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = androidx.compose.ui.platform.LocalContext.current

    // Pinch-to-zoom / Pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.8f, 3.0f)
        offset += offsetChange
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = resume?.title ?: stringResource(R.string.preview_title),
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = { showLinkedInExportSheet = true },
                        modifier = Modifier.testTag("preview_linkedin_json_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DataObject,
                            contentDescription = "Export for LinkedIn (JSON)"
                        )
                    }
                    IconButton(
                        onClick = { showCustomizeSheet = true },
                        modifier = Modifier.testTag("preview_customize_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Customize"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (resume != null) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Rich ATS template switcher bar
                        AtsTemplateSelectorBar(
                            templates = uiState.allTemplates,
                            selectedTemplateId = resume.templateId,
                            onSelectTemplate = { viewModel.changeTemplate(it) },
                            onOpenFullSelector = { viewModel.setTemplateSelectorSheetVisible(true) }
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showCustomizeSheet = true },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("bottom_customize_button")
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.preview_customize))
                            }

                            Button(
                                onClick = { onNavigateToExport(resume.id) },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("bottom_export_pdf_button")
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.action_export), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (resume == null || template == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Interactive Preview Canvas Container with warm editorial parchment background
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .transformable(state = transformableState)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                ) {
                    ResumeDocumentPreview(
                        resume = resume,
                        template = template,
                        modifier = Modifier.fillMaxWidth(0.92f)
                    )
                }

                // Reset zoom pill if zoomed
                if (scale != 1f || offset != Offset.Zero) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clickable {
                                scale = 1f
                                offset = Offset.Zero
                            }
                    ) {
                        Text(
                            text = "Reset Zoom",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }

    // Customization Modal Sheet
    if (showCustomizeSheet && resume != null) {
        val customization = resume.customization

        ModalBottomSheet(
            onDismissRequest = { showCustomizeSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.preview_customize),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Color Theme Picker
                Text(
                    text = stringResource(R.string.custom_theme_color),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(COLOR_PALETTES) { (hex, name) ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = (customization.primaryColorHex ?: template?.colors?.primary) == hex

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable {
                                    viewModel.updateCustomization(
                                        customization.copy(primaryColorHex = hex)
                                    )
                                }
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                                    shape = CircleShape
                                )
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color.White, CircleShape)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Font Size Scaling Slider
                Text(
                    text = stringResource(R.string.custom_font_size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = customization.fontSizeScale,
                    onValueChange = { scaleVal ->
                        viewModel.updateCustomization(
                            customization.copy(fontSizeScale = scaleVal)
                        )
                    },
                    valueRange = 0.8f..1.3f,
                    steps = 5,
                    modifier = Modifier.testTag("slider_font_size")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Photo Visibility Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.custom_show_photo),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = customization.showPhoto,
                        onCheckedChange = { show ->
                            viewModel.updateCustomization(
                                customization.copy(showPhoto = show)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Page Format Selector (A4 vs Letter)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.custom_page_format),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = customization.pageFormat == "A4",
                            onClick = {
                                viewModel.updateCustomization(customization.copy(pageFormat = "A4"))
                            },
                            label = { Text("A4") }
                        )
                        FilterChip(
                            selected = customization.pageFormat == "LETTER",
                            onClick = {
                                viewModel.updateCustomization(customization.copy(pageFormat = "LETTER"))
                            },
                            label = { Text("US Letter") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    var pendingJsonContent by remember { mutableStateOf<String?>(null) }
    val createJsonDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        val content = pendingJsonContent
        if (uri != null && content != null) {
            com.example.core.export.LinkedInJsonExporter.saveJsonToUri(context, content, uri)
        }
        pendingJsonContent = null
    }

    if (uiState.showTemplateSelectorSheet && resume != null) {
        AtsTemplateSelectorModalSheet(
            templates = uiState.allTemplates,
            selectedTemplateId = resume.templateId,
            onSelectTemplate = { viewModel.changeTemplate(it) },
            onDismiss = { viewModel.setTemplateSelectorSheetVisible(false) }
        )
    }

    if (showLinkedInExportSheet && resume != null) {
        ModalBottomSheet(
            onDismissRequest = { showLinkedInExportSheet = false },
            sheetState = linkedInSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                com.example.feature.export.LinkedInJsonExportCard(
                    resume = resume,
                    onSaveJsonFile = { format, jsonContent, defaultFileName ->
                        pendingJsonContent = jsonContent
                        createJsonDocumentLauncher.launch(defaultFileName)
                    },
                    onShareJson = { format, jsonContent, defaultFileName ->
                        com.example.core.export.LinkedInJsonExporter.shareJson(context, jsonContent, defaultFileName)
                    },
                    onCopyJson = { jsonContent ->
                        com.example.core.export.LinkedInJsonExporter.copyToClipboard(context, jsonContent)
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
