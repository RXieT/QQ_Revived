package me.padi.qqlite.revived.compose.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.padi.qqlite.revived.compose.theme.RevivedEffectLevel
import me.padi.qqlite.revived.compose.theme.RevivedThemeMode
import me.padi.qqlite.revived.compose.theme.RevivedThemePreference
import me.padi.qqlite.revived.compose.theme.RevivedThemePreset
import me.padi.qqlite.revived.compose.theme.RevivedUiMode
import me.padi.qqlite.revived.shared.model.module.MainUiState
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.RadioButton
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet

@Composable
fun ModuleSettingsScreen(
    uiState: MainUiState,
    themePreference: RevivedThemePreference,
    onRequestScopeClick: () -> Unit,
    onReloadClick: () -> Unit,
    onUiModeChange: (RevivedUiMode) -> Unit,
    onThemeModeChange: (RevivedThemeMode) -> Unit,
    onThemePresetChange: (RevivedThemePreset) -> Unit,
    onLiquidGlassChange: (Boolean) -> Unit,
    onGlassLevelChange: (RevivedEffectLevel) -> Unit,
    onFrostLevelChange: (RevivedEffectLevel) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .background(MiuixTheme.colorScheme.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("QQ Revived", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        SettingsSection("框架信息") {
            listOf(
                uiState.binderText, uiState.apiText, uiState.frameworkText,
                uiState.frameworkVersionText, uiState.frameworkVersionCodeText,
                uiState.frameworkPropertiesText, uiState.processText,
            ).filter { it.isNotBlank() }.forEach { Text(it) }
        }
        SettingsSection("作用域") {
            Text(uiState.scopeText.ifBlank { "尚未获取作用域" })
            Text("目标应用：QQ（com.tencent.qqlite）")
            TextButton(
                text = "请求作用域授权",
                onClick = onRequestScopeClick,
                // MainUiState 尚未提供独立的服务连接字段。
                enabled = uiState.binderText == "Binder acquired",
                modifier = Modifier.fillMaxWidth(),
            )
            Text("需要 Xposed 框架服务连接后才能请求授权。")
        }
        SettingsSection("模块更新") {
            Text("热重载已禁用。更新模块后，请强行停止 QQ 再重新打开。")
            TextButton(
                text = "热重载（已禁用）",
                onClick = onReloadClick,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Text("此处设置仅影响模块管理页，不影响 QQ")
        ThemeSettings(
            themePreference, onUiModeChange, onThemeModeChange,
            onThemePresetChange, onLiquidGlassChange,
            onGlassLevelChange, onFrostLevelChange,
        )
    }
}

@Composable
fun ModuleThemeSettingsSheet(
    show: Boolean,
    themePreference: RevivedThemePreference,
    onDismissRequest: () -> Unit,
    onUiModeChange: (RevivedUiMode) -> Unit,
    onThemeModeChange: (RevivedThemeMode) -> Unit,
    onThemePresetChange: (RevivedThemePreset) -> Unit,
    onLiquidGlassChange: (Boolean) -> Unit,
    onGlassLevelChange: (RevivedEffectLevel) -> Unit,
    onFrostLevelChange: (RevivedEffectLevel) -> Unit,
) {
    WindowBottomSheet(
        show = show,
        title = "模块主题设置",
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("此处设置仅影响当前 QQ 内的模块界面，不影响模块管理页")
            ThemeSettings(
                themePreference, onUiModeChange, onThemeModeChange,
                onThemePresetChange, onLiquidGlassChange,
                onGlassLevelChange, onFrostLevelChange,
            )
            TextButton(
                text = "完成",
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ThemeSettings(
    preference: RevivedThemePreference,
    onUiModeChange: (RevivedUiMode) -> Unit,
    onThemeModeChange: (RevivedThemeMode) -> Unit,
    onThemePresetChange: (RevivedThemePreset) -> Unit,
    onLiquidGlassChange: (Boolean) -> Unit,
    onGlassLevelChange: (RevivedEffectLevel) -> Unit,
    onFrostLevelChange: (RevivedEffectLevel) -> Unit,
) {
    SettingsSection("界面风格") {
        ChoiceGroup(
            choices = listOf(RevivedUiMode.Miuix to "Miuix", RevivedUiMode.Material to "Material"),
            selected = preference.uiMode,
            onSelect = onUiModeChange,
        )
        Text("应用于支持风格切换的模块界面；设置页使用 Miuix 控件。")
    }
    SettingsSection("深浅色") {
        ChoiceGroup(
            choices = listOf(
                RevivedThemeMode.System to "跟随系统",
                RevivedThemeMode.Light to "浅色",
                RevivedThemeMode.Dark to "深色",
            ),
            selected = preference.mode,
            onSelect = onThemeModeChange,
        )
    }
    SettingsSection("强调色") {
        ChoiceGroup(
            choices = listOf(
                RevivedThemePreset.Blue to "蓝色",
                RevivedThemePreset.Green to "绿色",
                RevivedThemePreset.Purple to "紫色",
            ),
            selected = preference.preset,
            onSelect = onThemePresetChange,
        )
    }
    SettingsSection("玻璃效果") {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                .toggleable(
                    value = preference.enableLiquidGlass,
                    role = Role.Switch,
                    onValueChange = onLiquidGlassChange,
                ).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("液态玻璃", modifier = Modifier.weight(1f))
            Switch(checked = preference.enableLiquidGlass, onCheckedChange = null)
        }
        Text("控制支持玻璃效果的模块组件；关闭后使用普通背景，保留已选等级。")
        val effectLevels = listOf(
            RevivedEffectLevel.Off to "关闭",
            RevivedEffectLevel.Low to "低",
            RevivedEffectLevel.Medium to "中",
            RevivedEffectLevel.High to "高",
        )
        Text("玻璃折射等级", fontWeight = FontWeight.SemiBold)
        ChoiceGroup(
            choices = effectLevels,
            selected = preference.glassLevel,
            onSelect = onGlassLevelChange,
            enabled = preference.enableLiquidGlass,
        )
        Text("磨砂模糊等级", fontWeight = FontWeight.SemiBold)
        ChoiceGroup(
            choices = effectLevels,
            selected = preference.frostLevel,
            onSelect = onFrostLevelChange,
            enabled = preference.enableLiquidGlass,
        )
        Text("玻璃控制折射与高光，磨砂控制背景模糊；两种等级可独立组合。")
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun <T> ChoiceGroup(
    choices: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    enabled: Boolean = true,
) {
    Column(modifier = Modifier.fillMaxWidth().selectableGroup()) {
        choices.forEach { (value, label) ->
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    .selectable(
                        selected = selected == value,
                        enabled = enabled,
                        role = Role.RadioButton,
                        onClick = { onSelect(value) },
                    ).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(label, modifier = Modifier.weight(1f).alpha(if (enabled) 1f else 0.38f))
                RadioButton(selected = selected == value, onClick = null)
            }
        }
    }
}
