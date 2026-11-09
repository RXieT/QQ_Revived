package me.padi.qqlite.revived.compose.screens.home

import android.widget.ImageView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.glass.GlassShape
import me.padi.qqlite.revived.compose.component.glass.revivedGlass
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import me.padi.qqlite.revived.compose.component.avatar.HostAvatar
import me.padi.qqlite.revived.compose.component.avatar.rememberImageRequest
import me.padi.qqlite.revived.compose.component.bottombar.BottomBar
import me.padi.qqlite.revived.compose.component.bottombar.BottomBarItemSpec
import me.padi.qqlite.revived.compose.component.bottombar.SideRail
import me.padi.qqlite.revived.compose.component.state.MiuixBadgePill
import me.padi.qqlite.revived.compose.component.state.MiuixEmptyState
import me.padi.qqlite.revived.compose.component.state.MiuixLoadingState
import me.padi.qqlite.revived.compose.screens.home.pages.ContactsPage
import me.padi.qqlite.revived.compose.screens.home.pages.ProfileActionsPage
import me.padi.qqlite.revived.compose.screens.home.pages.QZoneFeedPage
import me.padi.qqlite.revived.compose.screens.home.pages.RecentMessagesPage
import me.padi.qqlite.revived.compose.theme.ApplyTransparentSystemBars
import me.padi.qqlite.revived.compose.theme.RevivedTheme
import me.padi.qqlite.revived.compose.theme.RevivedUiMode
import me.padi.qqlite.revived.compose.theme.currentRevivedThemePreference
import me.padi.qqlite.revived.compose.theme.currentUiMode
import me.padi.qqlite.revived.shared.model.home.AvatarSpec
import me.padi.qqlite.revived.shared.model.home.HOME_RAIL_WIDTH_DP
import me.padi.qqlite.revived.shared.model.home.HomePage
import me.padi.qqlite.revived.shared.model.home.HomeProfile
import me.padi.qqlite.revived.shared.model.home.HomeUiState
import me.padi.qqlite.revived.shared.model.home.HomeWindowInfo
import me.padi.qqlite.revived.shared.model.home.ScrollSnapshot
import me.padi.qqlite.revived.shared.model.home.coerceInHome
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import androidx.compose.material3.Scaffold as MaterialScaffold
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold

