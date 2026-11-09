package me.padi.qqlite.revived.compose.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class FontScaleTest {
    @Test
    fun keepsSupportedValuesAndBounds() {
        listOf(0.85f, 1f, 1.1f, 1.25f, 1.5f).forEach { value ->
            assertEquals(value, normalizeFontScale(value), 0f)
        }
    }

    @Test
    fun clampsFiniteValues() {
        listOf(-Float.MAX_VALUE, -1f, 0f, 0.84f).forEach { value ->
            assertEquals(0.85f, normalizeFontScale(value), 0f)
        }
        listOf(1.51f, 2f, Float.MAX_VALUE).forEach { value ->
            assertEquals(1.5f, normalizeFontScale(value), 0f)
        }
    }

    @Test
    fun invalidValuesFallBackToDefault() {
        listOf(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY).forEach { value ->
            assertEquals(1.1f, normalizeFontScale(value), 0f)
        }
    }

    @Test
    fun multipliesSystemFontScaleWithoutClampingSystemPreference() {
        assertEquals(1.1f, effectiveFontScale(1f, 1.1f), 0.00001f)
        assertEquals(1.625f, effectiveFontScale(1.3f, 1.25f), 0.00001f)
        assertEquals(3f, effectiveFontScale(2f, 1.5f), 0.00001f)
        assertEquals(0.68f, effectiveFontScale(0.8f, 0.85f), 0.00001f)
    }

    @Test
    fun followSystemPreservesSystemFontScale() {
        listOf(0.85f, 1f, 1.3f, 2f).forEach { systemScale ->
            assertEquals(systemScale, effectiveFontScale(systemScale, 1f), 0f)
        }
    }

    @Test
    fun normalizesOnlyModuleScaleBeforeMultiplication() {
        assertEquals(2.2f, effectiveFontScale(2f, Float.NaN), 0.00001f)
        assertEquals(2.2f, effectiveFontScale(2f, Float.POSITIVE_INFINITY), 0.00001f)
        assertEquals(2.2f, effectiveFontScale(2f, Float.NEGATIVE_INFINITY), 0.00001f)
        assertEquals(1.7f, effectiveFontScale(2f, 0f), 0.00001f)
        assertEquals(3f, effectiveFontScale(2f, 5f), 0.00001f)
    }
}
