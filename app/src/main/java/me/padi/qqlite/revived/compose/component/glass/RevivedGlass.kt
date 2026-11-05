package me.padi.qqlite.revived.compose.component.glass

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
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
    if (!enabled || !LocalView.current.isHardwareAccelerated) {
        return background(surfaceColor, shape)
    }
    val dark = surfaceColor.luminance() < 0.5f
    return then(if (floating) Modifier.glassShadow(shape) else Modifier)
        .glass(
            backdrop = backdrop,
            shape = shape,
            style = GlassDefaults.style(dark),
            material = if (floating) GlassMaterials.puredThinGlass(dark)
                else GlassMaterials.actionBarMask(dark),
            stroke = if (floating) GlassStrokes.forTheme(dark) else null,
            shading = false
        )
}
