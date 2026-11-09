package me.padi.qqlite.revived.compose.component.bottombar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.padi.qqlite.revived.compose.component.glass.revivedGlass
import me.padi.qqlite.revived.compose.theme.currentRevivedThemePreference
import me.padi.qqlite.revived.utils.hostNavigationBarsPadding
import me.padi.qqlite.revived.utils.hostStatusBarsPadding
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.GlassShape
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal data class BottomBarItemSpec(val key: String, val label: String, val icon: ImageVector)

@Composable
internal fun BottomBar(
    items: List<BottomBarItemSpec>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    surfaceColor: Color,
    backdrop: Backdrop,
    pageProgress: (() -> Float)? = null
) {
    if (items.isEmpty()) return
    val enabled = currentRevivedThemePreference().hasGlassEffects
    val fallbackProgress by animateFloatAsState(selectedIndex.toFloat(), tween(240), label = "nav-position")
    val indicatorColor = MiuixTheme.colorScheme.primary.copy(alpha = 0.14f)
    Box(
        modifier = Modifier.fillMaxWidth().hostNavigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth()
                .revivedGlass(backdrop, GlassShape(32.dp), surfaceColor, enabled, floating = true)
                .padding(5.dp)
        ) {
            // 只在绘制阶段读取翻页进度，滑动不因选中块逐帧重组整个导航。
            Canvas(Modifier.matchParentSize()) {
                val gap = 4.dp.toPx()
                val width = ((size.width - gap * (items.size - 1)) / items.size).coerceAtLeast(0f)
                val progress = (pageProgress?.invoke() ?: fallbackProgress).coerceIn(0f, items.lastIndex.toFloat())
                val position = if (layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl)
                    items.lastIndex - progress else progress
                drawRoundRect(
                    color = indicatorColor,
                    topLeft = Offset(position * (width + gap), 0f),
                    size = Size(width, size.height),
                    cornerRadius = CornerRadius(27.dp.toPx())
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    key(item.key) {
                        NavigationItem(item, index == selectedIndex, { onSelect(index) }, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
internal fun SideRail(
    items: List<BottomBarItemSpec>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    surfaceColor: Color,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    val enabled = currentRevivedThemePreference().hasGlassEffects
    Column(
        modifier = modifier.fillMaxHeight().hostStatusBarsPadding().hostNavigationBarsPadding().padding(8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .revivedGlass(backdrop, GlassShape(28.dp), surfaceColor, enabled, floating = true)
                .padding(horizontal = 5.dp, vertical = 8.dp).selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items.forEachIndexed { index, item ->
                key(item.key) {
                    val color by animateColorAsState(
                        if (index == selectedIndex) MiuixTheme.colorScheme.primary.copy(alpha = 0.14f) else Color.Transparent,
                        tween(220), label = "rail-selection"
                    )
                    Box(Modifier.fillMaxWidth()) {
                        Canvas(Modifier.matchParentSize()) {
                            drawRoundRect(color, cornerRadius = CornerRadius(24.dp.toPx()))
                        }
                        NavigationItem(item, index == selectedIndex, { onSelect(index) }, Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationItem(item: BottomBarItemSpec, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val colors = MiuixTheme.colorScheme
    val foreground by animateColorAsState(
        if (selected) colors.primary else colors.onSurfaceVariantSummary,
        tween(180), label = "nav-color"
    )
    val iconScale by animateFloatAsState(if (selected) 1.08f else 1f, tween(180), label = "nav-icon")
    Column(
        modifier = modifier.heightIn(min = 58.dp).clip(RoundedCornerShape(27.dp))
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(item.icon, contentDescription = null, tint = foreground,
            modifier = Modifier.size(24.dp).graphicsLayer { scaleX = iconScale; scaleY = iconScale })
        Text(item.label, color = foreground, fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
