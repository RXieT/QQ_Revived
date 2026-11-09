package me.padi.qqlite.revived.utils

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class RecentImagesTest {
    @Test
    fun extensionUsesOnlyDisplayNameWhitelist() {
        assertEquals("png", safeImageExtension("照片.PNG"))
        assertEquals("jpeg", safeImageExtension("a.b.JPEG"))
        assertEquals("heic", safeImageExtension("照片.heic"))
        listOf("", "photo", "photo.", "photo.exe", "photo.jpg/../../file", "photo.png?x=1").forEach {
            assertEquals("jpg", safeImageExtension(it))
        }
    }

    @Test
    fun actualByteLimitAllowsBoundaryButRejectsOversize() {
        val exact = ByteArrayOutputStream()
        assertEquals(4L, copyImageBytes(ByteArrayInputStream(byteArrayOf(1, 2, 3, 4)), exact, 4))
        assertEquals(4, exact.size())
        val output = ByteArrayOutputStream()
        try {
            copyImageBytes(ByteArrayInputStream(ByteArray(5)), output, 4)
            fail("oversized image accepted")
        } catch (_: IOException) {
            assertTrue(output.size() <= 4)
        }
        assertEquals(30L * 1024 * 1024, MAX_IMAGE_BYTES)
        assertEquals(60, RECENT_IMAGE_LIMIT)
    }

    @Test
    fun rejectsEmptyImage() {
        try {
            copyImageBytes(ByteArrayInputStream(byteArrayOf()), ByteArrayOutputStream())
            fail("empty image accepted")
        } catch (_: IOException) {
            // 空文件不能交给宿主发送。
        }
    }

    @Test
    fun expiryKeepsRecentBoundaryUnknownAndFutureFiles() {
        val now = IMAGE_CACHE_MAX_AGE_MS * 3
        assertFalse(isExpiredImageCache(now, now))
        assertFalse(isExpiredImageCache(0, now))
        assertFalse(isExpiredImageCache(now + 1, now))
        assertFalse(isExpiredImageCache(now - IMAGE_CACHE_MAX_AGE_MS, now))
        assertTrue(isExpiredImageCache(now - IMAGE_CACHE_MAX_AGE_MS - 1, now))
    }

    @Test
    fun cleanupStaysInsideOwnDirectoryAndDoesNotRecurseOrFollowLinks() {
        val root = Files.createTempDirectory("recent-images-test").toFile()
        try {
            val own = File(root, "revived-images").apply { mkdirs() }
            val now = System.currentTimeMillis()
            val oldTime = now - IMAGE_CACHE_MAX_AGE_MS - 10_000
            val old = File(own, "old.jpg").apply { writeText("old"); setLastModified(oldTime) }
            val fresh = File(own, "new.jpg").apply { writeText("new") }
            val foreign = File(root, "qq-other.jpg").apply { writeText("qq"); setLastModified(oldTime) }
            val nested = File(own, "nested").apply { mkdirs() }
            val nestedImage = File(nested, "old.jpg").apply { writeText("nested"); setLastModified(oldTime) }
            val link = File(own, "linked.jpg")
            // Windows 等环境可能禁用符号链接；常规目录隔离断言仍须执行。
            val linked = runCatching { Files.createSymbolicLink(link.toPath(), foreign.toPath()) }.isSuccess
            cleanExpiredImageCache(own, now)
            assertFalse(old.exists())
            assertTrue(fresh.exists())
            assertTrue(foreign.exists())
            assertTrue(nestedImage.exists())
            if (linked) assertTrue(Files.isSymbolicLink(link.toPath()))
        } finally {
            root.deleteRecursively()
        }
    }
}
