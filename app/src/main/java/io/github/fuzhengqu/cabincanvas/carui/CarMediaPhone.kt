package io.github.fuzhengqu.cabincanvas.carui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.fuzhengqu.cabincanvas.core.CarViewport
import io.github.fuzhengqu.cabincanvas.data.CarAppCategory
import io.github.fuzhengqu.cabincanvas.data.CarContact
import io.github.fuzhengqu.cabincanvas.data.CarGlyph
import io.github.fuzhengqu.cabincanvas.data.CarMedia
import io.github.fuzhengqu.cabincanvas.data.CarNotification
import io.github.fuzhengqu.cabincanvas.data.FakeCarData
import io.github.fuzhengqu.cabincanvas.designsystem.Avatar
import io.github.fuzhengqu.cabincanvas.designsystem.CarBadge
import io.github.fuzhengqu.cabincanvas.designsystem.CarChip
import io.github.fuzhengqu.cabincanvas.designsystem.CarGhostButton
import io.github.fuzhengqu.cabincanvas.designsystem.CarIcon
import io.github.fuzhengqu.cabincanvas.designsystem.CarIconButton
import io.github.fuzhengqu.cabincanvas.designsystem.CarPrimaryButton
import io.github.fuzhengqu.cabincanvas.designsystem.CarProgress
import io.github.fuzhengqu.cabincanvas.designsystem.EmptyState
import io.github.fuzhengqu.cabincanvas.designsystem.GlassCard
import io.github.fuzhengqu.cabincanvas.designsystem.LocalCarDimens
import io.github.fuzhengqu.cabincanvas.designsystem.LocalCarPalette
import io.github.fuzhengqu.cabincanvas.designsystem.LocalCarTypography
import kotlinx.coroutines.delay

@Composable
fun CarMediaScreen(
    viewport: CarViewport,
    media: CarMedia,
    isPlaying: Boolean,
    progress: Float,
    onBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSeek: (Float) -> Unit,
    onSelectTrack: (Int) -> Unit,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Column(modifier = Modifier.fillMaxSize()) {
        CarTopBar("音乐", onBack, subtitle = "媒体库 · ${media.artist}")
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = dimens.pad, vertical = dimens.padSmall),
            horizontalArrangement = Arrangement.spacedBy(dimens.pad),
        ) {
            Column(modifier = Modifier.weight(0.42f), verticalArrangement = Arrangement.spacedBy(dimens.gap)) {
                AlbumArtLarge(media.accent, Modifier.fillMaxWidth().aspectRatio(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(media.title, style = LocalCarTypography.current.title, color = palette.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${media.artist} · ${media.album}", style = LocalCarTypography.current.bodySmall, color = palette.textSecondary, maxLines = 1)
                    }
                    CarBadge("无损", palette.success)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    CarIconButton(CarGlyph.PREV, "上一首", onPrev)
                    Spacer(Modifier.width(dimens.gap))
                    CarIconButton(
                        if (isPlaying) CarGlyph.PAUSE else CarGlyph.PLAY,
                        "播放",
                        onTogglePlay,
                        modifier = Modifier
                            .size(dimens.touch + 12.dp)
                            .clip(CircleShape)
                            .background(palette.primary),
                        tint = palette.onPrimary,
                    )
                    Spacer(Modifier.width(dimens.gap))
                    CarIconButton(CarGlyph.NEXT, "下一首", onNext)
                }
            }
            Column(modifier = Modifier.weight(0.58f), verticalArrangement = Arrangement.spacedBy(dimens.gap)) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("正在播放", style = LocalCarTypography.current.cardTitle, color = palette.textPrimary)
                    Spacer(Modifier.height(dimens.gapSmall))
                    CarProgress(progress)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatTime((media.durationMs * progress).toLong()), style = LocalCarTypography.current.caption, color = palette.textSecondary)
                        Text(formatTime(media.durationMs), style = LocalCarTypography.current.caption, color = palette.textSecondary)
                    }
                    Slider(
                        value = progress,
                        onValueChange = onSeek,
                        colors = SliderDefaults.colors(
                            thumbColor = palette.primary,
                            activeTrackColor = palette.primary,
                            inactiveTrackColor = palette.textTertiary.copy(alpha = 0.25f),
                        ),
                    )
                }
                GlassCard(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Text("歌词", style = LocalCarTypography.current.cardTitle, color = palette.textPrimary)
                    Spacer(Modifier.height(dimens.gapSmall))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(dimens.gapSmall)) {
                        items(FakeCarData.lyrics.size) { index ->
                            Text(
                                FakeCarData.lyrics[index],
                                style = if (index == 0) LocalCarTypography.current.cardTitle else LocalCarTypography.current.body,
                                color = if (index == 0) palette.primary else palette.textSecondary,
                            )
                        }
                    }
                }
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("播放队列", style = LocalCarTypography.current.cardTitle, color = palette.textPrimary)
                    Spacer(Modifier.height(dimens.gapSmall))
                    FakeCarData.media.forEachIndexed { index, track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(dimens.cardRadiusSmall))
                                .clickable { onSelectTrack(index) }
                                .padding(vertical = dimens.gapSmall),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AlbumArt(track.accent, 36.dp)
                            Spacer(Modifier.width(dimens.gapSmall))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(track.title, style = LocalCarTypography.current.bodySmall, color = palette.textPrimary, maxLines = 1)
                                Text(track.artist, style = LocalCarTypography.current.caption, color = palette.textSecondary, maxLines = 1)
                            }
                            if (track == media) CarBadge("播放中", palette.success)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumArtLarge(accent: Color, modifier: Modifier = Modifier) {
    val palette = LocalCarPalette.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(LocalCarDimens.current.cardRadius))
            .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.95f), palette.surfaceElevated, accent.copy(alpha = 0.45f)))),
        contentAlignment = Alignment.Center,
    ) {
        CarIcon(CarGlyph.MUSIC, size = 84.dp, tint = Color.White.copy(alpha = 0.9f))
    }
}

