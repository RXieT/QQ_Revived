package me.padi.qqlite.revived.compose.screens.aio

import android.os.Build
import android.view.View
import android.widget.Toast
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.padi.qqlite.revived.utils.IMAGE_SELECTION_LIMIT
import me.padi.qqlite.revived.utils.RecentImage
import me.padi.qqlite.revived.utils.RecentImagesResult
import me.padi.qqlite.revived.utils.copyRecentImageToCache
import me.padi.qqlite.revived.utils.loadRecentImages
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal enum class AioActivePanel { None, Emoji, Images }

/** 常规窗口 240..300dp；短横屏优先遵守可用高度的 40%，不强撑 240dp。 */
internal fun aioAttachmentPanelHeight(availableHeightDp: Float): Float {
    if (!availableHeightDp.isFinite() || availableHeightDp <= 0f) return 0f
    return minOf((availableHeightDp * 0.34f).coerceIn(240f, 300f), availableHeightDp * 0.4f)
}

internal fun toggleRecentImageSelection(selection: List<RecentImage>, image: RecentImage): List<RecentImage> = when {
    selection.any { it.uri == image.uri } -> selection.filterNot { it.uri == image.uri }
    selection.size < IMAGE_SELECTION_LIMIT -> selection + image
    else -> selection
}

