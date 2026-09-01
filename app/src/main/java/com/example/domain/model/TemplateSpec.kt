package com.example.domain.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TemplateSpec(
    val id: String,
    val version: Int = 1,
    val schemaVersion: Int = 1,
    val name: String,
    val description: String = "",
    val isPremium: Boolean = false,
    val isAtsFriendly: Boolean = false,
    val category: String = "general", // ats, professional, modern, arabic, minimal
    val supportedLanguages: List<String> = listOf("en", "ar"),
    val supportsRtl: Boolean = true,
    val page: TemplatePage = TemplatePage(),
    val layout: TemplateLayout = TemplateLayout(),
    val colors: TemplateColors = TemplateColors(),
    val typography: TemplateTypography = TemplateTypography(),
    val photo: TemplatePhoto = TemplatePhoto(),
    val sections: List<String> = emptyList(),
    val customizable: TemplateCustomizable = TemplateCustomizable()
) {
    val isSupportedSchema: Boolean
        get() = schemaVersion <= 1
}

@JsonClass(generateAdapter = true)
data class TemplatePage(
    val size: String = "A4", // A4 or Letter
    val marginTop: Float = 24f,
    val marginBottom: Float = 24f,
    val marginStart: Float = 26f,
    val marginEnd: Float = 26f
)

@JsonClass(generateAdapter = true)
data class TemplateLayout(
    val type: String = "single_column", // single_column, two_column, sidebar_start, sidebar_end, header_split
    val sidebarPosition: String = "none", // none, start, end
    val sidebarWidth: Float = 0.32f, // percentage between 0.2 and 0.45
    val headerStyle: String = "centered_clean", // centered_clean, left_aligned, sidebar_integrated, dark_top_banner, arabic_elegance, tech_modern
    val sectionHeaderStyle: String = "underline_bold" // underline_bold, subtle_divider, accent_badge, gold_border, arabic_badge, tech_pill
)

@JsonClass(generateAdapter = true)
data class TemplateColors(
    val primary: String = "#1A1A1A",
    val secondary: String = "#4A4A4A",
    val accent: String = "#0D5C75",
    val background: String = "#FFFFFF",
    val sidebar: String = "#F4F7F6",
    val textPrimary: String = "#111111",
    val textSecondary: String = "#444444"
)

@JsonClass(generateAdapter = true)
data class TemplateTypography(
    val fontFamily: String = "default", // default, sans_serif, serif, arabic_modern
    val nameSize: Float = 22f,
    val titleSize: Float = 13f,
    val sectionTitleSize: Float = 12f,
    val bodySize: Float = 9.5f,
    val captionSize: Float = 8f
)

@JsonClass(generateAdapter = true)
data class TemplatePhoto(
    val enabled: Boolean = true,
    val shape: String = "circle", // circle, rounded_square, square, none
    val size: Float = 75f
)

@JsonClass(generateAdapter = true)
data class TemplateCustomizable(
    val primaryColor: Boolean = true,
    val secondaryColor: Boolean = true,
    val fontFamily: Boolean = true,
    val fontSize: Boolean = true,
    val spacing: Boolean = true,
    val showPhoto: Boolean = true,
    val pageFormat: Boolean = true
)
