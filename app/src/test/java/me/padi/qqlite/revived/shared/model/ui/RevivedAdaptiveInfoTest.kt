package me.padi.qqlite.revived.shared.model.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RevivedAdaptiveInfoTest {
    @Test
    fun widthClassBoundaries() {
        listOf(
            0f to RevivedWidthSizeClass.Compact,
            599.99f to RevivedWidthSizeClass.Compact,
            600f to RevivedWidthSizeClass.Medium,
            839.99f to RevivedWidthSizeClass.Medium,
            840f to RevivedWidthSizeClass.Expanded
        ).forEach { (width, expected) ->
            assertEquals(expected, RevivedAdaptiveInfo.create(width.dp, 1000.dp).widthClass)
        }
    }

    @Test
    fun tabletRuleAndOrientationRemainIndependentOfWidthClass() {
        assertFalse(RevivedAdaptiveInfo.create(599.dp, 1000.dp).isTablet)
        assertTrue(RevivedAdaptiveInfo.create(600.dp, 600.dp).isTablet)
        assertFalse(RevivedAdaptiveInfo.create(600.dp, 599.dp).isTablet)
        assertFalse(RevivedAdaptiveInfo.create(719.99f.dp, 360.dp).isTablet)
        assertTrue(RevivedAdaptiveInfo.create(720.dp, 360.dp).isTablet)
        assertTrue(RevivedAdaptiveInfo.create(640.dp, 360.dp).isLandscape)
        assertFalse(RevivedAdaptiveInfo.create(600.dp, 600.dp).isLandscape)
        assertFalse(RevivedAdaptiveInfo.create(360.dp, 640.dp).isLandscape)
        assertFalse(RevivedAdaptiveInfo.create(0.dp, 0.dp).isLandscape)
    }

    @Test
    fun compactPortraitPreservesBaseline() {
        val aio = AioWindowInfo.create(360.dp, 800.dp)
        assertEquals(
            listOf(58, 10, 172, 12, 12, 10, 292, 360, 10, 8, 112).map { it.dp },
            aio.dimensions()
        )
        assertEquals(5, aio.emojiSystemColumns)
        assertEquals(3, aio.emojiPackColumns)
        val qav = QavWindowInfo.create(360.dp, 800.dp)
        assertEquals(listOf(18, 20, 88, 42, 20, 58, 28, 8).map { it.dp }, qav.dimensions())
        assertEquals(24.sp, qav.peerNameSize)
        assertEquals(13.sp, qav.statusTextSize)
    }

    @Test
    fun largerWindowsExpandContentButKeepReadingWidthsBounded() {
        val compact = AioWindowInfo.create(360.dp, 800.dp)
        val medium = AioWindowInfo.create(600.dp, 1000.dp)
        val expanded = AioWindowInfo.create(840.dp, 1200.dp)
        assertTrue(medium.bubbleMaxWidth > compact.bubbleMaxWidth)
        assertTrue(expanded.bubbleMaxWidth > medium.bubbleMaxWidth)
        assertEquals(560.dp, AioWindowInfo.create(4000.dp, 2000.dp).bubbleMaxWidth)
        assertEquals(720.dp, AioWindowInfo.create(4000.dp, 2000.dp).emojiSheetMaxWidth)
        assertTrue(expanded.emojiSystemColumns > compact.emojiSystemColumns)
        assertTrue(expanded.emojiPackColumns > compact.emojiPackColumns)
        assertTrue(QavWindowInfo.create(840.dp, 1200.dp).voiceAvatarSize > 88.dp)
    }

    @Test
    fun shortLandscapeReducesVerticalChromeEvenWhenTabletRuleMatches() {
        val portrait = AioWindowInfo.create(360.dp, 800.dp)
        val landscape = AioWindowInfo.create(800.dp, 360.dp)
        assertEquals(48.dp, landscape.topBarHeight)
        assertTrue(landscape.inputMaxHeight < portrait.inputMaxHeight)
        assertTrue(landscape.inputVerticalPadding < portrait.inputVerticalPadding)
        val call = QavWindowInfo.create(800.dp, 360.dp)
        assertTrue(call.voiceAvatarSize < 88.dp)
        assertTrue(call.verticalPadding < 20.dp)
    }

    @Test
    fun tinyZeroAndInvalidDimensionsStaySafe() {
        listOf(0.dp, 0.1f.dp, 1.dp, (-1).dp, Dp.Unspecified, Float.POSITIVE_INFINITY.dp)
            .forEach { size ->
                val aio = AioWindowInfo.create(size, size)
                val qav = QavWindowInfo.create(size, size)
                (aio.dimensions() + qav.dimensions()).forEach {
                    assertTrue("Invalid dimension: $it", it.value.isFinite() && it >= 0.dp)
                }
                assertTrue(aio.inputMaxHeight >= 44.dp)
                assertTrue(aio.emojiSystemColumns >= 1)
                assertTrue(aio.emojiPackColumns >= 1)
                assertTrue(qav.peerNameSize.value >= 6f)
                assertTrue(qav.statusTextSize.value > 0f)
            }
        val zero = AioWindowInfo.create(0.dp, 0.dp)
        assertEquals(0.dp, zero.bubbleMaxWidth)
        assertEquals(0.dp, zero.emojiSheetMaxWidth)
        assertEquals(44.dp, zero.inputMaxHeight)
    }

    @Test
    fun bubbleEmojiAndControlWidthsFitAvailableSpace() {
        listOf(0f, 1f, 40f, 100f, 240f, 320f, 360f, 599.99f, 600f, 720f, 839.99f, 840f, 2400f)
            .forEach { width ->
                listOf(0f, 1f, 360f, 800f).forEach { height ->
                    val aio = AioWindowInfo.create(width.dp, height.dp)
                    val availableBubble = (width - 2f * aio.listHorizontalPadding.value - 40f)
                        .coerceAtLeast(0f)
                    assertTrue(aio.bubbleMaxWidth.value <= availableBubble)
                    assertTrue(aio.emojiSheetMaxWidth.value <= width)
                    assertTrue(2f * aio.inputHorizontalPadding.value <= aio.emojiSheetMaxWidth.value)
                    assertTrue((aio.emojiSystemColumns - 1) * 8f <= aio.emojiSheetMaxWidth.value)
                    assertTrue((aio.emojiPackColumns - 1) * 8f <= aio.emojiSheetMaxWidth.value)
                    val qav = QavWindowInfo.create(width.dp, height.dp)
                    val controlsWidth = 2f * qav.horizontalPadding.value +
                        3f * (qav.controlSpacing.value + qav.controlButtonSize.value)
                    assertTrue(controlsWidth <= width + 0.001f)
                    assertTrue(qav.controlIconSize <= qav.controlButtonSize)
                }
            }
    }

    private fun AioWindowInfo.dimensions(): List<Dp> = listOf(
        topBarHeight, topBarHorizontalPadding, titleMaxWidth, listHorizontalPadding,
        listVerticalPadding, listItemSpacing, bubbleMaxWidth, emojiSheetMaxWidth,
        inputHorizontalPadding, inputVerticalPadding, inputMaxHeight
    )

    private fun QavWindowInfo.dimensions(): List<Dp> = listOf(
        horizontalPadding, verticalPadding, voiceAvatarSize, videoIdentityAvatarSize,
        controlSpacing, controlButtonSize, controlIconSize, controlLabelTopPadding
    )
}
