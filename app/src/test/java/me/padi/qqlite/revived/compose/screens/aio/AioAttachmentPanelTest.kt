package me.padi.qqlite.revived.compose.screens.aio

import me.padi.qqlite.revived.utils.RecentImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AioAttachmentPanelTest {
    @Test
    fun regularPanelHeightIsBetween240And300Dp() {
        assertEquals(240f, aioAttachmentPanelHeight(700f), 0f)
        assertEquals(272f, aioAttachmentPanelHeight(800f), 0.001f)
        assertEquals(300f, aioAttachmentPanelHeight(1200f), 0f)
    }

    @Test
    fun shortLandscapeNeverUsesMoreThanFortyPercent() {
        listOf(200f, 300f, 400f, 500f, 599f).forEach {
            assertTrue(aioAttachmentPanelHeight(it) <= it * 0.4f)
        }
        assertEquals(160f, aioAttachmentPanelHeight(400f), 0f)
        listOf(0f, -1f, Float.NaN, Float.POSITIVE_INFINITY).forEach {
            assertEquals(0f, aioAttachmentPanelHeight(it), 0f)
        }
    }

    @Test
    fun selectionHasSixItemLimitAndStableOrder() {
        val images = (1..7).map { RecentImage("content://images/$it", "$it.jpg", 1) }
        var selected = emptyList<RecentImage>()
        images.forEach { selected = toggleRecentImageSelection(selected, it) }
        assertEquals(images.take(6), selected)
        selected = toggleRecentImageSelection(selected, images[1])
        selected = toggleRecentImageSelection(selected, images[6])
        assertEquals(listOf(images[0], images[2], images[3], images[4], images[5], images[6]), selected)
    }

    @Test
    fun selectionIdentityUsesUriNotMutableMetadata() {
        val original = RecentImage("content://images/1", "before.jpg", 1)
        val refreshed = original.copy(displayName = "after.jpg", sizeBytes = 2)
        assertTrue(toggleRecentImageSelection(listOf(original), refreshed).isEmpty())
    }
}
