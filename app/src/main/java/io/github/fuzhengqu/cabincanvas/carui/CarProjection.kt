package io.github.fuzhengqu.cabincanvas.carui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.fuzhengqu.cabincanvas.core.CarViewport
import io.github.fuzhengqu.cabincanvas.data.CarAppCategory
import io.github.fuzhengqu.cabincanvas.data.CarAppEntry
import io.github.fuzhengqu.cabincanvas.data.CarGlyph
import io.github.fuzhengqu.cabincanvas.data.CarMedia
import io.github.fuzhengqu.cabincanvas.data.FakeCarData
import io.github.fuzhengqu.cabincanvas.designsystem.CarBadge
import io.github.fuzhengqu.cabincanvas.designsystem.CarGhostButton
import io.github.fuzhengqu.cabincanvas.designsystem.CarIcon
import io.github.fuzhengqu.cabincanvas.designsystem.CarIconButton
import io.github.fuzhengqu.cabincanvas.designsystem.CarPrimaryButton
import io.github.fuzhengqu.cabincanvas.designsystem.CarProgress
import io.github.fuzhengqu.cabincanvas.designsystem.GlassCard
import io.github.fuzhengqu.cabincanvas.designsystem.LocalCarDimens
import io.github.fuzhengqu.cabincanvas.designsystem.LocalCarPalette
import io.github.fuzhengqu.cabincanvas.designsystem.LocalCarTypography
import kotlinx.coroutines.delay

enum class CarProjectionMode(val label: String) {
    FULL("全屏"),
    COMPAT("兼容模式"),
    PHONE("手机比例"),
    SPLIT("分屏"),
    IMMERSIVE("沉浸模式"),
}

@Composable
fun CarProjectionScreen(
    viewport: CarViewport,
    app: CarAppEntry,
    mode: CarProjectionMode,
    media: CarMedia,
    isPlaying: Boolean,
    progress: Float,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onApps: () -> Unit,
    onModeChange: (CarProjectionMode) -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onVoice: () -> Unit,
) {
    var chromeVisible by remember { mutableStateOf(true) }
    LaunchedEffect(chromeVisible) {
        if (chromeVisible) {
            delay(4000)
            chromeVisible = false
        }
    }
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                        chromeVisible = true
                    }
                }
            },
    ) {
        AnimatedVisibility(visible = chromeVisible, enter = fadeIn(), exit = fadeOut()) {
            CarTopBar(
                title = app.label,
                onBack = onBack,
                subtitle = "投屏预览 · ${mode.label}",
                actions = {
                    CarIconButton(CarGlyph.SPLIT, "分屏", {
                        onModeChange(if (mode == CarProjectionMode.SPLIT) CarProjectionMode.FULL else CarProjectionMode.SPLIT)
                    }, active = mode == CarProjectionMode.SPLIT)
                    CarIconButton(
                        if (mode == CarProjectionMode.IMMERSIVE) CarGlyph.EXIT else CarGlyph.FULLSCREEN,
                        "沉浸模式",
                        {
                            onModeChange(
                                if (mode == CarProjectionMode.IMMERSIVE) CarProjectionMode.FULL else CarProjectionMode.IMMERSIVE,
                            )
                        },
                        active = mode == CarProjectionMode.IMMERSIVE,
                    )
                    CarIconButton(CarGlyph.CLOSE, "关闭", onHome)
                },
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (mode) {
                CarProjectionMode.COMPAT -> CompatStage(app) { MockAppSurface(app, Modifier.fillMaxSize()) }
                CarProjectionMode.PHONE -> PhoneStage(app)
                CarProjectionMode.SPLIT -> SplitStage(app, media, isPlaying, progress, onTogglePlay, onNext, onPrev)
                CarProjectionMode.IMMERSIVE -> ImmersiveStage(app)
                CarProjectionMode.FULL -> MockAppSurface(app, Modifier.fillMaxSize())
            }
        }

        if (mode == CarProjectionMode.IMMERSIVE) {
            FloatingDock(onHome, onApps, onBack, onVoice, Modifier.padding(bottom = dimens.padSmall))
        } else {
            Spacer(Modifier.height(dimens.padSmall))
        }
    }
}

@Composable
private fun CompatStage(app: CarAppEntry, content: @Composable () -> Unit) {
    val palette = LocalCarPalette.current
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        val targetWidth = minOf(maxWidth, maxHeight * (16f / 9f))
        Box(
            modifier = Modifier
                .width(targetWidth)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, palette.outline, RoundedCornerShape(12.dp)),
        ) {
            content()
        }
    }
}

