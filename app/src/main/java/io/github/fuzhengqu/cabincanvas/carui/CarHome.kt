package io.github.fuzhengqu.cabincanvas.carui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.fuzhengqu.cabincanvas.core.CarDestination
import io.github.fuzhengqu.cabincanvas.core.CarDockMode
import io.github.fuzhengqu.cabincanvas.core.CarSizeClass
import io.github.fuzhengqu.cabincanvas.core.CarViewport
import io.github.fuzhengqu.cabincanvas.data.CarForecast
import io.github.fuzhengqu.cabincanvas.data.CarGlyph
import io.github.fuzhengqu.cabincanvas.data.CarMedia
import io.github.fuzhengqu.cabincanvas.data.CarNotification
import io.github.fuzhengqu.cabincanvas.data.CarRoute
import io.github.fuzhengqu.cabincanvas.data.CarSuggestion
import io.github.fuzhengqu.cabincanvas.data.CarWeather
import io.github.fuzhengqu.cabincanvas.data.FakeCarData
import io.github.fuzhengqu.cabincanvas.designsystem.CarBadge
import io.github.fuzhengqu.cabincanvas.designsystem.CarDock
import io.github.fuzhengqu.cabincanvas.designsystem.CarGhostButton
import io.github.fuzhengqu.cabincanvas.designsystem.CarIcon
import io.github.fuzhengqu.cabincanvas.designsystem.CarIconButton
import io.github.fuzhengqu.cabincanvas.designsystem.CarPrimaryButton
import io.github.fuzhengqu.cabincanvas.designsystem.CarProgress
import io.github.fuzhengqu.cabincanvas.designsystem.CarStatusBar
import io.github.fuzhengqu.cabincanvas.designsystem.GlassCard
import io.github.fuzhengqu.cabincanvas.designsystem.LocalCarDimens
import io.github.fuzhengqu.cabincanvas.designsystem.LocalCarPalette
import io.github.fuzhengqu.cabincanvas.designsystem.LocalCarTypography
import io.github.fuzhengqu.cabincanvas.designsystem.PageDots
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CarHome(
    viewport: CarViewport,
    media: CarMedia,
    isPlaying: Boolean,
    progress: Float,
    weather: CarWeather,
    route: CarRoute,
    suggestions: List<CarSuggestion>,
    notifications: List<CarNotification>,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onOpenMedia: () -> Unit,
    onOpenApps: () -> Unit,
    onOpenPhone: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenNavi: () -> Unit,
    onOpenNotifications: () -> Unit,
    onVoice: () -> Unit,
) {
    val dimens = LocalCarDimens.current
    Column(modifier = Modifier.fillMaxSize()) {
        CarStatusBar(weather = weather)
        if (viewport.layout.dockMode == CarDockMode.LEFT) {
            Row(modifier = Modifier.weight(1f)) {
                CarDock(
                    viewport = viewport,
                    selected = CarDestination.HOME,
                    onSelect = { destination ->
                        when (destination) {
                            CarDestination.HOME -> Unit
                            CarDestination.APPS -> onOpenApps()
                            CarDestination.MEDIA -> onOpenMedia()
                            CarDestination.PHONE -> onOpenPhone()
                            CarDestination.SETTINGS -> onOpenSettings()
                            CarDestination.VOICE -> onVoice()
                        }
                    },
                )
                HomeContent(
                    viewport = viewport,
                    media = media,
                    isPlaying = isPlaying,
                    progress = progress,
                    route = route,
                    suggestions = suggestions,
                    notifications = notifications,
                    onTogglePlay = onTogglePlay,
                    onNext = onNext,
                    onPrev = onPrev,
                    onOpenMedia = onOpenMedia,
                    onOpenNavi = onOpenNavi,
                    onOpenNotifications = onOpenNotifications,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            HomeContent(
                viewport = viewport,
                media = media,
                isPlaying = isPlaying,
                progress = progress,
                route = route,
                suggestions = suggestions,
                notifications = notifications,
                onTogglePlay = onTogglePlay,
                onNext = onNext,
                onPrev = onPrev,
                onOpenMedia = onOpenMedia,
                onOpenNavi = onOpenNavi,
                onOpenNotifications = onOpenNotifications,
                modifier = Modifier.weight(1f),
            )
            CarDock(
                viewport = viewport,
                selected = CarDestination.HOME,
                onSelect = { destination ->
                    when (destination) {
                        CarDestination.HOME -> Unit
                        CarDestination.APPS -> onOpenApps()
                        CarDestination.MEDIA -> onOpenMedia()
                        CarDestination.PHONE -> onOpenPhone()
                        CarDestination.SETTINGS -> onOpenSettings()
                        CarDestination.VOICE -> onVoice()
                    }
                },
            )
        }
    }
}

@Composable
private fun HomeContent(
    viewport: CarViewport,
    media: CarMedia,
    isPlaying: Boolean,
    progress: Float,
    route: CarRoute,
    suggestions: List<CarSuggestion>,
    notifications: List<CarNotification>,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onOpenMedia: () -> Unit,
    onOpenNavi: () -> Unit,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = LocalCarDimens.current
    Column(modifier = modifier.padding(horizontal = dimens.pad, vertical = dimens.padSmall)) {
        when (viewport.sizeClass) {
            CarSizeClass.COMPACT -> {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(dimens.gap)) {
                    MapCard(route, onOpenNavi, Modifier.weight(0.62f))
                    MediaCard(media, isPlaying, progress, onTogglePlay, onNext, onPrev, onOpenMedia, Modifier.weight(0.38f), compact = true)
                }
            }
            CarSizeClass.MEDIUM -> {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(dimens.gap)) {
                    MapCard(route, onOpenNavi, Modifier.weight(viewport.layout.mapWeight))
                    Column(
                        modifier = Modifier.weight(1f - viewport.layout.mapWeight),
                        verticalArrangement = Arrangement.spacedBy(dimens.gap),
                    ) {
                        MediaCard(media, isPlaying, progress, onTogglePlay, onNext, onPrev, onOpenMedia, Modifier.weight(0.44f))
                        SuggestionCard(suggestions, Modifier.weight(0.32f))
                        ClockWeatherCard(Modifier.weight(0.24f))
                    }
                }
            }
            CarSizeClass.EXPANDED, CarSizeClass.ULTRA -> {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(dimens.gap)) {
                    MapCard(route, onOpenNavi, Modifier.weight(viewport.layout.mapWeight))
                    Column(
                        modifier = Modifier.weight(0.24f),
                        verticalArrangement = Arrangement.spacedBy(dimens.gap),
                    ) {
                        MediaCard(media, isPlaying, progress, onTogglePlay, onNext, onPrev, onOpenMedia, Modifier.weight(0.52f))
                        SuggestionCard(suggestions, Modifier.weight(0.48f))
                    }
                    Column(
                        modifier = Modifier.weight(0.24f),
                        verticalArrangement = Arrangement.spacedBy(dimens.gap),
                    ) {
                        ClockWeatherCard(Modifier.weight(0.42f))
                        MiniNotifications(notifications, onOpenNotifications, Modifier.weight(0.58f))
                    }
                }
            }
        }
        Spacer(Modifier.height(dimens.gapSmall))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            PageDots(count = viewport.layout.pageCount, selected = 0)
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.gapSmall), verticalAlignment = Alignment.CenterVertically) {
                CarBadge("M0 · UI 预览")
                Text(
                    "1280×720 基准 · 当前 ${viewport.widthPx}×${viewport.heightPx}",
                    style = LocalCarTypography.current.caption,
                    color = LocalCarPalette.current.textTertiary,
                )
            }
        }
    }
}

