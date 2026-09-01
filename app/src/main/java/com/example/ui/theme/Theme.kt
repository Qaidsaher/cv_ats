package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Editorial Aesthetic Color Schemes
private val LightColorScheme = lightColorScheme(
    primary = EditorialCharcoal,          // #2D2A26
    onPrimary = EditorialPaperWhite,      // #FDFBF9
    primaryContainer = EditorialSand,     // #E7E2D9
    onPrimaryContainer = EditorialInk,    // #1C1B17
    secondary = EditorialSlate,           // #5C5852
    onSecondary = EditorialPaperWhite,
    secondaryContainer = EditorialCardSurface, // #F2EFE9
    onSecondaryContainer = EditorialInk,
    tertiary = EditorialMuted,            // #8C877E
    onTertiary = EditorialPaperWhite,
    tertiaryContainer = EditorialAccent,  // #C9C2B5
    onTertiaryContainer = EditorialCharcoal,
    background = EditorialPaperWhite,     // #FDFBF9
    onBackground = EditorialInk,          // #1C1B17
    surface = Color.White,
    onSurface = EditorialInk,
    surfaceVariant = EditorialCardSurface,// #F2EFE9
    onSurfaceVariant = EditorialSlate,    // #5C5852
    outline = EditorialBorder,            // #D9D4CC
    outlineVariant = EditorialSand        // #E7E2D9
)

private val DarkColorScheme = darkColorScheme(
    primary = EditorialSand,              // #E7E2D9
    onPrimary = EditorialInk,             // #1C1B17
    primaryContainer = EditorialDarkCard, // #2D2A26
    onPrimaryContainer = EditorialDarkText,
    secondary = EditorialAccent,          // #C9C2B5
    onSecondary = EditorialInk,
    secondaryContainer = EditorialDarkSurface,
    onSecondaryContainer = EditorialDarkText,
    tertiary = EditorialWarmTan,          // #A69F92
    onTertiary = EditorialInk,
    background = EditorialDarkBackground, // #181714
    onBackground = EditorialDarkText,
    surface = EditorialDarkSurface,       // #24221E
    onSurface = EditorialDarkText,
    surfaceVariant = EditorialDarkCard,   // #2D2A26
    onSurfaceVariant = EditorialDarkMuted,// #A69F92
    outline = EditorialDarkBorder         // #423E37
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted brand theme by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
