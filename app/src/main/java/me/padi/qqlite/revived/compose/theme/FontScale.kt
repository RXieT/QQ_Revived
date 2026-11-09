package me.padi.qqlite.revived.compose.theme

const val DEFAULT_FONT_SCALE = 1.1f
const val MIN_FONT_SCALE = 0.85f
const val MAX_FONT_SCALE = 1.5f

/** 仅规范模块倍率，保留系统字号；纯函数可在 JVM 上测试。 */
fun normalizeFontScale(value: Float): Float =
    if (value.isFinite()) value.coerceIn(MIN_FONT_SCALE, MAX_FONT_SCALE) else DEFAULT_FONT_SCALE

fun effectiveFontScale(systemFontScale: Float, moduleFontScale: Float): Float =
    systemFontScale * normalizeFontScale(moduleFontScale)