@Composable
fun MapCard(route: CarRoute, onNavigate: () -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    GlassCard(modifier = modifier.fillMaxSize(), contentPadding = 0.dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            CarMapCanvas(modifier = Modifier.fillMaxSize())
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(dimens.cardPadding),
            ) {
                CarBadge("导航中", palette.success)
                Spacer(Modifier.height(dimens.gapSmall))
                Text(
                    route.destination,
                    style = LocalCarTypography.current.title,
                    color = palette.textPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "${route.etaMinutes} 分钟 · ${route.distanceKm} 公里",
                    style = LocalCarTypography.current.bodySmall,
                    color = palette.textSecondary,
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(dimens.cardPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("预计到达 15:42", style = LocalCarTypography.current.caption, color = palette.textSecondary)
                    CarProgress(progress = route.progress, modifier = Modifier.width(180.dp))
                }
                CarPrimaryButton(text = "开始导航", onClick = onNavigate, glyph = CarGlyph.NAVI)
            }
        }
    }
}

@Composable
fun CarMapCanvas(modifier: Modifier = Modifier) {
    val palette = LocalCarPalette.current
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawRect(color = palette.mapLand)
        // City blocks
        val blocks = listOf(
            floatArrayOf(0.05f, 0.10f, 0.18f, 0.16f),
            floatArrayOf(0.28f, 0.06f, 0.20f, 0.20f),
            floatArrayOf(0.55f, 0.12f, 0.14f, 0.14f),
            floatArrayOf(0.76f, 0.06f, 0.18f, 0.18f),
            floatArrayOf(0.08f, 0.40f, 0.16f, 0.18f),
            floatArrayOf(0.30f, 0.34f, 0.22f, 0.12f),
            floatArrayOf(0.60f, 0.40f, 0.12f, 0.22f),
            floatArrayOf(0.80f, 0.34f, 0.14f, 0.16f),
            floatArrayOf(0.12f, 0.70f, 0.24f, 0.16f),
            floatArrayOf(0.44f, 0.72f, 0.18f, 0.18f),
            floatArrayOf(0.70f, 0.68f, 0.20f, 0.22f),
        )
        blocks.forEach { b ->
            drawRoundRect(
                color = palette.mapBlock,
                topLeft = Offset(w * b[0], h * b[1]),
                size = Size(w * b[2], h * b[3]),
                cornerRadius = CornerRadius(10.dp.toPx()),
            )
        }
        // Roads
        val road = palette.mapRoad.copy(alpha = 0.9f)
        drawLine(road, Offset(0f, h * 0.30f), Offset(w, h * 0.26f), strokeWidth = 8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(road, Offset(0f, h * 0.62f), Offset(w, h * 0.58f), strokeWidth = 10.dp.toPx(), cap = StrokeCap.Round)
        drawLine(road, Offset(w * 0.22f, 0f), Offset(w * 0.28f, h), strokeWidth = 7.dp.toPx(), cap = StrokeCap.Round)
        drawLine(road, Offset(w * 0.66f, 0f), Offset(w * 0.60f, h), strokeWidth = 7.dp.toPx(), cap = StrokeCap.Round)
        // Route
        val path = Path().apply {
            moveTo(w * 0.16f, h * 0.86f)
            cubicTo(w * 0.28f, h * 0.68f, w * 0.38f, h * 0.70f, w * 0.46f, h * 0.52f)
            cubicTo(w * 0.54f, h * 0.34f, w * 0.66f, h * 0.38f, w * 0.76f, h * 0.22f)
        }
        drawPath(path, palette.mapRoute.copy(alpha = 0.26f), style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round))
        drawPath(path, palette.mapRoute, style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round))
        // Destination pin
        drawCircle(palette.primary, radius = 11.dp.toPx(), center = Offset(w * 0.76f, h * 0.22f))
        drawCircle(palette.onPrimary, radius = 5.dp.toPx(), center = Offset(w * 0.76f, h * 0.22f))
        // Car marker
        drawCircle(palette.success.copy(alpha = 0.24f), radius = 15.dp.toPx(), center = Offset(w * 0.46f, h * 0.52f))
        drawCircle(palette.success, radius = 8.dp.toPx(), center = Offset(w * 0.46f, h * 0.52f))
        drawCircle(palette.onPrimary, radius = 3.dp.toPx(), center = Offset(w * 0.46f, h * 0.52f))
    }
}