@Composable
private fun PhoneStage(app: CarAppEntry) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val phoneWidth = minOf(maxWidth * 0.46f, maxHeight * (9f / 16f))
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = dimens.pad),
            horizontalArrangement = Arrangement.spacedBy(dimens.gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(phoneWidth)
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, palette.outline, RoundedCornerShape(24.dp)),
            ) {
                MockAppSurface(app, Modifier.fillMaxSize())
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(dimens.gap)) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("手机比例窗口", style = LocalCarTypography.current.cardTitle, color = palette.textPrimary)
                    Spacer(Modifier.height(dimens.gapSmall))
                    Text(
                        "竖屏应用不再被拉伸。右侧保留车机控制、媒体和返回入口。",
                        style = LocalCarTypography.current.bodySmall,
                        color = palette.textSecondary,
                    )
                }
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("投屏提示", style = LocalCarTypography.current.cardTitle, color = palette.textPrimary)
                    Spacer(Modifier.height(dimens.gapSmall))
                    Text(
                        "真实投屏时，这里显示应用的真实界面；当前为 UI 预览。",
                        style = LocalCarTypography.current.bodySmall,
                        color = palette.textSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun SplitStage(
    app: CarAppEntry,
    media: CarMedia,
    isPlaying: Boolean,
    progress: Float,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {
    val dimens = LocalCarDimens.current
    Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = dimens.pad),
        horizontalArrangement = Arrangement.spacedBy(dimens.gap),
    ) {
        Box(modifier = Modifier.weight(0.58f).fillMaxHeight()) {
            MockAppSurface(app, Modifier.fillMaxSize())
        }
        Box(modifier = Modifier.weight(0.42f).fillMaxHeight()) {
            MediaCard(media, isPlaying, progress, onTogglePlay, onNext, onPrev, {}, Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun ImmersiveStage(app: CarAppEntry) {
    MockAppSurface(app, Modifier.fillMaxSize())
}

@Composable
private fun FloatingDock(
    onHome: () -> Unit,
    onApps: () -> Unit,
    onBack: () -> Unit,
    onVoice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(palette.dock)
                .border(1.dp, palette.outline, RoundedCornerShape(50))
                .padding(horizontal = dimens.padSmall, vertical = dimens.padSmall),
            horizontalArrangement = Arrangement.spacedBy(dimens.gapSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CarIconButton(CarGlyph.HOME, "首页", onHome)
            CarIconButton(CarGlyph.APPS, "应用", onApps)
            CarIconButton(CarGlyph.BACK, "返回", onBack)
            CarIconButton(CarGlyph.MIC, "语音", onVoice)
        }
    }
}

@Composable
fun MockAppSurface(app: CarAppEntry, modifier: Modifier = Modifier) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(dimens.cardRadius))
            .background(
                Brush.linearGradient(
                    listOf(
                        app.tint.copy(alpha = 0.28f),
                        palette.surface.copy(alpha = 0.96f),
                        palette.background,
                    ),
                ),
            )
            .border(1.dp, palette.outline, RoundedCornerShape(dimens.cardRadius))
            .padding(dimens.cardPadding),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(dimens.touch)
                    .clip(RoundedCornerShape(dimens.cardRadiusSmall))
                    .background(app.tint),
                contentAlignment = Alignment.Center,
            ) {
                if (app.icon != null) {
                    Image(
                        bitmap = app.icon,
                        contentDescription = app.label,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(5.dp),
                    )
                } else {
                    Text(app.label.take(1), style = LocalCarTypography.current.title, color = Color.White)
                }
            }
            Spacer(Modifier.width(dimens.gap))
            Column(modifier = Modifier.weight(1f)) {
                Text(app.label, style = LocalCarTypography.current.cardTitle, color = palette.textPrimary)
                Text("应用内容预览", style = LocalCarTypography.current.caption, color = palette.textSecondary)
            }
            CarBadge("预览", app.tint)
        }
        Spacer(Modifier.height(dimens.gap))
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (app.category) {
                CarAppCategory.NAVI -> NaviPreview()
                CarAppCategory.MUSIC -> MusicPreview()
                CarAppCategory.VIDEO -> VideoPreview(app)
                CarAppCategory.MINE -> ChatPreview()
                CarAppCategory.TOOL -> ToolPreview()
                CarAppCategory.ALL -> GenericPreview(app)
            }
        }
        Spacer(Modifier.height(dimens.gapSmall))
        Text(
            "这是 UI 预览；接入 CarLife 后显示真实应用画面。",
            style = LocalCarTypography.current.caption,
            color = palette.textTertiary,
        )
    }
}

