package com.cyclesync.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.cyclesync.core.constants.AppColors

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = Color.White,
    secondary = AppColors.Secondary,
    onSecondary = Color.White,
    tertiary = AppColors.Tertiary,
    onTertiary = Color.White,
    background = AppColors.BackgroundLight,
    onBackground = AppColors.TextPrimaryLight,
    surface = AppColors.SurfaceLight,
    onSurface = AppColors.TextPrimaryLight,
    surfaceVariant = Color(0xFFFCE4EC),
    onSurfaceVariant = AppColors.TextSecondaryLight,
    error = AppColors.Error,
    onError = Color.White,
    outline = Color(0xFFF8BBD0),
    primaryContainer = Color(0xFFFCE4EC),
    onPrimaryContainer = Color(0xFF880E4F),
    secondaryContainer = Color(0xFFF8BBD0),
    onSecondaryContainer = Color(0xFF880E4F),
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Color(0xFFB71C1C)
)

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Secondary,
    onPrimary = Color.White,
    secondary = AppColors.Tertiary,
    onSecondary = Color.White,
    tertiary = AppColors.Tertiary,
    onTertiary = Color.White,
    background = AppColors.BackgroundDark,
    onBackground = AppColors.TextPrimaryDark,
    surface = AppColors.SurfaceDark,
    onSurface = AppColors.TextPrimaryDark,
    surfaceVariant = Color(0xFF4A2040),
    onSurfaceVariant = AppColors.TextSecondaryDark,
    error = AppColors.Error,
    onError = Color.White,
    outline = Color(0xFF7B4A6E),
    primaryContainer = Color(0xFF880E4F),
    onPrimaryContainer = Color(0xFFFCE4EC),
    secondaryContainer = Color(0xFF6A1B4D),
    onSecondaryContainer = Color(0xFFF8BBD0),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFFCDD2)
)

@Composable
fun CycleSyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