@Composable
fun MediaCard(
    media: CarMedia,
    isPlaying: Boolean,
    progress: Float,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    GlassCard(modifier = modifier.fillMaxSize(), onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AlbumArt(media.accent, size = if (compact) 52.dp else 66.dp)
            Spacer(Modifier.width(dimens.gap))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    media.title,
                    style = LocalCarTypography.current.cardTitle,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    media.artist,
                    style = LocalCarTypography.current.bodySmall,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!compact) {
                    Spacer(Modifier.height(dimens.gapSmall))
                    CarBadge("正在播放", palette.success)
                }
            }
        }
        Spacer(Modifier.height(dimens.gap))
        CarProgress(progress)
        Spacer(Modifier.height(dimens.gapSmall))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CarIconButton(CarGlyph.PREV, "上一首", onPrev, modifier = Modifier.size(dimens.touch - 8.dp))
            CarIconButton(
                if (isPlaying) CarGlyph.PAUSE else CarGlyph.PLAY,
                if (isPlaying) "暂停" else "播放",
                onTogglePlay,
                modifier = Modifier
                    .size(dimens.touch + 4.dp)
                    .clip(CircleShape)
                    .background(palette.primary),
                tint = palette.onPrimary,
            )
            CarIconButton(CarGlyph.NEXT, "下一首", onNext, modifier = Modifier.size(dimens.touch - 8.dp))
        }
        Spacer(Modifier.weight(1f))
        Text("下一首", style = LocalCarTypography.current.caption, color = palette.textTertiary)
        Spacer(Modifier.height(dimens.gapSmall))
        val next = FakeCarData.media.firstOrNull { it.title != media.title }
        if (next != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AlbumArt(next.accent, 30.dp)
                Spacer(Modifier.width(dimens.gapSmall))
                Column {
                    Text(next.title, style = LocalCarTypography.current.bodySmall, color = palette.textPrimary, maxLines = 1)
                    Text(next.artist, style = LocalCarTypography.current.caption, color = palette.textSecondary, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun AlbumArt(accent: Color, size: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    val palette = LocalCarPalette.current
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(accent.copy(alpha = 0.95f), palette.surfaceElevated, accent.copy(alpha = 0.55f)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        CarIcon(CarGlyph.MUSIC, size = size * 0.38f, tint = Color.White.copy(alpha = 0.92f))
    }
}

@Composable
private fun SuggestionCard(suggestions: List<CarSuggestion>, modifier: Modifier = Modifier) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    GlassCard(modifier = modifier.fillMaxSize()) {
        Text("建议", style = LocalCarTypography.current.cardTitle, color = palette.textPrimary)
        Spacer(Modifier.height(dimens.gapSmall))
        suggestions.take(3).forEach { suggestion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimens.gapSmall / 2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(dimens.touch - 8.dp)
                        .clip(CircleShape)
                        .background(suggestion.tint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CarIcon(suggestion.glyph, size = dimens.iconSmall, tint = suggestion.tint)
                }
                Spacer(Modifier.width(dimens.gapSmall))
                Column(modifier = Modifier.weight(1f)) {
                    Text(suggestion.title, style = LocalCarTypography.current.body, color = palette.textPrimary, maxLines = 1)
                    Text(
                        suggestion.subtitle,
                        style = LocalCarTypography.current.caption,
                        color = palette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                CarIcon(CarGlyph.CHEVRON, size = dimens.iconSmall, tint = palette.textTertiary)
            }
        }
    }
}

@Composable
private fun ClockWeatherCard(modifier: Modifier = Modifier) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    val now = java.time.LocalTime.now()
    val date = LocalDate.now().format(DateTimeFormatter.ofPattern("M月d日 EEE"))
    GlassCard(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    now.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = LocalCarTypography.current.display,
                    color = palette.textPrimary,
                )
                Text(date, style = LocalCarTypography.current.caption, color = palette.textSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("26°", style = LocalCarTypography.current.title, color = palette.textPrimary)
                Text("多云", style = LocalCarTypography.current.caption, color = palette.textSecondary)
            }
        }
        Spacer(Modifier.height(dimens.gapSmall))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            FakeCarData.weather.forecast.forEach { forecast: CarForecast ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(forecast.day, style = LocalCarTypography.current.caption, color = palette.textTertiary)
                    Text(forecast.temp, style = LocalCarTypography.current.bodySmall, color = palette.textPrimary)
                    Text(forecast.condition, style = LocalCarTypography.current.caption, color = palette.textSecondary)
                }
            }
        }
    }
}

@Composable
private fun MiniNotifications(
    notifications: List<CarNotification>,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    GlassCard(modifier = modifier.fillMaxSize(), onClick = onOpen) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("通知", style = LocalCarTypography.current.cardTitle, color = palette.textPrimary)
            Spacer(Modifier.weight(1f))
            CarBadge("${notifications.size}")
        }
        Spacer(Modifier.height(dimens.gapSmall))
        notifications.take(3).forEach { notification ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimens.gapSmall / 2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(notification.color),
                )
                Spacer(Modifier.width(dimens.gapSmall))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        notification.title,
                        style = LocalCarTypography.current.bodySmall,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        notification.body,
                        style = LocalCarTypography.current.caption,
                        color = palette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(notification.time, style = LocalCarTypography.current.caption, color = palette.textTertiary)
            }
        }
    }
}