@Composable
private fun NaviPreview() {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Box(modifier = Modifier.fillMaxSize()) {
        CarMapCanvas(modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(dimens.gap),
        ) {
            CarBadge("去公司", palette.primary)
            Spacer(Modifier.height(dimens.gapSmall))
            Text("12 分钟 · 6.4 公里", style = LocalCarTypography.current.cardTitle, color = palette.textPrimary)
            Spacer(Modifier.height(dimens.gapSmall))
            CarPrimaryButton("开始导航", {}, glyph = CarGlyph.NAVI)
        }
    }
}

@Composable
private fun MusicPreview() {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(dimens.gap)) {
        AlbumArt(Color(0xFF2F6BFF), 160.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text("夜航星", style = LocalCarTypography.current.title, color = palette.textPrimary)
            Text("不才 · 夜航星", style = LocalCarTypography.current.bodySmall, color = palette.textSecondary)
            Spacer(Modifier.height(dimens.gap))
            CarProgress(0.42f)
            Spacer(Modifier.height(dimens.gap))
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.gap)) {
                CarIconButton(CarGlyph.PREV, "上一首", {})
                CarIconButton(CarGlyph.PLAY, "播放", {})
                CarIconButton(CarGlyph.NEXT, "下一首", {})
            }
        }
    }
}

@Composable
private fun VideoPreview(app: CarAppEntry) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(dimens.cardRadiusSmall))
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            CarIcon(CarGlyph.PLAY, size = 56.dp, tint = Color.White)
            CarBadge("16:9 · 1080p", app.tint, Modifier.align(Alignment.TopStart).padding(dimens.gapSmall))
        }
        Spacer(Modifier.height(dimens.gap))
        Text("视频标题 · ${app.label}", style = LocalCarTypography.current.cardTitle, color = palette.textPrimary)
        Text("停车时可用；行驶中默认仅音频。", style = LocalCarTypography.current.caption, color = palette.textSecondary)
        Spacer(Modifier.height(dimens.gapSmall))
        Row(horizontalArrangement = Arrangement.spacedBy(dimens.gapSmall)) {
            CarBadge("第 1 集", app.tint)
            CarBadge("第 2 集", palette.textTertiary)
            CarBadge("第 3 集", palette.textTertiary)
        }
    }
}

@Composable
private fun ChatPreview() {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    LazyColumn(verticalArrangement = Arrangement.spacedBy(dimens.gapSmall)) {
        items(6) { index ->
            val incoming = index % 2 == 0
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (incoming) Arrangement.Start else Arrangement.End,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (incoming) palette.surfaceElevated else palette.primary)
                        .padding(horizontal = dimens.padSmall, vertical = dimens.padSmall),
                ) {
                    Text(
                        if (incoming) "晚上一起吃饭吗？我订了位置。" else "好，我 6 点到。",
                        style = LocalCarTypography.current.bodySmall,
                        color = if (incoming) palette.textPrimary else palette.onPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolPreview() {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    val items = listOf(
        "连接" to CarGlyph.USB,
        "显示" to CarGlyph.BRIGHTNESS,
        "声音" to CarGlyph.VOLUME,
        "应用" to CarGlyph.APPS,
        "语音" to CarGlyph.MIC,
        "关于" to CarGlyph.INFO,
    )
    Column(verticalArrangement = Arrangement.spacedBy(dimens.gapSmall)) {
        items.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.gapSmall)) {
                row.forEach { (label, glyph) ->
                    GlassCard(modifier = Modifier.weight(1f)) {
                        CarIcon(glyph, size = dimens.iconLarge, tint = palette.primary)
                        Spacer(Modifier.height(dimens.gapSmall))
                        Text(label, style = LocalCarTypography.current.body, color = palette.textPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun GenericPreview(app: CarAppEntry) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Column(verticalArrangement = Arrangement.spacedBy(dimens.gapSmall)) {
        repeat(3) { index ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "${app.label} · 内容 ${index + 1}",
                    style = LocalCarTypography.current.body,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text("真实投屏后显示应用内容", style = LocalCarTypography.current.caption, color = palette.textSecondary)
            }
        }
    }
}
