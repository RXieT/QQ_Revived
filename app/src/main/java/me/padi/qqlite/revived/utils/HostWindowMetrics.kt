package me.padi.qqlite.revived.utils

import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/** 使用实际宿主窗口，不依赖被 PhoneResourcesHook 改写过的 DisplayMetrics。 */
internal data class HostWindowMetrics(
    val widthPx: Int = 0,
    val heightPx: Int = 0,
    val statusBarTopPx: Int = 0,
    val navigationBarBottomPx: Int = 0,
    val safeLeftPx: Int = 0,
    val safeRightPx: Int = 0,
    val hasInsets: Boolean = false
)

internal val LocalHostWindowMetrics = staticCompositionLocalOf { HostWindowMetrics() }

@Composable
internal fun ProvideHostWindowMetrics(sourceView: View, content: @Composable () -> Unit) {
    var metrics by remember(sourceView) { mutableStateOf(sourceView.readHostWindowMetrics()) }
    DisposableEffect(sourceView) {
        var observer: ViewTreeObserver? = null
        val listener = ViewTreeObserver.OnPreDrawListener {
            // 不替换宿主 OnApplyWindowInsetsListener，也不消费宿主的 Insets。
            metrics = sourceView.readHostWindowMetrics()
            true
        }
        fun stopObserving() {
            observer?.takeIf { it.isAlive }?.removeOnPreDrawListener(listener)
            observer = null
        }
        fun startObserving() {
            stopObserving()
            metrics = sourceView.readHostWindowMetrics()
            observer = sourceView.viewTreeObserver.also { it.addOnPreDrawListener(listener) }
        }
        val attachment = object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(view: View) = startObserving()
            override fun onViewDetachedFromWindow(view: View) = stopObserving()
        }
        sourceView.addOnAttachStateChangeListener(attachment)
        if (sourceView.isAttachedToWindow) startObserving()
        onDispose {
            sourceView.removeOnAttachStateChangeListener(attachment)
            stopObserving()
        }
    }
    CompositionLocalProvider(LocalHostWindowMetrics provides metrics, content = content)
}

private fun View.readHostWindowMetrics(): HostWindowMetrics {
    val root = rootView
    val insets = ViewCompat.getRootWindowInsets(this)
    val status = insets?.getInsets(WindowInsetsCompat.Type.statusBars())
    val navigation = insets?.getInsets(WindowInsetsCompat.Type.navigationBars())
    val cutout = insets?.getInsets(WindowInsetsCompat.Type.displayCutout())
    return HostWindowMetrics(
        widthPx = root.width,
        heightPx = root.height,
        statusBarTopPx = maxOf(status?.top ?: 0, cutout?.top ?: 0),
        navigationBarBottomPx = maxOf(navigation?.bottom ?: 0, cutout?.bottom ?: 0),
        safeLeftPx = maxOf(navigation?.left ?: 0, cutout?.left ?: 0),
        safeRightPx = maxOf(navigation?.right ?: 0, cutout?.right ?: 0),
        hasInsets = insets != null
    )
}

@Composable
internal fun Modifier.hostStatusBarsPadding(): Modifier {
    val metrics = LocalHostWindowMetrics.current
    if (!metrics.hasInsets) return statusBarsPadding()
    return windowInsetsPadding(
        WindowInsets(metrics.safeLeftPx, metrics.statusBarTopPx, metrics.safeRightPx, 0)
    )
}

@Composable
internal fun Modifier.hostNavigationBarsPadding(): Modifier {
    val metrics = LocalHostWindowMetrics.current
    if (!metrics.hasInsets) return navigationBarsPadding()
    // 与 imePadding 共用 Insets 消费机制，避免键盘显示时重复叠加底部间距。
    return windowInsetsPadding(
        WindowInsets(metrics.safeLeftPx, 0, metrics.safeRightPx, metrics.navigationBarBottomPx)
    )
}
