package me.padi.qqlite.revived.compose.component.glass

import me.padi.qqlite.revived.compose.theme.RevivedEffectLevel
import me.padi.qqlite.revived.compose.theme.RevivedThemePreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlassEffectParametersTest {
    @Test
    fun bothOffDisableEffects() {
        val effects = GlassEffectParameters.from(RevivedEffectLevel.Off, RevivedEffectLevel.Off)
        assertFalse(effects.hasEffects)
        assertFalse(effects.hasRefraction)
        assertEquals(0f, effects.blurRadiusDp, 0f)
    }

    @Test
    fun refractionAndFrostAreIndependent() {
        for (glass in RevivedEffectLevel.entries) {
            for (frost in RevivedEffectLevel.entries) {
                val effects = GlassEffectParameters.from(glass, frost)
                assertEquals(
                    GlassEffectParameters.from(glass, RevivedEffectLevel.Off).refractionScale,
                    effects.refractionScale, 0f
                )
                assertEquals(
                    GlassEffectParameters.from(RevivedEffectLevel.Off, frost).blurRadiusDp,
                    effects.blurRadiusDp, 0f
                )
                assertEquals(glass != RevivedEffectLevel.Off, effects.hasRefraction)
                assertEquals(glass != RevivedEffectLevel.Off || frost != RevivedEffectLevel.Off, effects.hasEffects)
            }
        }
    }

    @Test
    fun masterSwitchOverridesLevelsWithoutLosingSelection() {
        val preference = RevivedThemePreference(
            enableLiquidGlass = false,
            glassLevel = RevivedEffectLevel.High,
            frostLevel = RevivedEffectLevel.Medium
        )
        assertFalse(preference.hasGlassEffects)
        val enabled = preference.copy(enableLiquidGlass = true)
        assertTrue(enabled.hasGlassEffects)
        assertEquals(preference.glassLevel, enabled.glassLevel)
        assertEquals(preference.frostLevel, enabled.frostLevel)
        assertFalse(enabled.copy(glassLevel = RevivedEffectLevel.Off, frostLevel = RevivedEffectLevel.Off).hasGlassEffects)
    }

    @Test
    fun levelsAreMonotonicAndBounded() {
        val levels = RevivedEffectLevel.entries.map { GlassEffectParameters.from(it, it) }
        levels.zipWithNext().forEach { (a, b) ->
            assertTrue(a.refractionScale < b.refractionScale)
            assertTrue(a.blurRadiusDp < b.blurRadiusDp)
        }
        levels.forEach {
            assertTrue(it.refractionScale in 0f..1f)
            assertTrue(it.blurRadiusDp in 0f..36f)
        }
        assertEquals(listOf(0f, 8f, 20f, 36f), levels.map { it.blurRadiusDp })
    }
}