@Composable
internal fun HomeScreen(controller: HomeUiController) {
    val uiState by controller.uiState.collectAsState()
    val pages = uiState.pages
    val currentIndex = uiState.currentIndex.coerceInHome(pages)
    val profile = uiState.profile
    val bottomBarItems = remember(pages) {
        pages.map { page ->
            BottomBarItemSpec(
                key = page.title, label = page.title, icon = page.icon()
            )
        }
    }

    RevivedTheme {
        val themePreference = currentRevivedThemePreference()
        val uiMode = currentUiMode()
        val backgroundColor = when (uiMode) {
            RevivedUiMode.Miuix -> MiuixTheme.colorScheme.secondaryVariant
            RevivedUiMode.Material -> MaterialTheme.colorScheme.surfaceContainerLow
        }
        val surfaceColor = when (uiMode) {
            RevivedUiMode.Miuix -> MiuixTheme.colorScheme.surface
            RevivedUiMode.Material -> MaterialTheme.colorScheme.surface
        }
        val liquidBackdrop = rememberLayerBackdrop {
            drawRect(backgroundColor)
            drawContent()
        }
        val pagerState = rememberPagerState(
            initialPage = currentIndex, pageCount = { pages.size.coerceAtLeast(1) })
        val scope = rememberCoroutineScope()
        ApplyTransparentSystemBars(surfaceColor)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            HomeBackdrop(
                uiMode = uiMode,
                accentColor = MiuixTheme.colorScheme.primary,
                modifier = Modifier.matchParentSize()
            )
            val windowInfo = remember(maxWidth, maxHeight) {
                HomeWindowInfo.create(maxWidth, maxHeight)
            }
            LaunchedEffect(currentIndex, pages.size) {
                if (pages.isNotEmpty() && !pagerState.isScrollInProgress && pagerState.settledPage != currentIndex) {
                    pagerState.animateScrollToPage(currentIndex)
                }
            }
            LaunchedEffect(pagerState, pages.size) {
                if (pages.isEmpty()) return@LaunchedEffect
                snapshotFlow { pagerState.settledPage }.distinctUntilChanged().collect { page ->
                    controller.selectPage(page)
                }
            }
            val topBar: @Composable () -> Unit = {
                HomeTopBar(
                    controller = controller,
                    profile = profile,
                    currentPage = pages.getOrNull(currentIndex),
                    windowInfo = windowInfo,
                    enableLiquidGlass = themePreference.hasGlassEffects,
                    surfaceColor = surfaceColor,
                    backdrop = liquidBackdrop,
                )
            }
            val bottomBar: @Composable () -> Unit = {
                if (!windowInfo.useNavigationRail) {
                    BottomBar(
                        items = bottomBarItems,
                        selectedIndex = pagerState.currentPage,
                        onSelect = { index ->
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        surfaceColor = surfaceColor,
                        backdrop = liquidBackdrop,
                        pageProgress = { pagerState.currentPage + pagerState.currentPageOffsetFraction }
                    )
                }
            }
            val content: @Composable (PaddingValues) -> Unit = { innerPadding ->
                val listInsets = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = innerPadding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                            top = 0.dp,
                            end = innerPadding.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                            bottom = 0.dp
                        )
                ) {
                    if (windowInfo.useNavigationRail) {
                        SideRail(
                            items = bottomBarItems,
                            selectedIndex = pagerState.currentPage,
                            onSelect = { index ->
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            surfaceColor = surfaceColor,
                            backdrop = liquidBackdrop,
                            modifier = Modifier.width(HOME_RAIL_WIDTH_DP.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (themePreference.hasGlassEffects) Modifier.layerBackdrop(liquidBackdrop) else Modifier)
                            .background(backgroundColor)
                            .padding(horizontal = windowInfo.contentPadding)
                    ) {
                        if (pages.isEmpty()) {
                            MiuixLoadingState("正在加载首页")
                        } else {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize(),
                                // 不将四页全部保留在 Composition 中，减少常驻 View/图片。
                                beyondViewportPageCount = 0,
                                userScrollEnabled = true
                            ) { pageIndex ->
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Column(
                                        modifier = Modifier
                                            .widthIn(max = windowInfo.maxContentWidth)
                                            .fillMaxWidth()
                                            .align(Alignment.TopCenter)
                                    ) {
                                        CompositionLocalProvider(LocalHomeContentInsets provides listInsets) {
                                            HomeContent(
                                                controller = controller,
                                                uiState = uiState,
                                                currentPage = pages.getOrNull(pageIndex),
                                                windowInfo = windowInfo
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            when (uiMode) {
                RevivedUiMode.Miuix -> MiuixScaffold(
                    topBar = topBar,
                    bottomBar = bottomBar,
                    content = content
                )

                RevivedUiMode.Material -> MaterialScaffold(
                    topBar = topBar,
                    bottomBar = bottomBar,
                    containerColor = Color.Transparent,
                    content = content
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    controller: HomeUiController,
    profile: HomeProfile,
    currentPage: HomePage?,
    windowInfo: HomeWindowInfo,
    enableLiquidGlass: Boolean,
    surfaceColor: Color,
    backdrop: Backdrop,
) {
    val pageTitle = currentPage?.title ?: "首页"
    val uiMode = currentUiMode()
    val contentColor = when (uiMode) {
        RevivedUiMode.Miuix -> MiuixTheme.colorScheme.onSurface
        RevivedUiMode.Material -> MaterialTheme.colorScheme.onSurface
    }
    val appBarEdgePadding = (windowInfo.horizontalPadding - 16.dp).coerceAtLeast(0.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
            .homeTopBarGlass(
                enabled = enableLiquidGlass,
                surfaceColor = surfaceColor,
                backdrop = backdrop
            )
    ) {
        SmallTopAppBar(
            title = pageTitle,
            subtitle = profile.nickName ?: "QQ Revived",
            color = if (enableLiquidGlass) Color.Transparent else surfaceColor.copy(alpha = 0.92f),
            titleColor = contentColor,
            subtitleColor = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            navigationIcon = {
                HostAppIcon(modifier = Modifier.padding(start = appBarEdgePadding))
            },
            actions = {
                HomeProfileAvatar(
                    controller = controller,
                    profile = profile,
                    modifier = Modifier.padding(end = appBarEdgePadding),
                    sizeDp = 42
                )
            }
        )
    }
}

@Composable
private fun Modifier.homeTopBarGlass(
    enabled: Boolean,
    surfaceColor: Color,
    backdrop: Backdrop,
): Modifier {
    val corner = androidx.compose.foundation.shape.CornerSize(22.dp)
    val square = androidx.compose.foundation.shape.CornerSize(0.dp)
    return revivedGlass(
        backdrop = backdrop,
        shape = GlassShape(square, square, corner, corner),
        surfaceColor = surfaceColor,
        enabled = enabled
    )
}

@Composable
private fun HomeContent(
    controller: HomeUiController,
    uiState: HomeUiState,
    currentPage: HomePage?,
    windowInfo: HomeWindowInfo
) {
    if (currentPage == null) {
        MiuixLoadingState("正在加载首页")
        return
    }
    if (uiState.isPageDataLoading(currentPage)) {
        MiuixLoadingState("正在加载${currentPage.title}")
        return
    }
    Column(modifier = Modifier.fillMaxSize()) {
        when (currentPage.title) {
            "消息" -> RecentMessagesPage(controller, uiState, windowInfo)
            "联系人" -> ContactsPage(controller, uiState, windowInfo)
            "动态" -> QZoneFeedPage(controller, uiState, windowInfo)
            "我的" -> ProfileActionsPage(controller, uiState, windowInfo)
            else -> MiuixEmptyState("暂无页面")
        }
    }
}

private fun HomeUiState.isPageDataLoading(page: HomePage): Boolean {
    return when (page.title) {
        "消息" -> recentRows.isEmpty()
        "联系人" -> contacts.isEmpty()
        "动态" -> qzoneFeeds.isEmpty()
        // 模块设置不依赖宿主动作列表，不能被加载态挡住。
        "我的" -> false
        else -> false
    }
}

@Composable
private fun HomeBackdrop(
    uiMode: RevivedUiMode,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val secondary = when (uiMode) {
        RevivedUiMode.Miuix -> Color(0xFF7AC7FF)
        RevivedUiMode.Material -> Color(0xFF9B8CFF)
    }
    val warm = when (uiMode) {
        RevivedUiMode.Miuix -> Color(0xFFFFD38A)
        RevivedUiMode.Material -> Color(0xFFFFB4A2)
    }
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.12f),
                        secondary.copy(alpha = 0.10f),
                        Color.Transparent
                    )
                )
            )
        }
        Box(
            modifier = Modifier
                .offset(x = (-28).dp, y = 22.dp)
                .size(220.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.30f), Color.Transparent
                        )
                    ), shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .offset(x = 180.dp, y = 96.dp)
                .size(180.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            secondary.copy(alpha = 0.24f), Color.Transparent
                        )
                    ), shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .offset(x = 96.dp, y = 360.dp)
                .size(240.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            warm.copy(alpha = 0.16f), Color.Transparent
                        )
                    ), shape = CircleShape
                )
        )
    }
}

