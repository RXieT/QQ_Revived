package me.padi.qqlite.revived.compose.component.glass

import me.padi.qqlite.revived.compose.theme.RevivedEffectLevel

/** 两个等级分别映射到光学强度和模糊半径，不以整体透明度冒充折射等级。 */
internal data class GlassEffectParameters(
    val refractionScale: Float,
    val blurRadiusDp: Float
) {
    val hasRefraction: Boolean get() = refractionScale > 0f
    val hasEffects: Boolean get() = hasRefraction || blurRadiusDp > 0f

    companion object {
        fun from(glass: RevivedEffectLevel, frost: RevivedEffectLevel): GlassEffectParameters {
            val refraction = when (glass) {
                RevivedEffectLevel.Off -> 0f
                RevivedEffectLevel.Low -> 0.35f
                RevivedEffectLevel.Medium -> 0.65f
                RevivedEffectLevel.High -> 1f
            }
            val radius = when (frost) {
                RevivedEffectLevel.Off -> 0f
                RevivedEffectLevel.Low -> 8f
                RevivedEffectLevel.Medium -> 20f
                RevivedEffectLevel.High -> 36f
            }
            return GlassEffectParameters(refraction, radius)
        }
    }
}
