package me.padi.qqlite.revived.compose.screens.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/** 列表滚动内容的安全留白；不能用列表外 Spacer，否则玻璃永远采样到纯色。 */
internal val LocalHomeContentInsets = staticCompositionLocalOf { PaddingValues() }

@Composable
internal fun homeListContentPadding(horizontal: androidx.compose.ui.unit.Dp = 0.dp): PaddingValues {
    val insets = LocalHomeContentInsets.current
    return PaddingValues(
        start = horizontal,
        top = insets.calculateTopPadding() + 16.dp,
        end = horizontal,
        bottom = insets.calculateBottomPadding() + 20.dp
    )
}
