package me.padi.qqlite.revived.compose.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import me.padi.qqlite.revived.utils.findActivityWindow

/** 只调整当前 Activity 的系统栏，不从 UI 反向引用 Hook。 */
@Composable
internal fun ApplyTransparentSystemBars(surfaceColor: Color) {
    val context = LocalContext.current
    SideEffect {
        val window = context.findActivityWindow() ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        @Suppress("DEPRECATION")
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        @Suppress("DEPRECATION")
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        window.isStatusBarContrastEnforced = false
        window.isNavigationBarContrastEnforced = false
        WindowCompat.getInsetsController(window, window.decorView).apply {
            val light = surfaceColor.luminance() > 0.5f
            isAppearanceLightStatusBars = light
            isAppearanceLightNavigationBars = light
        }
    }
}
