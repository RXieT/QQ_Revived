package me.padi.qqlite.revived.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme as materialDarkColorScheme
import androidx.compose.material3.lightColorScheme as materialLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

private val LocalRevivedThemePreference = staticCompositionLocalOf { RevivedThemePreference() }

@Composable
fun currentRevivedThemePreference(): RevivedThemePreference = LocalRevivedThemePreference.current

@Composable
fun currentUiMode(): RevivedUiMode = currentRevivedThemePreference().uiMode

@Composable
fun RevivedTheme(content: @Composable () -> Unit) {
    // 在子树读取偏好之前完成首次加载；状态对象不持有 Activity。
    RevivedThemeState.initialize(LocalContext.current)
    val preference = RevivedThemeState.preference
    val dark = when (preference.mode) {
        RevivedThemeMode.System -> isSystemInDarkTheme()
        RevivedThemeMode.Light -> false
        RevivedThemeMode.Dark -> true
    }
    val colors = remember(dark, preference.preset) {
        val accent = when (preference.preset) {
            RevivedThemePreset.Blue -> Color(0xFF2563C5)
            RevivedThemePreset.Green -> Color(0xFF16734B)
            RevivedThemePreset.Purple -> Color(0xFF7945B8)
        }
        val primary = if (dark) lerp(accent, Color.White, 0.50f) else accent
        val onPrimary = if (dark) Color(0xFF101318) else Color.White
        val base = if (dark) darkColorScheme() else lightColorScheme()
        base.copy(
            primary = primary,
            onPrimary = onPrimary,
            primaryVariant = accent,
            onPrimaryVariant = Color.White,
            primaryContainer = if (dark) lerp(accent, Color.Black, 0.45f)
                else lerp(accent, Color.White, 0.86f),
            onPrimaryContainer = if (dark) lerp(accent, Color.White, 0.85f)
                else lerp(accent, Color.Black, 0.60f),
            disabledPrimary = lerp(base.surface, primary, 0.30f),
            disabledOnPrimary = lerp(base.surface, onPrimary, 0.50f),
            disabledPrimaryButton = lerp(base.surface, primary, 0.30f),
            disabledOnPrimaryButton = lerp(base.surface, onPrimary, 0.50f),
            disabledPrimarySlider = lerp(base.surface, primary, 0.30f),
        )
    }
    val materialColors = remember(dark, colors) {
        val base = if (dark) materialDarkColorScheme() else materialLightColorScheme()
        base.copy(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.primaryContainer,
            onPrimaryContainer = colors.onPrimaryContainer,
            secondary = colors.primary,
            onSecondary = colors.onPrimary,
            secondaryContainer = colors.primaryContainer,
            onSecondaryContainer = colors.onPrimaryContainer,
            tertiary = colors.primary,
            onTertiary = colors.onPrimary,
            tertiaryContainer = colors.primaryContainer,
            onTertiaryContainer = colors.onPrimaryContainer,
            background = colors.background,
            onBackground = colors.onBackground,
            surface = colors.surface,
            onSurface = colors.onSurface,
            error = colors.error,
            onError = colors.onError,
            errorContainer = colors.errorContainer,
            onErrorContainer = colors.onErrorContainer,
        )
    }
    CompositionLocalProvider(LocalRevivedThemePreference provides preference) {
        MaterialTheme(colorScheme = materialColors) {
            MiuixTheme(colors = colors, content = content)
        }
    }
}
