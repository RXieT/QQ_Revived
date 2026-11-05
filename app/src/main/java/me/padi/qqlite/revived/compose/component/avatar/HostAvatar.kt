package me.padi.qqlite.revived.compose.component.avatar

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.request.ImageRequest
import me.padi.qqlite.revived.compose.screens.home.HomeUiController
import me.padi.qqlite.revived.shared.model.home.AvatarSpec
import me.padi.qqlite.revived.shared.model.home.normalizedImageValue
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.io.File

@Composable
internal fun HostAvatar(
    controller: HomeUiController,
    spec: AvatarSpec?,
    fallback: String,
    sizeDp: Int = 44
) {
    val imageUrl = spec?.imageUrl?.normalizedImageValue()?.takeIf {
        it.startsWith("http://", ignoreCase = true) ||
            it.startsWith("https://", ignoreCase = true) ||
            it.startsWith("/") ||
            it.startsWith("file://", ignoreCase = true) ||
            it.startsWith("content://", ignoreCase = true)
    }
    if (imageUrl != null) {
        UrlAvatar(imageUrl = imageUrl, fallback = fallback, sizeDp = sizeDp)
    } else if (spec != null && (spec.uid.isNotBlank() || spec.uin > 0L)) {
        AndroidView(
            factory = { context -> controller.createAvatarView(context, spec) },
            update = { view ->
                (view as? ImageView)?.scaleType = ImageView.ScaleType.CENTER_CROP
                controller.reloadAvatar(view, spec)
            },
            modifier = Modifier.size(sizeDp.dp).clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(CircleShape)
                .background(MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            AvatarFallback(fallback)
        }
    }
}

@Composable
private fun UrlAvatar(imageUrl: String, fallback: String, sizeDp: Int) {
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = rememberImageRequest(imageUrl, "avatar"),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        ) {
            val imageState by painter.state.collectAsState()
            when (imageState) {
                is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                is AsyncImagePainter.State.Loading -> ImageLoadingIndicator()
                else -> AvatarFallback(fallback)
            }
        }
    }
}

@Composable
private fun AvatarFallback(fallback: String) {
    Text(
        text = fallback.take(1).ifBlank { "Q" },
        color = MiuixTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
internal fun ImageLoadingIndicator() {
    InfiniteProgressIndicator(
        modifier = Modifier.size(20.dp),
        color = MiuixTheme.colorScheme.primary
    )
}

private fun imageModel(value: String?): Any? {
    val normalized = value?.normalizedImageValue() ?: return null
    return if (normalized.startsWith("/") && File(normalized).exists()) {
        File(normalized)
    } else {
        normalized
    }
}

@Composable
internal fun rememberImageRequest(
    value: String?,
    cacheKeyPrefix: String,
    requestVersion: Int = 0
): ImageRequest? {
    val context = LocalContext.current
    val model = remember(value, requestVersion) { imageModel(value) } ?: return null
    val cacheKey = remember(model, cacheKeyPrefix) { "$cacheKeyPrefix:$model" }
    return remember(context, model, cacheKey, requestVersion) {
        ImageRequest.Builder(context)
            .data(model)
            .memoryCacheKey(cacheKey)
            .diskCacheKey(cacheKey)
            .build()
    }
}