@Composable
fun CarPhoneScreen(
    viewport: CarViewport,
    contacts: List<CarContact>,
    onBack: () -> Unit,
    onCall: (CarContact) -> Unit,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    var tab by remember { mutableIntStateOf(0) }
    var number by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<CarContact?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        CarTopBar("电话", onBack, subtitle = "最近通话 · 联系人 · 拨号")
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = dimens.pad),
            horizontalArrangement = Arrangement.spacedBy(dimens.gapSmall),
        ) {
            listOf("最近", "联系人", "拨号").forEachIndexed { index, label ->
                CarChip(label, tab == index, onClick = { tab = index })
            }
        }
        Spacer(Modifier.height(dimens.gapSmall))
        when (tab) {
            0 -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = dimens.pad),
                verticalArrangement = Arrangement.spacedBy(dimens.gapSmall),
            ) {
                items(contacts) { contact ->
                    ContactRow(contact, "今天 09:41 · 已接听", onCall = { onCall(contact) })
                }
            }
            1 -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = dimens.pad),
                verticalArrangement = Arrangement.spacedBy(dimens.gapSmall),
            ) {
                items(contacts) { contact ->
                    ContactRow(contact, contact.number, onCall = { onCall(contact) })
                }
            }
            else -> Dialer(number, onChange = { number = it }, onCall = {
                val contact = contacts.firstOrNull() ?: FakeCarData.contacts.first()
                onCall(contact.copy(name = if (number.isBlank()) contact.name else number, number = number.ifBlank { contact.number }))
            })
        }
    }

    val contact = selected
    if (contact != null) {
        AlertDialog(
            onDismissRequest = { selected = null },
            containerColor = palette.surfaceElevated,
            title = { Text(contact.name, color = palette.textPrimary) },
            text = { Text(contact.number, color = palette.textSecondary) },
            confirmButton = { TextButton(onClick = { selected = null; onCall(contact) }) { Text("呼叫", color = palette.primary) } },
            dismissButton = { TextButton(onClick = { selected = null }) { Text("取消", color = palette.textSecondary) } },
        )
    }
}

