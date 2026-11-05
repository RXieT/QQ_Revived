package me.padi.qqlite.revived.compose.component.bottombar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
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
    backdrop: Backdrop
) {
    if (items.isEmpty()) return
    val enabled = currentRevivedThemePreference().enableLiquidGlass
    Row(
        modifier = Modifier
            .hostNavigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .widthIn(max = 560.dp)
            .fillMaxWidth()
            .revivedGlass(backdrop, GlassShape(30.dp), surfaceColor, enabled, floating = true)
            .padding(6.dp)
            .selectableGroup(),
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
    val enabled = currentRevivedThemePreference().enableLiquidGlass
    Column(
        modifier = modifier.fillMaxHeight()
            .hostStatusBarsPadding()
            .hostNavigationBarsPadding()
            .padding(8.dp)
            .revivedGlass(backdrop, GlassShape(28.dp), surfaceColor, enabled, floating = true)
            .padding(horizontal = 6.dp, vertical = 12.dp)
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items.forEachIndexed { index, item ->
            key(item.key) {
                NavigationItem(item, index == selectedIndex, { onSelect(index) }, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun NavigationItem(
    item: BottomBarItemSpec,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val colors = MiuixTheme.colorScheme
    val foreground = if (selected) colors.primary else colors.onSurfaceVariantSummary
    Column(
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) colors.primary.copy(alpha = 0.13f) else Color.Transparent)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(item.icon, contentDescription = null, tint = foreground, modifier = Modifier.size(23.dp))
        Text(item.label, color = foreground, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
