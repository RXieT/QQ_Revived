package me.padi.qqlite.revived.utils

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

internal const val RECENT_IMAGE_LIMIT = 60
internal const val IMAGE_SELECTION_LIMIT = 6
internal const val MAX_IMAGE_BYTES = 30L * 1024 * 1024
internal const val IMAGE_CACHE_MAX_AGE_MS = 24L * 60 * 60 * 1000

internal data class RecentImage(val uri: String, val displayName: String, val sizeBytes: Long)

internal sealed interface RecentImagesResult {
    data class Available(val images: List<RecentImage>, val limitedAccess: Boolean) : RecentImagesResult
    data class AccessRequired(val message: String) : RecentImagesResult
    data class Failed(val message: String) : RecentImagesResult
}

/** 只读取宿主现有授权；模块没有也不会为宿主申请图库权限。 */
internal suspend fun loadRecentImages(context: Context): RecentImagesResult = withContext(Dispatchers.IO) {
    fun granted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    val fullAccess = granted(Manifest.permission.READ_EXTERNAL_STORAGE) ||
        (Build.VERSION.SDK_INT >= 33 && granted(Manifest.permission.READ_MEDIA_IMAGES))
    val partialAccess = Build.VERSION.SDK_INT >= 34 &&
        granted(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
    if (!fullAccess && !partialAccess) {
        return@withContext RecentImagesResult.AccessRequired(
            "QQ 尚未获得图库读取授权。可用“完整相册”选择图片；此面板不会申请额外权限。"
        )
    }
    try {
        val images = mutableListOf<RecentImage>()
        val args = Bundle().apply {
            putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media._ID))
            putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_DESCENDING)
            putInt(ContentResolver.QUERY_ARG_LIMIT, RECENT_IMAGE_LIMIT)
        }
        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.SIZE),
            args,
            null
        ) ?: return@withContext RecentImagesResult.Failed("图库暂时不可用，请重试或打开完整相册。")
        cursor.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            // 即使 provider 忽略 QUERY_ARG_LIMIT，也不向 UI 返回超过 60 项。
            while (images.size < RECENT_IMAGE_LIMIT && it.moveToNext()) {
                coroutineContext.ensureActive()
                images += RecentImage(
                    uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, it.getLong(idColumn)).toString(),
                    displayName = it.getString(nameColumn).orEmpty(),
                    sizeBytes = it.getLong(sizeColumn)
                )
            }
        }
        RecentImagesResult.Available(images, limitedAccess = !fullAccess)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: SecurityException) {
        RecentImagesResult.AccessRequired("QQ 的图库授权不足或已撤销，请使用完整相册选择图片。")
    } catch (_: Exception) {
        RecentImagesResult.Failed("读取最近图片失败，请重试或使用完整相册。")
    }
}

internal fun safeImageExtension(displayName: String): String {
    val extension = displayName.substringAfterLast('.', "").lowercase(Locale.ROOT)
    return extension.takeIf { it in setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif", "avif") } ?: "jpg"
}

/** 按实际读取字节限制，不能相信 MediaStore 提供的 SIZE。 */
internal fun copyImageBytes(input: InputStream, output: OutputStream, maxBytes: Long = MAX_IMAGE_BYTES): Long {
    require(maxBytes >= 0)
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var total = 0L
    while (true) {
        val read = input.read(buffer)
        if (read < 0) break
        if (read == 0) continue
        if (read.toLong() > maxBytes - total) throw IOException("单张图片不能超过 30MB")
        output.write(buffer, 0, read)
        total += read
    }
    if (total == 0L) throw IOException("图片文件为空")
    return total
}

internal fun isExpiredImageCache(lastModified: Long, now: Long): Boolean =
    lastModified > 0L && now > lastModified && now - lastModified > IMAGE_CACHE_MAX_AGE_MS

/** 仅清理本功能目录的普通旧文件，不递归、不跟随符号链接，也不碰 QQ 其他缓存。 */
internal fun cleanExpiredImageCache(directory: File, now: Long = System.currentTimeMillis()) {
    val canonicalDirectory = directory.canonicalFile
    val canonicalParent = directory.absoluteFile.parentFile?.canonicalFile ?: return
    if (canonicalDirectory != File(canonicalParent, directory.name)) return
    directory.listFiles()?.forEach { file ->
        if (file.isFile && file.canonicalFile == File(canonicalDirectory, file.name) &&
            isExpiredImageCache(file.lastModified(), now)
        ) {
            file.delete()
        }
    }
}

/** 返回宿主可读的本地文件路径，发送后留存 24h 供宿主异步读取。 */
internal suspend fun copyRecentImageToCache(context: Context, image: RecentImage): Result<String> = withContext(Dispatchers.IO) {
    var target: File? = null
    try {
        coroutineContext.ensureActive()
        if (image.sizeBytes > MAX_IMAGE_BYTES) throw IOException("单张图片不能超过 30MB")
        val hostContext = context.applicationContext ?: context
        val directory = File(hostContext.cacheDir, "revived-images")
        if (!directory.isDirectory && !directory.mkdirs()) throw IOException("无法创建图片缓存目录")
        // 目录若被替换成链接，不写入/清理链接指向的其他缓存。
        if (directory.canonicalFile != File(hostContext.cacheDir.canonicalFile, "revived-images")) {
            throw IOException("图片缓存目录异常")
        }
        cleanExpiredImageCache(directory)
        val file = File(directory, "image-${UUID.randomUUID()}.${safeImageExtension(image.displayName)}")
        target = file
        val uri = android.net.Uri.parse(image.uri)
        if (uri.scheme != "content") throw IOException("不是有效的图库图片")
        val input = context.contentResolver.openInputStream(uri) ?: throw IOException("无法读取图片")
        input.use { source -> file.outputStream().use { output -> copyImageBytes(source, output) } }
        coroutineContext.ensureActive()
        if (!file.canRead()) throw IOException("图片缓存不可读")
        Result.success(file.absolutePath)
    } catch (cancelled: CancellationException) {
        target?.delete()
        throw cancelled
    } catch (_: SecurityException) {
        target?.delete()
        Result.failure(IOException("图片读取授权已失效，请重新选择或使用完整相册"))
    } catch (error: Exception) {
        target?.delete()
        Result.failure(IOException(error.message ?: "复制图片失败", error))
    }
}
