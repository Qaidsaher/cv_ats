package com.example.core.templates

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.Experience
import com.example.domain.model.Resume
import com.example.domain.model.TemplateSpec

@Composable
fun ResumeDocumentPreview(
    resume: Resume,
    template: TemplateSpec,
    modifier: Modifier = Modifier
) {
    val isRtl = resume.isRtl
    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    // Parse custom/template colors
    val primaryColor = remember(resume.customization.primaryColorHex, template.colors.primary) {
        parseComposeColor(resume.customization.primaryColorHex ?: template.colors.primary, Color(0xFF1F6F5C))
    }
    val secondaryColor = remember(resume.customization.secondaryColorHex, template.colors.secondary) {
        parseComposeColor(resume.customization.secondaryColorHex ?: template.colors.secondary, Color(0xFF444444))
    }
    val accentColor = remember(resume.customization.accentColorHex, template.colors.accent) {
        parseComposeColor(resume.customization.accentColorHex ?: template.colors.accent, Color(0xFFE0A96D))
    }
    val backgroundColor = remember(template.colors.background) {
        parseComposeColor(template.colors.background, Color.White)
    }
    val sidebarColor = remember(template.colors.sidebar) {
        parseComposeColor(template.colors.sidebar, Color(0xFFF3F7F5))
    }
    val textPrimaryColor = remember(template.colors.textPrimary) {
        parseComposeColor(template.colors.textPrimary, Color(0xFF111111))
    }
    val textSecondaryColor = remember(template.colors.textSecondary) {
        parseComposeColor(template.colors.textSecondary, Color(0xFF444444))
    }

    val isLetter = resume.customization.pageFormat.equals("LETTER", ignoreCase = true)
    val pageAspectRatio = if (isLetter) 612f / 792f else 595f / 842f

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        CardContainer(
            aspectRatio = pageAspectRatio,
            backgroundColor = backgroundColor,
            modifier = modifier
        ) {
            when (template.layout.type) {
                "sidebar_start" -> {
                    SidebarLayoutPreview(
                        resume = resume,
                        template = template,
                        sidebarOnStart = true,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        accentColor = accentColor,
                        sidebarColor = sidebarColor,
                        textPrimary = textPrimaryColor,
                        textSecondary = textSecondaryColor
                    )
                }
                "sidebar_end" -> {
                    SidebarLayoutPreview(
                        resume = resume,
                        template = template,
                        sidebarOnStart = false,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        accentColor = accentColor,
                        sidebarColor = sidebarColor,
                        textPrimary = textPrimaryColor,
                        textSecondary = textSecondaryColor
                    )
                }
                "header_split" -> {
                    HeaderSplitLayoutPreview(
                        resume = resume,
                        template = template,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        accentColor = accentColor,
                        textPrimary = textPrimaryColor,
                        textSecondary = textSecondaryColor
                    )
                }
                else -> {
                    SingleColumnLayoutPreview(
                        resume = resume,
                        template = template,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        accentColor = accentColor,
                        textPrimary = textPrimaryColor,
                        textSecondary = textSecondaryColor
                    )
                }
            }
        }
    }
}

@Composable
private fun CardContainer(
    aspectRatio: Float,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .shadow(6.dp, RoundedCornerShape(4.dp))
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .border(0.5.dp, Color(0x1F000000), RoundedCornerShape(4.dp))
    ) {
        content()
    }
}

