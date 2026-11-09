package me.padi.qqlite.revived.compose.component.glass

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.padi.qqlite.revived.compose.theme.currentRevivedThemePreference
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.glass.GlassShape
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 静态高对比背景便于比较模糊等级，不启动无限动画、不采样整个设置页。 */
@Composable
internal fun GlassSettingsPreview() {
    val backdrop = rememberLayerBackdrop()
    val preference = currentRevivedThemePreference()
    Column {
        Box(Modifier.fillMaxWidth().height(128.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize().layerBackdrop(backdrop)) {
                val stripe = 14.dp.toPx()
                var x = 0f
                var index = 0
                while (x < size.width) {
                    drawRect(
                        color = if (index % 2 == 0) Color(0xFF7599CC) else Color(0xFFE2B783),
                        topLeft = Offset(x, 0f), size = Size(stripe, size.height)
                    )
                    x += stripe
                    index++
                }
                drawCircle(Color(0xFFE46C86), radius = 30.dp.toPx(), center = Offset(size.width * 0.35f, size.height / 2))
            }
            Box(
                Modifier.size(200.dp, 88.dp).revivedGlass(
                    backdrop, GlassShape(24.dp), MiuixTheme.colorScheme.surface,
                    enabled = preference.hasGlassEffects, floating = true
                ), contentAlignment = Alignment.Center
            ) {
                Text("玻璃预览 Aa", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Text(
            if (LocalView.current.isHardwareAccelerated)
                "调整等级时比较条纹清晰度和边缘折射；纯色背景上的差异可能较小。"
            else "当前窗口未启用硬件加速，显示普通背景，玻璃等级不会影响效果。",
            modifier = Modifier.padding(top = 8.dp), fontSize = 13.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
        )
    }
}
