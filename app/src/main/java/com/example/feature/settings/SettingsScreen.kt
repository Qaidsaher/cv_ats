package com.example.feature.settings

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.designsystem.components.AppTopBar

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAppLangDialog by remember { mutableStateOf(false) }
    var showResumeLangDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPageFormatDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearInfoMessage()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.settings_title),
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // PRO Status Banner
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isProUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToPremium)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (uiState.isProUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = if (uiState.isProUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (uiState.isProUser) "PRO Member Active" else "Upgrade to PRO",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isProUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (uiState.isProUser) "Unlimited exports, no watermarks, all templates unlocked." else "Unlock all professional templates and clean exports.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.isProUser) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = if (uiState.isProUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            // Preferences Card
            item {
                Text(
                    text = "PREFERENCES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            RoundedCornerShape(20.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        SettingsRow(
                            icon = Icons.Default.Language,
                            title = stringResource(R.string.settings_app_language),
                            subtitle = if (uiState.appLanguage == "ar") "العربية (Arabic)" else "English",
                            onClick = { showAppLangDialog = true }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        SettingsRow(
                            icon = Icons.Default.Description,
                            title = stringResource(R.string.settings_default_resume_lang),
                            subtitle = if (uiState.defaultResumeLang == "ar") "العربية (RTL)" else "English (LTR)",
                            onClick = { showResumeLangDialog = true }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        SettingsRow(
                            icon = Icons.Default.DarkMode,
                            title = stringResource(R.string.settings_theme),
                            subtitle = when (uiState.appTheme) {
                                "dark" -> stringResource(R.string.theme_dark)
                                "light" -> stringResource(R.string.theme_light)
                                else -> stringResource(R.string.theme_system)
                            },
                            onClick = { showThemeDialog = true }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        SettingsRow(
                            icon = Icons.Default.FormatSize,
                            title = stringResource(R.string.settings_default_page_format),
                            subtitle = if (uiState.defaultPageFormat == "A4") "A4 (Standard)" else "US Letter",
                            onClick = { showPageFormatDialog = true }
                        )
                    }
                }
            }

            // About Card
            item {
                Text(
                    text = "ABOUT & BACKUP",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            RoundedCornerShape(20.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        SettingsRow(
                            icon = Icons.Default.Info,
                            title = stringResource(R.string.settings_about),
                            subtitle = "Resume Chronicle v1.0 • Professional ATS Engine",
                            onClick = { showAboutDialog = true }
                        )
                    }
                }
            }
        }
    }

    // App Language Dialog
    if (showAppLangDialog) {
        AlertDialog(
            onDismissRequest = { showAppLangDialog = false },
            title = { Text(stringResource(R.string.settings_app_language), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setAppLanguage("en")
                                showAppLangDialog = false
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(selected = uiState.appLanguage == "en", onClick = {
                            viewModel.setAppLanguage("en")
                            showAppLangDialog = false
                        })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("English")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setAppLanguage("ar")
                                showAppLangDialog = false
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(selected = uiState.appLanguage == "ar", onClick = {
                            viewModel.setAppLanguage("ar")
                            showAppLangDialog = false
                        })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("العربية (Arabic)")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAppLangDialog = false }) { Text("Close") }
            }
        )
    }

    // Resume Language Dialog
    if (showResumeLangDialog) {
        AlertDialog(
            onDismissRequest = { showResumeLangDialog = false },
            title = { Text(stringResource(R.string.settings_default_resume_lang), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setDefaultResumeLang("en")
                                showResumeLangDialog = false
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(selected = uiState.defaultResumeLang == "en", onClick = {
                            viewModel.setDefaultResumeLang("en")
                            showResumeLangDialog = false
                        })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("English (Left-to-Right)")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setDefaultResumeLang("ar")
                                showResumeLangDialog = false
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(selected = uiState.defaultResumeLang == "ar", onClick = {
                            viewModel.setDefaultResumeLang("ar")
                            showResumeLangDialog = false
                        })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("العربية (Right-to-Left)")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showResumeLangDialog = false }) { Text("Close") }
            }
        )
    }

    // Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.settings_theme), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf(
                        "system" to stringResource(R.string.theme_system),
                        "light" to stringResource(R.string.theme_light),
                        "dark" to stringResource(R.string.theme_dark)
                    ).forEach { (key, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setAppTheme(key)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(selected = uiState.appTheme == key, onClick = {
                                viewModel.setAppTheme(key)
                                showThemeDialog = false
                            })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Close") }
            }
        )
    }

    // Page Format Dialog
    if (showPageFormatDialog) {
        AlertDialog(
            onDismissRequest = { showPageFormatDialog = false },
            title = { Text(stringResource(R.string.settings_default_page_format), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("A4" to "A4 (Standard 210 × 297 mm)", "LETTER" to "US Letter (8.5 × 11 in)").forEach { (key, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setDefaultPageFormat(key)
                                    showPageFormatDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(selected = uiState.defaultPageFormat == key, onClick = {
                                viewModel.setDefaultPageFormat(key)
                                showPageFormatDialog = false
                            })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPageFormatDialog = false }) { Text("Close") }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Resume Chronicle", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Craft professional, ATS-friendly resumes and export crisp, vector-quality PDF documents seamlessly.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Version: 1.0.0\nNative Android Print Engine & PDFBox Compatible",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}