@Composable
private fun SingleColumnLayoutPreview(
    resume: Resume,
    template: TemplateSpec,
    primaryColor: Color,
    secondaryColor: Color,
    accentColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    val isRtl = resume.isRtl
    val scale = resume.customization.fontSizeScale

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (template.photo.enabled && resume.customization.showPhoto && !resume.personalInfo.profilePhotoUri.isNullOrBlank()) {
                AsyncImage(
                    model = resume.personalInfo.profilePhotoUri,
                    contentDescription = "Profile Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size((46 * scale).dp)
                        .clip(if (template.photo.shape == "circle") CircleShape else RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resume.personalInfo.fullName,
                    fontSize = (16 * scale).sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                if (resume.personalInfo.professionalTitle.isNotBlank()) {
                    Text(
                        text = resume.personalInfo.professionalTitle,
                        fontSize = (10 * scale).sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
            }
        }

        // Contact Info Line
        val contactItems = listOf(
            resume.personalInfo.email,
            resume.personalInfo.phone,
            resume.personalInfo.locationFormatted
        ).filter { it.isNotBlank() }.joinToString("  •  ")

        if (contactItems.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = contactItems,
                fontSize = (7.5 * scale).sp,
                color = textSecondary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(thickness = 1.dp, color = primaryColor)
        Spacer(modifier = Modifier.height(8.dp))

        // Sections
        for (sectionKey in resume.sectionOrder) {
            if (resume.sectionVisibility[sectionKey] == false) continue

            when (sectionKey) {
                "summary" -> {
                    if (resume.summary.isNotBlank()) {
                        SectionHeaderView(if (isRtl) "الملخص المهني" else "Professional Summary", primaryColor, scale)
                        Text(
                            text = resume.summary,
                            fontSize = (7.5 * scale).sp,
                            color = textPrimary,
                            lineHeight = (10.5 * scale).sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
                "experience" -> {
                    if (resume.experiences.isNotEmpty()) {
                        SectionHeaderView(if (isRtl) "الخبرات المهنية" else "Experience", primaryColor, scale)
                        for (exp in resume.experiences) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${exp.jobTitle} – ${exp.company}",
                                    fontSize = (8 * scale).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Text(
                                    text = "${exp.startDate} - ${if (exp.currentlyWorking) (if (isRtl) "الآن" else "Present") else exp.endDate}",
                                    fontSize = (7 * scale).sp,
                                    color = textSecondary
                                )
                            }
                            if (exp.description.isNotBlank()) {
                                Text(
                                    text = exp.description,
                                    fontSize = (7 * scale).sp,
                                    color = textPrimary,
                                    lineHeight = (9.5 * scale).sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                    }
                }
                "education" -> {
                    if (resume.educations.isNotEmpty()) {
                        SectionHeaderView(if (isRtl) "التعليم والمؤهلات" else "Education", primaryColor, scale)
                        for (edu in resume.educations) {
                            Text(
                                text = "${edu.degree}${if (edu.fieldOfStudy.isNotBlank()) " in ${edu.fieldOfStudy}" else ""} – ${edu.institution}",
                                fontSize = (7.5 * scale).sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
                "skills" -> {
                    if (resume.skills.isNotEmpty()) {
                        SectionHeaderView(if (isRtl) "المهارات" else "Skills", primaryColor, scale)
                        val skillsStr = resume.skills.joinToString(", ") { "${it.name}${if (it.level.isNotBlank()) " (${it.level})" else ""}" }
                        Text(
                            text = skillsStr,
                            fontSize = (7.5 * scale).sp,
                            color = textPrimary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
                "languages" -> {
                    if (resume.languages.isNotEmpty()) {
                        SectionHeaderView(if (isRtl) "اللغات" else "Languages", primaryColor, scale)
                        val langStr = resume.languages.joinToString("  •  ") { "${it.name} (${it.level})" }
                        Text(
                            text = langStr,
                            fontSize = (7.5 * scale).sp,
                            color = textPrimary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarLayoutPreview(
    resume: Resume,
    template: TemplateSpec,
    sidebarOnStart: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    accentColor: Color,
    sidebarColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    val isRtl = resume.isRtl
    val scale = resume.customization.fontSizeScale

    Row(modifier = Modifier.fillMaxSize()) {
        val sidebarContent: @Composable () -> Unit = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(100.dp)
                    .background(sidebarColor)
                    .padding(10.dp)
            ) {
                // Photo in Sidebar
                if (template.photo.enabled && resume.customization.showPhoto && !resume.personalInfo.profilePhotoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = resume.personalInfo.profilePhotoUri,
                        contentDescription = "Profile Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(46.dp)
                            .align(Alignment.CenterHorizontally)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Contact
                SectionHeaderView(if (isRtl) "التواصل" else "Contact", primaryColor, scale)
                listOfNotNull(
                    resume.personalInfo.email.takeIf { it.isNotBlank() },
                    resume.personalInfo.phone.takeIf { it.isNotBlank() },
                    resume.personalInfo.locationFormatted.takeIf { it.isNotBlank() }
                ).forEach {
                    Text(
                        text = it,
                        fontSize = (6.5 * scale).sp,
                        color = textPrimary,
                        lineHeight = 9.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                // Skills in Sidebar
                if (resume.skills.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    SectionHeaderView(if (isRtl) "المهارات" else "Skills", primaryColor, scale)
                    resume.skills.take(5).forEach {
                        Text(
                            text = "• ${it.name}",
                            fontSize = (6.5 * scale).sp,
                            color = textPrimary,
                            modifier = Modifier.padding(bottom = 1.5.dp)
                        )
                    }
                }

                // Languages in Sidebar
                if (resume.languages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    SectionHeaderView(if (isRtl) "اللغات" else "Languages", primaryColor, scale)
                    resume.languages.forEach {
                        Text(
                            text = "• ${it.name} (${it.level})",
                            fontSize = (6.5 * scale).sp,
                            color = textPrimary,
                            modifier = Modifier.padding(bottom = 1.5.dp)
                        )
                    }
                }
            }
        }

        val mainContent: @Composable () -> Unit = {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(12.dp)
            ) {
                Text(
                    text = resume.personalInfo.fullName,
                    fontSize = (15 * scale).sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                if (resume.personalInfo.professionalTitle.isNotBlank()) {
                    Text(
                        text = resume.personalInfo.professionalTitle,
                        fontSize = (9 * scale).sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Summary
                if (resume.summary.isNotBlank()) {
                    SectionHeaderView(if (isRtl) "الملخص المهني" else "Summary", primaryColor, scale)
                    Text(
                        text = resume.summary,
                        fontSize = (7 * scale).sp,
                        color = textPrimary,
                        lineHeight = (9.5 * scale).sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                // Experience
                if (resume.experiences.isNotEmpty()) {
                    SectionHeaderView(if (isRtl) "الخبرات المهنية" else "Experience", primaryColor, scale)
                    for (exp in resume.experiences) {
                        Text(
                            text = "${exp.jobTitle} - ${exp.company}",
                            fontSize = (7.5 * scale).sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "${exp.startDate} - ${if (exp.currentlyWorking) (if (isRtl) "الآن" else "Present") else exp.endDate}",
                            fontSize = (6.5 * scale).sp,
                            color = textSecondary
                        )
                        if (exp.description.isNotBlank()) {
                            Text(
                                text = exp.description,
                                fontSize = (6.5 * scale).sp,
                                color = textPrimary,
                                lineHeight = (8.5 * scale).sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }

                // Education
                if (resume.educations.isNotEmpty()) {
                    SectionHeaderView(if (isRtl) "التعليم" else "Education", primaryColor, scale)
                    for (edu in resume.educations) {
                        Text(
                            text = "${edu.degree} – ${edu.institution}",
                            fontSize = (7 * scale).sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                    }
                }
            }
        }

        if (sidebarOnStart) {
            sidebarContent()
            mainContent()
        } else {
            mainContent()
            sidebarContent()
        }
    }
}

@Composable
private fun HeaderSplitLayoutPreview(
    resume: Resume,
    template: TemplateSpec,
    primaryColor: Color,
    secondaryColor: Color,
    accentColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    val isRtl = resume.isRtl
    val scale = resume.customization.fontSizeScale

    Column(modifier = Modifier.fillMaxSize()) {
        // Dark Top Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(primaryColor)
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = resume.personalInfo.fullName,
                    fontSize = (16 * scale).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (resume.personalInfo.professionalTitle.isNotBlank()) {
                    Text(
                        text = resume.personalInfo.professionalTitle,
                        fontSize = (9.5 * scale).sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
                val contacts = listOf(
                    resume.personalInfo.email,
                    resume.personalInfo.phone,
                    resume.personalInfo.locationFormatted
                ).filter { it.isNotBlank() }.joinToString("  |  ")
                if (contacts.isNotBlank()) {
                    Text(
                        text = contacts,
                        fontSize = (7 * scale).sp,
                        color = Color(0xFFD1D5DB),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // Body Content
        Column(modifier = Modifier.padding(14.dp)) {
            if (resume.summary.isNotBlank()) {
                SectionHeaderView(if (isRtl) "الملخص المهني" else "Professional Summary", primaryColor, scale)
                Text(
                    text = resume.summary,
                    fontSize = (7.5 * scale).sp,
                    color = textPrimary,
                    lineHeight = (10 * scale).sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            if (resume.experiences.isNotEmpty()) {
                SectionHeaderView(if (isRtl) "الخبرات المهنية" else "Experience", primaryColor, scale)
                for (exp in resume.experiences) {
                    Text(
                        text = "${exp.jobTitle} - ${exp.company} (${exp.startDate} - ${if (exp.currentlyWorking) (if (isRtl) "الآن" else "Present") else exp.endDate})",
                        fontSize = (7.5 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    if (exp.description.isNotBlank()) {
                        Text(
                            text = exp.description,
                            fontSize = (7 * scale).sp,
                            color = textPrimary,
                            lineHeight = (9 * scale).sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }

            if (resume.skills.isNotEmpty()) {
                SectionHeaderView(if (isRtl) "المهارات" else "Skills", primaryColor, scale)
                Text(
                    text = resume.skills.joinToString(", ") { "${it.name} (${it.level})" },
                    fontSize = (7.5 * scale).sp,
                    color = textPrimary
                )
            }
        }
    }
}

@Composable
private fun SectionHeaderView(title: String, color: Color, scale: Float) {
    Column(modifier = Modifier.padding(bottom = 4.dp, top = 4.dp)) {
        Text(
            text = title,
            fontSize = (8.5 * scale).sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        HorizontalDivider(thickness = 0.8.dp, color = color.copy(alpha = 0.6f))
    }
}

private fun parseComposeColor(hex: String?, fallback: Color): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}