@Composable
private fun ContactRow(contact: CarContact, subtitle: String, onCall: () -> Unit) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = dimens.padSmall) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(contact.letter, contact.color)
            Spacer(Modifier.width(dimens.gap))
            Column(modifier = Modifier.weight(1f)) {
                Text(contact.name, style = LocalCarTypography.current.body, color = palette.textPrimary, fontWeight = FontWeight.Medium)
                Text(subtitle, style = LocalCarTypography.current.caption, color = palette.textSecondary, maxLines = 1)
            }
            CarIconButton(CarGlyph.CALL, "呼叫 ${contact.name}", onCall, tint = palette.success)
        }
    }
}

@Composable
private fun Dialer(number: String, onChange: (String) -> Unit, onCall: () -> Unit) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#")
    Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = dimens.pad),
        horizontalArrangement = Arrangement.spacedBy(dimens.pad),
    ) {
        Column(modifier = Modifier.weight(0.42f), verticalArrangement = Arrangement.spacedBy(dimens.gapSmall)) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    number.ifBlank { "输入号码" },
                    style = LocalCarTypography.current.title,
                    color = if (number.isBlank()) palette.textTertiary else palette.textPrimary,
                    maxLines = 1,
                )
            }
            keys.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(dimens.gapSmall)) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(dimens.touch + 8.dp)
                                .clip(RoundedCornerShape(dimens.cardRadiusSmall))
                                .background(palette.surfaceElevated.copy(alpha = 0.75f))
                                .border(1.dp, palette.outline, RoundedCornerShape(dimens.cardRadiusSmall))
                                .clickable { onChange(number + key) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(key, style = LocalCarTypography.current.title, color = palette.textPrimary)
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.gapSmall)) {
                CarGhostButton("删除", { if (number.isNotEmpty()) onChange(number.dropLast(1)) }, Modifier.weight(1f))
                CarPrimaryButton("呼叫", onCall, Modifier.weight(1f), CarGlyph.CALL)
            }
        }
        Column(modifier = Modifier.weight(0.58f), verticalArrangement = Arrangement.spacedBy(dimens.gapSmall)) {
            Text("常用联系人", style = LocalCarTypography.current.cardTitle, color = palette.textPrimary)
            FakeCarData.contacts.take(4).forEach { contact ->
                ContactRow(contact, contact.number, onCall = { onChange(contact.number) })
            }
        }
    }
}

@Composable
fun CarNotificationsScreen(
    notifications: List<CarNotification>,
    onBack: () -> Unit,
    onClear: () -> Unit,
    onReply: (CarNotification) -> Unit,
    onSimulate: () -> Unit,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Column(modifier = Modifier.fillMaxSize()) {
        CarTopBar(
            "通知",
            onBack,
            subtitle = "${notifications.size} 条 · 驾驶中仅显示高优先级",
            actions = {
                CarGhostButton("模拟", onSimulate)
                CarGhostButton("清空", onClear)
            },
        )
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(CarGlyph.NOTIFICATION, "没有新通知", "导航、电话和日程会优先显示在这里")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = dimens.pad),
                verticalArrangement = Arrangement.spacedBy(dimens.gapSmall),
            ) {
                items(notifications, key = { it.title + it.time }) { notification ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(notification.color))
                            Spacer(Modifier.width(dimens.gapSmall))
                            Text(notification.app, style = LocalCarTypography.current.caption, color = palette.textSecondary)
                            Spacer(Modifier.weight(1f))
                            Text(notification.time, style = LocalCarTypography.current.caption, color = palette.textTertiary)
                        }
                        Spacer(Modifier.height(dimens.gapSmall))
                        Text(notification.title, style = LocalCarTypography.current.body, color = palette.textPrimary, fontWeight = FontWeight.Medium)
                        Text(notification.body, style = LocalCarTypography.current.bodySmall, color = palette.textSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        if (notification.canReply) {
                            Spacer(Modifier.height(dimens.gapSmall))
                            CarGhostButton("快捷回复", { onReply(notification) }, glyph = CarGlyph.CHAT)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceOverlay(onCommand: (String) -> Unit, onDismiss: () -> Unit) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    val transition = rememberInfiniteTransition(label = "voice")
    val pulse by transition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "pulse",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.scrim)
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(150.dp * pulse)
                        .clip(CircleShape)
                        .border(2.dp, palette.primary.copy(alpha = 0.25f), CircleShape),
                )
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .clip(CircleShape)
                        .background(palette.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    CarIcon(CarGlyph.MIC, size = 48.dp, tint = palette.onPrimary)
                }
            }
            Spacer(Modifier.height(dimens.gap))
            Text("我在听", style = LocalCarTypography.current.title, color = palette.textPrimary)
            Text("说“导航回家”“播放音乐”或“给妈妈打电话”", style = LocalCarTypography.current.bodySmall, color = palette.textSecondary)
            Spacer(Modifier.height(dimens.gap))
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.gapSmall)) {
                FakeCarData.voiceCommands.take(3).forEach { command ->
                    CarChip(command, selected = false, onClick = { onCommand(command) })
                }
            }
            Spacer(Modifier.height(dimens.gap))
            CarGhostButton("取消", onDismiss)
        }
    }
}