/** 始终保留在主屏组合中，收起面板不丢多选；只在打开图库/手动刷新时查询。 */
@Composable
internal fun AioAttachmentPanel(
    activePanel: AioActivePanel,
    imeVisible: Boolean,
    height: Dp,
    controller: AioUiController,
    onFullAlbum: () -> Unit,
    emojiContent: @Composable () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var result by remember(controller) { mutableStateOf<RecentImagesResult?>(null) }
    var selection by remember(controller) { mutableStateOf<List<RecentImage>>(emptyList()) }
    var sending by remember(controller) { mutableStateOf(false) }
    var refresh by remember(controller) { mutableIntStateOf(0) }
    var lastVisiblePanel by remember(controller) { mutableStateOf(AioActivePanel.Emoji) }
    LaunchedEffect(activePanel) {
        if (activePanel != AioActivePanel.None) lastVisiblePanel = activePanel
    }
    val displayedPanel = if (activePanel == AioActivePanel.None) lastVisiblePanel else activePanel
    LaunchedEffect(activePanel, refresh, controller, context) {
        if (activePanel == AioActivePanel.Images) {
            result = null
            result = loadRecentImages(context)
        }
    }
    // IME 正在显示/退出时先不占面板空间，避免键盘与面板叠高。
    if (!imeVisible) {
        AnimatedVisibility(
            visible = activePanel != AioActivePanel.None,
            enter = fadeIn(tween(120)) + expandVertically(
                animationSpec = tween(120), expandFrom = Alignment.Top,
                initialHeight = { (it * 0.94f).toInt() }
            ),
            exit = fadeOut(tween(90))
        ) {
            Box(Modifier.fillMaxWidth().height(height).background(MiuixTheme.colorScheme.surface)) {
                when (displayedPanel) {
                    AioActivePanel.Emoji -> emojiContent()
                    AioActivePanel.Images -> RecentImagesPanel(
                        result = result,
                        selection = selection,
                        sending = sending,
                        onRefresh = { if (!sending) refresh++ },
                        onFullAlbum = onFullAlbum,
                        onToggle = { image ->
                            if (!sending) {
                                val next = toggleRecentImageSelection(selection, image)
                                if (next == selection) {
                                    Toast.makeText(context, "最多选择 6 张图片", Toast.LENGTH_SHORT).show()
                                }
                                selection = next
                            }
                        },
                        onSend = {
                            if (!sending && selection.isNotEmpty()) {
                                sending = true
                                val batch = selection.toList()
                                scope.launch {
                                    try {
                                        for (image in batch) {
                                            val copy = copyRecentImageToCache(context, image)
                                            val path = copy.getOrNull()
                                            // 复制在 IO；显式切回主线程调用宿主发送。
                                            val sent = if (path != null) {
                                                try {
                                                    withContext(Dispatchers.Main.immediate) { controller.sendImage(path) }
                                                } catch (cancelled: CancellationException) {
                                                    throw cancelled
                                                } catch (_: Exception) {
                                                    false
                                                }
                                            } else false
                                            if (sent) {
                                                selection = selection.filterNot { it.uri == image.uri }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    copy.exceptionOrNull()?.message ?: "图片发送失败，已保留选择，可重试",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                            // 不立即删除副本，宿主可能异步读取。下次复制时仅清理 24h 前的文件。
                                        }
                                    } finally {
                                        sending = false
                                    }
                                }
                            }
                        }
                    )
                    AioActivePanel.None -> Unit
                }
            }
        }
    }
}

@Composable
private fun RecentImagesPanel(
    result: RecentImagesResult?,
    selection: List<RecentImage>,
    sending: Boolean,
    onRefresh: () -> Unit,
    onFullAlbum: () -> Unit,
    onToggle: (RecentImage) -> Unit,
    onSend: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 放大文字时让辅助入口横向滚动，发送按钮始终可见且不会被挤出。
            Row(Modifier.weight(1f).horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
                PanelTextAction("完整相册", onFullAlbum, enabled = !sending)
                PanelTextAction("刷新", onRefresh, enabled = !sending)
            }
            Button(
                onClick = onSend,
                enabled = selection.isNotEmpty() && !sending,
                minHeight = 40.dp,
                minWidth = 88.dp,
                insideMargin = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColorsPrimary()
            ) {
                Text(
                    text = if (sending) "发送中" else "发送 ${selection.size}/6",
                    fontSize = 13.sp,
                    color = if (selection.isNotEmpty()) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1
                )
            }
        }
        when (result) {
            null -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                InfiniteProgressIndicator(modifier = Modifier.size(24.dp))
            }
            is RecentImagesResult.AccessRequired -> PanelExplanation(result.message, Modifier.weight(1f))
            is RecentImagesResult.Failed -> PanelExplanation(result.message, Modifier.weight(1f))
            is RecentImagesResult.Available -> {
                if (result.limitedAccess) {
                    Text("仅显示已授权图片", fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                }
                if (result.images.isEmpty()) {
                    PanelExplanation("没有可显示的已授权图片，可打开完整相册选择。", Modifier.weight(1f))
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(100.dp),
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        items(result.images, key = { it.uri }) { image ->
                            RecentImageThumbnail(image, selection.indexOfFirst { it.uri == image.uri }, !sending) {
                                onToggle(image)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentImageThumbnail(image: RecentImage, selectedIndex: Int, enabled: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val request = remember(context, image.uri) {
        ImageRequest.Builder(context).data(image.uri).size(240, 240).build()
    }
    val chosen = selectedIndex >= 0
    Box(
        Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp))
            .background(MiuixTheme.colorScheme.surfaceVariant)
            .border(if (chosen) 2.dp else 0.dp, if (chosen) MiuixTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(8.dp))
            .semantics {
                selected = chosen
                contentDescription = image.displayName + if (chosen) "，已选择第 ${selectedIndex + 1} 张" else "，未选择"
            }
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        AsyncImage(model = request, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(
            Modifier.align(Alignment.TopEnd).padding(5.dp).size(30.dp).clip(CircleShape)
                .background(if (chosen) MiuixTheme.colorScheme.primary else Color.Black.copy(alpha = 0.45f))
                .border(1.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (chosen) Text("${selectedIndex + 1}", fontSize = 12.sp, color = MiuixTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun PanelExplanation(message: String, modifier: Modifier) {
    // 短横屏/1.5x 字号可滚动说明，不挤掉固定的完整相册与发送入口。
    Box(modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(12.dp)) {
        Text(message, fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
    }
}

@Composable
internal fun PanelTextAction(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Box(
        Modifier.clip(RoundedCornerShape(8.dp)).clickable(enabled = enabled, onClick = onClick)
            .heightIn(min = 44.dp).padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
            color = if (enabled) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary)
    }
}

/** 不依赖宿主没有提供的 OnBackPressedDispatcherOwner，也不替换 Window.Callback。 */
@Composable
internal fun AioPanelBackCallback(enabled: Boolean, onClose: () -> Unit) {
    val view = LocalView.current
    val close by rememberUpdatedState(onClose)
    DisposableEffect(view, enabled) {
        var dispatcher: OnBackInvokedDispatcher? = null
        val callback = OnBackInvokedCallback { close() }
        fun unregister() {
            dispatcher?.let { runCatching { it.unregisterOnBackInvokedCallback(callback) } }
            dispatcher = null
        }
        fun register() {
            unregister()
            if (enabled && Build.VERSION.SDK_INT >= 33 && view.isAttachedToWindow && view.windowToken != null) {
                // 宿主 target 28/未启用 OnBackInvoked 时可能不分发此回调；顶部返回仍先收起面板。
                val available = view.findOnBackInvokedDispatcher()
                if (available != null) {
                    runCatching {
                        available.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, callback)
                    }.onSuccess { dispatcher = available }
                }
            }
        }
        val attachment = object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) = register()
            override fun onViewDetachedFromWindow(v: View) = unregister()
        }
        view.addOnAttachStateChangeListener(attachment)
        register()
        onDispose {
            view.removeOnAttachStateChangeListener(attachment)
            unregister()
        }
    }
}
