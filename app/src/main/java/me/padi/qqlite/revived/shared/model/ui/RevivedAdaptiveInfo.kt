package me.padi.qqlite.revived.shared.model.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class RevivedWidthSizeClass { Compact, Medium, Expanded }

data class RevivedAdaptiveInfo(
    val isTablet: Boolean,
    val isLandscape: Boolean,
    val widthClass: RevivedWidthSizeClass
) {
    companion object {
        fun create(width: Dp, height: Dp): RevivedAdaptiveInfo {
            val w = width.safeValue()
            val h = height.safeValue()
            return RevivedAdaptiveInfo(
                // 保留旧 Home 判定：宽横屏手机也可能进入平板布局。
                isTablet = minOf(w, h) >= 600f || w >= 720f,
                isLandscape = w > h,
                widthClass = when {
                    w >= 840f -> RevivedWidthSizeClass.Expanded
                    w >= 600f -> RevivedWidthSizeClass.Medium
                    else -> RevivedWidthSizeClass.Compact
                }
            )
        }
    }
}

data class AioWindowInfo(
    val topBarHeight: Dp,
    val topBarHorizontalPadding: Dp,
    val titleMaxWidth: Dp,
    val listHorizontalPadding: Dp,
    val listVerticalPadding: Dp,
    val listItemSpacing: Dp,
    val bubbleMaxWidth: Dp,
    val emojiSheetMaxWidth: Dp,
    val inputHorizontalPadding: Dp,
    val inputVerticalPadding: Dp,
    val inputMaxHeight: Dp,
    val emojiSystemColumns: Int,
    val emojiPackColumns: Int
) {
    companion object {
        fun create(width: Dp, height: Dp): AioWindowInfo {
            val w = width.safeValue()
            val h = height.safeValue()
            val adaptive = RevivedAdaptiveInfo.create(width, height)
            val wide = adaptive.widthClass != RevivedWidthSizeClass.Compact
            val expanded = adaptive.widthClass == RevivedWidthSizeClass.Expanded
            val shortLandscape = adaptive.isLandscape && h < 480f
            val horizontal = minOf(if (wide) 16f else 10f, w / 2f)
            val listHorizontal = minOf(if (wide) 24f else 12f, w / 2f)
            val sheetWidth = minOf(w, if (expanded) 720f else if (wide) 600f else 480f)
            return AioWindowInfo(
                topBarHeight = minOf(h, if (shortLandscape) 48f else if (adaptive.isTablet) 64f else 58f).dp,
                topBarHorizontalPadding = horizontal.dp,
                titleMaxWidth = minOf(
                    (w - 2f * horizontal - 136f).coerceAtLeast(0f),
                    if (expanded) 360f else if (wide) 280f else 172f
                ).dp,
                listHorizontalPadding = listHorizontal.dp,
                listVerticalPadding = minOf(h / 2f, if (shortLandscape) 8f else if (wide) 16f else 12f).dp,
                listItemSpacing = minOf(h, if (wide) 12f else 10f).dp,
                // 消息行两侧内边距之外，还需预留 32dp 头像和 8dp 间隔。
                bubbleMaxWidth = minOf(
                    (w - 2f * listHorizontal - 40f).coerceAtLeast(0f),
                    if (expanded) 560f else if (wide) 420f else 292f
                ).dp,
                emojiSheetMaxWidth = sheetWidth.dp,
                inputHorizontalPadding = minOf(horizontal, sheetWidth / 2f).dp,
                inputVerticalPadding = minOf(h / 2f, if (shortLandscape) 4f else 8f).dp,
                // 调用方有 heightIn(min = 44.dp)，即使零窗口也不能返回小于 44 的 max。
                inputMaxHeight = minOf(h * 0.25f, if (shortLandscape) 80f else if (wide) 160f else 112f)
                    .coerceAtLeast(44f).dp,
                // 以实际面板宽度推导列数，超窄窗口至少一列；普通手机仍为 5/3 列。
                emojiSystemColumns = ((sheetWidth + 8f) / 64f).toInt().coerceIn(1, if (wide) 10 else 5),
                emojiPackColumns = ((sheetWidth + 8f) / 104f).toInt().coerceIn(1, if (wide) 6 else 3)
            )
        }
    }
}

data class QavWindowInfo(
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val voiceAvatarSize: Dp,
    val videoIdentityAvatarSize: Dp,
    val controlSpacing: Dp,
    val controlButtonSize: Dp,
    val controlIconSize: Dp,
    val controlLabelTopPadding: Dp,
    val peerNameSize: TextUnit,
    val statusTextSize: TextUnit
) {
    companion object {
        fun create(width: Dp, height: Dp): QavWindowInfo {
            val w = width.safeValue()
            val h = height.safeValue()
            val adaptive = RevivedAdaptiveInfo.create(width, height)
            val targetScale = if (adaptive.isTablet) 1.2f else 1f
            // 三个视频按钮含间距和页面边距共 270dp；矮横屏优先节约垂直空间。
            val scale = minOf(targetScale, w / 270f, h / 480f)
            val textScale = scale.coerceIn(0.75f, targetScale)
            return QavWindowInfo(
                horizontalPadding = (18f * scale).dp,
                verticalPadding = (20f * scale).dp,
                voiceAvatarSize = (88f * scale).dp,
                videoIdentityAvatarSize = (42f * scale).dp,
                controlSpacing = (20f * scale).dp,
                controlButtonSize = (58f * scale).dp,
                controlIconSize = (28f * scale).dp,
                controlLabelTopPadding = (8f * scale).dp,
                // 视频姓名调用方会再减去 6sp；不要把文本随零窗口缩到零。
                peerNameSize = (24f * textScale).sp,
                statusTextSize = (13f * textScale).sp
            )
        }
    }
}

// 窗口尚未测量或收到非法尺寸时，按零处理，不传播 NaN/负数到布局约束。
private fun Dp.safeValue(): Float = value.takeIf { it.isFinite() && it >= 0f } ?: 0f