@Composable
fun IncomingCallOverlay(contact: CarContact, onAnswer: () -> Unit, onDecline: () -> Unit) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Box(
        modifier = Modifier.fillMaxSize().background(palette.scrim),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Avatar(contact.letter, contact.color, size = 96.dp)
            Spacer(Modifier.height(dimens.gap))
            Text(contact.name, style = LocalCarTypography.current.display, color = palette.textPrimary)
            Text(contact.number, style = LocalCarTypography.current.body, color = palette.textSecondary)
            Text("来电", style = LocalCarTypography.current.caption, color = palette.success)
            Spacer(Modifier.height(dimens.pad))
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.pad)) {
                CallCircle(CarGlyph.CALL_END, palette.danger, "挂断", onDecline)
                CallCircle(CarGlyph.CALL, palette.success, "接听", onAnswer)
            }
        }
    }
}

@Composable
private fun CallCircle(glyph: CarGlyph, color: Color, label: String, onClick: () -> Unit) {
    val dimens = LocalCarDimens.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(dimens.touch + 28.dp)
                .clip(CircleShape)
                .background(color)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            CarIcon(glyph, size = dimens.iconLarge, tint = Color.White)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = LocalCarTypography.current.caption, color = LocalCarPalette.current.textSecondary)
    }
}

@Composable
fun InCallOverlay(contact: CarContact, onHangup: () -> Unit) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    var seconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            seconds += 1
        }
    }
    Box(modifier = Modifier.fillMaxSize().background(palette.scrim), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Avatar(contact.letter, contact.color, size = 96.dp)
            Spacer(Modifier.height(dimens.gap))
            Text(contact.name, style = LocalCarTypography.current.display, color = palette.textPrimary)
            Text("%02d:%02d".format(seconds / 60, seconds % 60), style = LocalCarTypography.current.body, color = palette.textSecondary)
            Spacer(Modifier.height(dimens.pad))
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.gap)) {
                CarIconButton(CarGlyph.MUTE, "静音", {})
                CarIconButton(CarGlyph.SPEAKER, "免提", {})
                CallCircle(CarGlyph.CALL_END, palette.danger, "挂断", onHangup)
            }
        }
    }
}

@Composable
fun NotificationToast(notification: CarNotification, onDismiss: () -> Unit) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Box(modifier = Modifier.fillMaxWidth().padding(dimens.pad), contentAlignment = Alignment.TopCenter) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(0.62f),
            onClick = onDismiss,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(notification.color))
                Spacer(Modifier.width(dimens.gapSmall))
                Text(notification.app, style = LocalCarTypography.current.caption, color = palette.textSecondary)
                Spacer(Modifier.weight(1f))
                Text(notification.time, style = LocalCarTypography.current.caption, color = palette.textTertiary)
            }
            Spacer(Modifier.height(dimens.gapSmall))
            Text(notification.title, style = LocalCarTypography.current.body, color = palette.textPrimary, fontWeight = FontWeight.Medium)
            Text(notification.body, style = LocalCarTypography.current.bodySmall, color = palette.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun formatTime(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(total / 60, total % 60)
}