@Composable
private fun HostAppIcon(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val appIcon = remember(context) {
        context.applicationInfo.loadIcon(context.packageManager)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MiuixTheme.colorScheme.surfaceVariant)
            .border(
                1.5.dp, MiuixTheme.colorScheme.primary.copy(alpha = 0.34f), RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AndroidView(
            factory = { viewContext ->
                ImageView(viewContext).apply {
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }
            }, update = { view ->
                view.setImageDrawable(appIcon)
            }, modifier = Modifier.size(30.dp)
        )

        Text(
            text = "QQ Revived",
            style = MiuixTheme.textStyles.headline2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HomeProfileAvatar(
    controller: HomeUiController, profile: HomeProfile, modifier: Modifier = Modifier, sizeDp: Int
) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.24f))
            .border(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.28f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val uid = profile.uid.orEmpty()
        if (uid.isNotBlank()) {
            HostAvatar(
                controller = controller, spec = AvatarSpec(
                    chatType = 1,
                    uid = uid,
                    uin = uid.toLongOrNull() ?: 0L,
                    fragmentRef = controller.hostFragmentRef
                ), fallback = profile.nickName?.take(1).orEmpty(), sizeDp = sizeDp
            )
        } else if (!profile.avatarPath.isNullOrBlank()) {
            AsyncImage(
                model = rememberImageRequest(profile.avatarPath, "home-profile"),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            Text(text = profile.nickName?.take(1)?.takeIf { it.isNotBlank() } ?: "Q",
                color = MiuixTheme.colorScheme.primary,
                fontSize = (sizeDp * 0.52f).sp,
                fontWeight = FontWeight.Bold)
        }
    }
}

private fun HomePage.icon(): ImageVector {
    return when (title) {
        "消息" -> Icons.AutoMirrored.Filled.Message
        "联系人" -> Icons.Filled.Contacts
        "动态" -> Icons.Filled.DynamicFeed
        "我的" -> Icons.Filled.AccountCircle
        else -> Icons.Filled.Widgets
    }
}

@Composable
internal fun rememberHomeListState(snapshot: ScrollSnapshot): LazyListState {
    return rememberLazyListState(
        initialFirstVisibleItemIndex = snapshot.index.coerceAtLeast(0),
        initialFirstVisibleItemScrollOffset = snapshot.offset.coerceAtLeast(0)
    )
}

@Composable
internal fun PersistListScroll(
    listState: LazyListState, onChanged: (ScrollSnapshot) -> Unit
) {
    val currentOnChanged by rememberUpdatedState(onChanged)
    fun snapshot() = ScrollSnapshot(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.distinctUntilChanged().collect { scrolling ->
            if (!scrolling) currentOnChanged(snapshot())
        }
    }
    DisposableEffect(listState) {
        onDispose { currentOnChanged(snapshot()) }
    }
}


@Composable
internal fun HomeListRow(
    controller: HomeUiController,
    title: String,
    subtitle: String,
    meta: String,
    badge: String?,
    leading: String,
    avatar: AvatarSpec?,
    icon: ImageVector,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp),
        pressFeedbackType = PressFeedbackType.Sink,
        showIndication = true,
        onClick = onClick,
        onLongPress = onLongClick,
        colors = CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.surfaceVariant,
            contentColor = MiuixTheme.colorScheme.onSurface
        ),

        ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            HostAvatar(controller, avatar, leading, sizeDp = 48)
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = title.ifBlank { "未命名" },
                    color = MiuixTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            Column(modifier = Modifier.padding(start = 8.dp), horizontalAlignment = Alignment.End) {
                Text(
                    text = meta,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    fontSize = 11.sp
                )
                if (badge != null) {
                    MiuixBadgePill(text = badge, modifier = Modifier.padding(top = 5.dp))
                }
            }
        }
    }
}
