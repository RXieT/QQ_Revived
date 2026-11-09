package me.padi.qqlite.revived.compose.component.glass

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import me.padi.qqlite.revived.compose.theme.currentRevivedThemePreference
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.GlassDefaults
import top.yukonga.miuix.kmp.glass.GlassMaterials
import top.yukonga.miuix.kmp.glass.GlassShape
import top.yukonga.miuix.kmp.glass.GlassStrokes
import top.yukonga.miuix.kmp.glass.glass
import top.yukonga.miuix.kmp.glass.glassShadow

/** 独立集成 Apache Miuix 材质，不使用参考应用的自定义导航或着色器。 */
@Composable
internal fun Modifier.revivedGlass(
    backdrop: Backdrop,
    shape: GlassShape,
    surfaceColor: Color,
    enabled: Boolean,
    floating: Boolean = false
): Modifier {
    val preference = currentRevivedThemePreference()
    val effects = remember(preference.glassLevel, preference.frostLevel) {
        GlassEffectParameters.from(preference.glassLevel, preference.frostLevel)
    }
    if (!enabled || !preference.enableLiquidGlass || !effects.hasEffects ||
        !LocalView.current.isHardwareAccelerated
    ) {
        return background(surfaceColor, shape)
    }
    val dark = surfaceColor.luminance() < 0.5f
    val style = remember(dark, effects) {
        val base = GlassDefaults.style(dark)
        val strength = effects.refractionScale
        val blurPx = effects.blurRadiusDp * GlassDefaults.SourceDensity
        base.copy(
            edge = base.edge.copy(
                thickness = base.edge.thickness * strength,
                reflectOffset = base.edge.reflectOffset * strength
            ),
            reflect = base.reflect.copy(
                strength = base.reflect.strength * strength,
                lighten = base.reflect.lighten * strength
            ),
            light = base.light.copy(
                intensity = base.light.intensity * strength,
                oppositeIntensity = base.light.oppositeIntensity * strength
            ),
            // 包括着色器的宽域采样，磨砂关闭时不残留另一层模糊。
            blur = base.blur.copy(small = blurPx, big = blurPx)
        )
    }
    val material = remember(dark, floating, effects.blurRadiusDp) {
        val base = if (floating) GlassMaterials.puredThinGlass(dark)
            else GlassMaterials.actionBarMask(dark)
        base.copy(blurRadius = effects.blurRadiusDp.dp)
    }
    return then(if (floating) Modifier.glassShadow(shape) else Modifier)
        .glass(
            backdrop = backdrop,
            shape = shape,
            style = style,
            material = material,
            // 小面积导航不叠加全分辨率噪声 pass，减少移动时的 GPU 合成开销。
            noiseCoefficient = 0f,
            stroke = if (floating && effects.hasRefraction) GlassStrokes.forTheme(dark) else null,
            shading = effects.hasRefraction
        )
}
