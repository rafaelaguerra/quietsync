package com.rafaelaguerra.synctask.ui.theme

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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightScheme = lightColorScheme(
    primary = TealDeep,
    onPrimary = White,
    primaryContainer = TealSoft,
    onPrimaryContainer = Color(0xFF0E2A2C),
    secondary = Clay,
    onSecondary = White,
    secondaryContainer = ClaySoft,
    onSecondaryContainer = Color(0xFF3C261E),
    tertiary = Indigo,
    onTertiary = White,
    tertiaryContainer = IndigoSoft,
    onTertiaryContainer = Color(0xFF1B2A4A),
    error = Color(0xFFBA1A1A),
    onError = White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = WarmCream,
    onBackground = Charcoal,
    surface = LightSurface,
    onSurface = Charcoal,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = WarmGray,
    outline = LightOutline,
    outlineVariant = Color(0xFFECE6DA),
    inverseSurface = Charcoal,
    inverseOnSurface = WarmCream,
    inversePrimary = TealSoft,
    surfaceTint = TealDeep
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF7DBEC2),
    onPrimary = Color(0xFF0B2E30),
    primaryContainer = TealContainerDark,
    onPrimaryContainer = TealOnContainerDark,
    secondary = Color(0xFFE8B69E),
    onSecondary = Color(0xFF40221B),
    secondaryContainer = ClayContainerDark,
    onSecondaryContainer = ClayOnContainerDark,
    tertiary = Color(0xFF9FB3D9),
    onTertiary = Color(0xFF1E2A4A),
    tertiaryContainer = IndigoContainerDark,
    onTertiaryContainer = IndigoOnContainerDark,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = Color(0xFF22292C),
    inverseSurface = DarkOnSurface,
    inverseOnSurface = DarkSurface,
    inversePrimary = TealDeep,
    surfaceTint = Color(0xFF7DBEC2)
)

@Composable
fun SynctaskTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Use brand palette by default; dynamic color stays opt-in for users on Android 12+.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkScheme
        else -> LightScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            val isLightBackground = colorScheme.background.luminance() > 0.5f
            controller.isAppearanceLightStatusBars = isLightBackground
            controller.isAppearanceLightNavigationBars = isLightBackground
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = SynctaskShapes,
        content = content
    )
}
