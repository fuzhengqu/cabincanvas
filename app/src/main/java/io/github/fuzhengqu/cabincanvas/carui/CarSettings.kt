package io.github.fuzhengqu.cabincanvas.carui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import io.github.fuzhengqu.cabincanvas.core.CarLayoutMode
import io.github.fuzhengqu.cabincanvas.core.CarSizeClass
import io.github.fuzhengqu.cabincanvas.core.CarViewport
import io.github.fuzhengqu.cabincanvas.data.CarCardConfig
import io.github.fuzhengqu.cabincanvas.data.CarCardSize
import io.github.fuzhengqu.cabincanvas.data.CarCardType
import io.github.fuzhengqu.cabincanvas.data.CarGlyph
import io.github.fuzhengqu.cabincanvas.designsystem.CarBadge
import io.github.fuzhengqu.cabincanvas.designsystem.CarChip
import io.github.fuzhengqu.cabincanvas.designsystem.CarGhostButton
import io.github.fuzhengqu.cabincanvas.designsystem.CarIcon
import io.github.fuzhengqu.cabincanvas.designsystem.CarPrimaryButton
import io.github.fuzhengqu.cabincanvas.designsystem.CarThemeMode
import io.github.fuzhengqu.cabincanvas.designsystem.CarWallpaper
import io.github.fuzhengqu.cabincanvas.designsystem.GlassCard
import io.github.fuzhengqu.cabincanvas.designsystem.LocalCarDimens
import io.github.fuzhengqu.cabincanvas.designsystem.LocalCarPalette
import io.github.fuzhengqu.cabincanvas.designsystem.LocalCarTypography
import io.github.fuzhengqu.cabincanvas.designsystem.SettingRow
import io.github.fuzhengqu.cabincanvas.designsystem.carWallpaperBrush

@Composable
fun CarSettingsScreen(
    viewport: CarViewport,
    themeMode: CarThemeMode,
    wallpaper: CarWallpaper,
    layoutMode: CarLayoutMode,
    onBack: () -> Unit,
    onTheme: () -> Unit,
    onWallpaper: () -> Unit,
    onLayoutMode: (CarLayoutMode) -> Unit,
    onCardManager: () -> Unit,
    onCalibration: () -> Unit,
    onDiagnostics: () -> Unit,
    onOnboarding: () -> Unit,
    onSimulateNotification: () -> Unit,
    notificationAccess: Boolean = false,
    phoneAccess: Boolean = false,
    onOpenNotificationAccess: () -> Unit = {},
    onRequestPhonePermissions: () -> Unit = {},
    onRefreshData: () -> Unit = {},
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Column(modifier = Modifier.fillMaxSize()) {
        CarTopBar("设置", onBack, subtitle = "CabinCanvas v${io.github.fuzhengqu.cabincanvas.BuildConfig.VERSION_NAME}")
        LazyVerticalGrid(
            columns = GridCells.Fixed(
                if (viewport.sizeClass == CarSizeClass.EXPANDED || viewport.sizeClass == CarSizeClass.ULTRA) 2 else 1,
            ),
            modifier = Modifier.fillMaxSize().padding(horizontal = dimens.pad),
            horizontalArrangement = Arrangement.spacedBy(dimens.gap),
            verticalArrangement = Arrangement.spacedBy(dimens.gap),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = dimens.pad),
        ) {
            item {
                SettingsGroup("数据源") {
                    SettingRow(
                        CarGlyph.NOTIFICATION,
                        "通知使用权",
                        if (notificationAccess) "已授权 · 通知与媒体会话可用" else "未授权 · 通知/媒体使用示例数据",
                        trailing = if (notificationAccess) "已开启" else "去授权",
                        onClick = onOpenNotificationAccess,
                    )
                    SettingRow(
                        CarGlyph.PHONE,
                        "电话与联系人",
                        if (phoneAccess) "已授权 · 联系人与通话状态可用" else "未授权 · 使用示例联系人",
                        trailing = if (phoneAccess) "已开启" else "授权",
                        onClick = onRequestPhonePermissions,
                    )
                    SettingRow(
                        CarGlyph.REFRESH,
                        "刷新媒体会话",
                        "在手机上播放音乐后点此刷新",
                        trailing = "刷新",
                        onClick = onRefreshData,
                    )
                }
            }
            item {
                SettingsGroup("连接") {
                    SettingRow(CarGlyph.USB, "连接方式", "USB 有线", trailing = "已连接", onClick = onDiagnostics)
                    SettingRow(CarGlyph.WIFI, "无线 CarLife", "需要车机支持", switch = true, onClick = {})
                    SettingRow(CarGlyph.LINK, "自动连接", "检测到车机后自动启动", switch = true, onClick = {})
                    SettingRow(CarGlyph.INFO, "CarLife 引擎", "com.baidu.carlife.oppo 8.8.4", trailing = "查看", onClick = onDiagnostics)
                }
            }
            item {
                SettingsGroup("显示") {
                    SettingRow(
                        CarGlyph.GRID,
                        "桌面布局",
                        "经典左 Dock / 融合底部胶囊 / 自动",
                        trailing = layoutMode.label(),
                        onClick = { onLayoutMode(nextLayout(layoutMode)) },
                    )
                    SettingRow(CarGlyph.DARK, "主题", "深空 / 晨雾 / 自动", trailing = themeMode.label(), onClick = onTheme)
                    SettingRow(CarGlyph.WALLPAPER, "壁纸", wallpaper.label, trailing = "更换", onClick = onWallpaper)
                    SettingRow(CarGlyph.FULLSCREEN, "分辨率", "跟随车机 · ${viewport.widthPx}×${viewport.heightPx}", trailing = "${viewport.densityDpi}dpi")
                    SettingRow(CarGlyph.TUNE, "字体缩放", "0.82× – 1.40×", trailing = "%.2f×".format(viewport.layout.typeScale))
                }
            }
            item {
                SettingsGroup("声音") {
                    SettingRow(CarGlyph.VOLUME, "音频通道", "USB 优先，失败切蓝牙 A2DP", switch = true, onClick = {})
                    SettingRow(CarGlyph.QUEUE, "歌词", "支持时显示同步歌词", switch = true, onClick = {})
                    SettingRow(CarGlyph.SPEAKER, "导航音量", "播报时自动压低媒体", trailing = "70%")
                }
            }
            item {
                SettingsGroup("应用") {
                    SettingRow(CarGlyph.APPS, "投屏管理", "全屏 / 兼容 / 手机比例", trailing = "管理")
                    SettingRow(CarGlyph.NAVI, "默认导航", "高德地图", trailing = "更改")
                    SettingRow(CarGlyph.MUSIC, "默认音乐", "网易云音乐", trailing = "更改")
                }
            }
            item {
                SettingsGroup("语音与通知") {
                    SettingRow(CarGlyph.MIC, "语音助手", "方向盘语音键 / Dock 麦克风", switch = true, onClick = {})
                    SettingRow(CarGlyph.NOTIFICATION, "通知同步", "微信、短信、日程", switch = true, onClick = {})
                    SettingRow(CarGlyph.NOTIFICATION, "驾驶中仅高优先级", "导航 / 电话 / 日程", switch = true, onClick = {})
                    SettingRow(CarGlyph.BUG, "模拟通知", "测试通知 Toast 与列表", trailing = "触发", onClick = onSimulateNotification)
                }
            }
            item {
                SettingsGroup("桌面") {
                    SettingRow(CarGlyph.STAR, "卡片管理", "添加 / 删除 / 排序 / 尺寸", trailing = "打开", onClick = onCardManager)
                    SettingRow(CarGlyph.CALIBRATE, "投屏校准", "四边安全区 / overscan", trailing = "校准", onClick = onCalibration)
                    SettingRow(CarGlyph.INFO, "重新查看引导", "权限、引擎、连接、主题", trailing = "打开", onClick = onOnboarding)
                }
            }
            item {
                SettingsGroup("关于") {
                    SettingRow(CarGlyph.INFO, "版本", "v${io.github.fuzhengqu.cabincanvas.BuildConfig.VERSION_NAME}", trailing = "GPL-3.0")
                    SettingRow(CarGlyph.BUG, "诊断", "流分辨率、帧率、触摸、日志", trailing = "打开", onClick = onDiagnostics)
                }
            }
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Column(verticalArrangement = Arrangement.spacedBy(dimens.gapSmall)) {
        Text(title, style = LocalCarTypography.current.caption, color = palette.textTertiary, fontWeight = FontWeight.SemiBold)
        GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = dimens.padSmall) {
            content()
        }
    }
}

@Composable
fun CarThemePickerScreen(
    viewport: CarViewport,
    themeMode: CarThemeMode,
    wallpaper: CarWallpaper,
    onBack: () -> Unit,
    onTheme: (CarThemeMode) -> Unit,
    onWallpaper: (CarWallpaper) -> Unit,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Column(modifier = Modifier.fillMaxSize()) {
        CarTopBar("主题与壁纸", onBack, subtitle = "深空 / 晨雾 / 暖阳")
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = dimens.pad), verticalArrangement = Arrangement.spacedBy(dimens.gap)) {
            Text("主题", style = LocalCarTypography.current.cardTitle, color = palette.textPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.gap)) {
                CarThemeMode.entries.forEach { mode ->
                    ThemeCard(mode, themeMode == mode, Modifier.weight(1f)) { onTheme(mode) }
                }
            }
            Spacer(Modifier.height(dimens.gapSmall))
            Text("壁纸", style = LocalCarTypography.current.cardTitle, color = palette.textPrimary)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(dimens.gap),
                verticalArrangement = Arrangement.spacedBy(dimens.gap),
            ) {
                items(CarWallpaper.entries) { item ->
                    WallpaperCard(item, wallpaper == item, viewport) { onWallpaper(item) }
                }
            }
        }
    }
}

@Composable
private fun ThemeCard(mode: CarThemeMode, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    val dark = mode != CarThemeMode.LIGHT
    val bg = if (dark) Color(0xFF0B0D12) else Color(0xFFF3F4F6)
    val card = if (dark) Color(0xFF1A1C28) else Color.White
    Box(
        modifier = modifier
            .height(112.dp)
            .clip(RoundedCornerShape(dimens.cardRadiusSmall))
            .background(bg)
            .border(if (selected) 2.dp else 1.dp, if (selected) palette.primary else palette.outline, RoundedCornerShape(dimens.cardRadiusSmall))
            .clickable(onClick = onClick)
            .padding(dimens.padSmall),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(card),
            )
            Spacer(Modifier.height(dimens.gapSmall))
            Text(mode.label(), style = LocalCarTypography.current.body, color = if (dark) Color.White else Color(0xFF111318))
        }
    }
}

@Composable
private fun WallpaperCard(item: CarWallpaper, selected: Boolean, viewport: CarViewport, onClick: () -> Unit) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(dimens.cardRadiusSmall))
            .background(carWallpaperBrush(item, palette))
            .border(if (selected) 2.dp else 1.dp, if (selected) palette.primary else palette.outline, RoundedCornerShape(dimens.cardRadiusSmall))
            .clickable(onClick = onClick)
            .padding(dimens.padSmall),
        contentAlignment = Alignment.BottomStart,
    ) {
        Text(item.label, style = LocalCarTypography.current.bodySmall, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun CarCardManagerScreen(
    cards: List<CarCardConfig>,
    onBack: () -> Unit,
    onToggle: (CarCardType) -> Unit,
    onSize: (CarCardType, CarCardSize) -> Unit,
    onAdd: () -> Unit,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Column(modifier = Modifier.fillMaxSize()) {
        CarTopBar("卡片管理", onBack, subtitle = "最多 8 张 · 长按排序在 M1 接入", actions = {
            CarPrimaryButton("添加卡片", onAdd, glyph = CarGlyph.ADD)
        })
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = dimens.pad),
            verticalArrangement = Arrangement.spacedBy(dimens.gapSmall),
        ) {
            items(cards.size) { index ->
                val card = cards[index]
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(dimens.touch - 6.dp)
                                .clip(RoundedCornerShape(dimens.cardRadiusSmall))
                                .background(palette.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            CarIcon(card.type.glyph, size = dimens.iconSmall, tint = palette.primary)
                        }
                        Spacer(Modifier.width(dimens.gap))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(card.type.label, style = LocalCarTypography.current.body, color = palette.textPrimary)
                            Text(if (card.enabled) "已启用" else "已隐藏", style = LocalCarTypography.current.caption, color = palette.textSecondary)
                        }
                        CarChip("显示", card.enabled, onClick = { onToggle(card.type) })
                    }
                    Spacer(Modifier.height(dimens.gapSmall))
                    Row(horizontalArrangement = Arrangement.spacedBy(dimens.gapSmall)) {
                        CarCardSize.entries.forEach { size ->
                            CarChip(size.label, card.size == size, onClick = { onSize(card.type, size) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CarCalibrationScreen(
    onBack: () -> Unit,
    onSave: (Int) -> Unit,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    var left by remember { mutableFloatStateOf(0f) }
    var top by remember { mutableFloatStateOf(0f) }
    var right by remember { mutableFloatStateOf(0f) }
    var bottom by remember { mutableFloatStateOf(0f) }
    Column(modifier = Modifier.fillMaxSize()) {
        CarTopBar("投屏校准", onBack, subtitle = "车机边缘裁切时调整安全区")
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = dimens.pad),
            horizontalArrangement = Arrangement.spacedBy(dimens.pad),
        ) {
            Column(modifier = Modifier.weight(0.55f), verticalArrangement = Arrangement.spacedBy(dimens.gapSmall)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(dimens.cardRadius))
                        .background(palette.surface)
                        .border(1.dp, palette.outline, RoundedCornerShape(dimens.cardRadius)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = (left * 6).dp,
                                top = (top * 6).dp,
                                end = (right * 6).dp,
                                bottom = (bottom * 6).dp,
                            )
                            .border(2.dp, palette.primary, RoundedCornerShape(dimens.cardRadiusSmall)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("可视安全区", style = LocalCarTypography.current.cardTitle, color = palette.primary)
                    }
                }
                Text(
                    "把四边调到刚好不裁切画面。保存后会应用到投屏容器。",
                    style = LocalCarTypography.current.caption,
                    color = palette.textSecondary,
                )
            }
            Column(modifier = Modifier.weight(0.45f), verticalArrangement = Arrangement.spacedBy(dimens.gapSmall)) {
                CalibrationSlider("左边距", left) { left = it }
                CalibrationSlider("上边距", top) { top = it }
                CalibrationSlider("右边距", right) { right = it }
                CalibrationSlider("下边距", bottom) { bottom = it }
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(dimens.gapSmall)) {
                    CarGhostButton("重置", { left = 0f; top = 0f; right = 0f; bottom = 0f }, Modifier.weight(1f))
                    CarPrimaryButton("保存", { onSave(maxOf(left, top, right, bottom).toInt()) }, Modifier.weight(1f), CarGlyph.CHECK)
                }
            }
        }
    }
}

@Composable
private fun CalibrationSlider(label: String, value: Float, onChange: (Float) -> Unit) {
    val palette = LocalCarPalette.current
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = LocalCarTypography.current.bodySmall, color = palette.textPrimary)
            Text("${value.toInt()}%", style = LocalCarTypography.current.caption, color = palette.textSecondary)
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = 0f..8f,
            steps = 7,
            colors = SliderDefaults.colors(
                thumbColor = palette.primary,
                activeTrackColor = palette.primary,
                inactiveTrackColor = palette.textTertiary.copy(alpha = 0.25f),
            ),
        )
    }
}

@Composable
fun CarDiagnosticsScreen(
    onBack: () -> Unit,
    onOpenProbe: () -> Unit,
    onShareLog: () -> Unit,
    onSimulateNotification: () -> Unit,
    onTestReply: () -> Unit = {},
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    var log by remember {
        mutableStateOf(
            """
            [M0] UI-only preview build
            [engine] com.baidu.carlife.oppo 8.8.4 (273)
            [transport] C / B 待真车选择
            [display] 1280x720 @ 160dpi
            [video] H.264 720p30 · 4 Mbps (simulated)
            [touch] ready
            [audio] USB / BT fallback
            """.trimIndent(),
        )
    }
    Column(modifier = Modifier.fillMaxSize()) {
        CarTopBar("诊断", onBack, subtitle = "M0 链路 · UI 预览")
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = dimens.pad),
            horizontalArrangement = Arrangement.spacedBy(dimens.gap),
        ) {
            Column(modifier = Modifier.weight(0.42f), verticalArrangement = Arrangement.spacedBy(dimens.gapSmall)) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("状态", style = LocalCarTypography.current.cardTitle, color = palette.textPrimary)
                    Spacer(Modifier.height(dimens.gapSmall))
                    DiagRow("引擎", "已安装 8.8.4", palette.success)
                    DiagRow("传输", "C / B 待选", palette.warning)
                    DiagRow("显示", "1280×720 @ 160dpi", palette.primary)
                    DiagRow("触摸", "就绪", palette.success)
                    DiagRow("音频", "USB / BT", palette.primary)
                }
                CarPrimaryButton("打开 M0 探针", onOpenProbe, Modifier.fillMaxWidth(), CarGlyph.BUG)
                CarGhostButton("分享日志", onShareLog, Modifier.fillMaxWidth(), CarGlyph.SHARE)
                CarGhostButton("模拟通知", onSimulateNotification, Modifier.fillMaxWidth(), CarGlyph.NOTIFICATION)
                CarGhostButton("测试通知回复", onTestReply, Modifier.fillMaxWidth(), CarGlyph.CHAT)
            }
            GlassCard(modifier = Modifier.weight(0.58f).fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("日志", style = LocalCarTypography.current.cardTitle, color = palette.textPrimary)
                    Spacer(Modifier.weight(1f))
                    CarBadge("本地", palette.textTertiary)
                }
                Spacer(Modifier.height(dimens.gapSmall))
                Text(log, style = LocalCarTypography.current.mono, color = palette.textSecondary, modifier = Modifier.weight(1f))
                Spacer(Modifier.height(dimens.gapSmall))
                Row(horizontalArrangement = Arrangement.spacedBy(dimens.gapSmall)) {
                    CarGhostButton("清空", { log = "" }, Modifier.weight(1f))
                    CarGhostButton("追加", { log += "\n[${java.time.LocalTime.now()}] event: simulated" }, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DiagRow(label: String, value: String, color: Color) {
    val palette = LocalCarPalette.current
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(7.dp).clip(RoundedCornerShape(50)).background(color))
        Spacer(Modifier.width(8.dp))
        Text(label, style = LocalCarTypography.current.caption, color = palette.textSecondary, modifier = Modifier.width(48.dp))
        Text(value, style = LocalCarTypography.current.caption, color = palette.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun CarOnboardingScreen(
    onFinish: () -> Unit,
    onSkip: () -> Unit,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    var step by remember { mutableIntStateOf(0) }
    val steps = listOf(
        Triple("欢迎来到 CabinCanvas", "把手机应用画到车机大屏，保留 CarLife 原车入口。", CarGlyph.CAR),
        Triple("权限与引擎", "通知使用权、联系人、电话、麦克风按需授权；百度 CarLife+ 组件由你自行安装。", CarGlyph.LOCK),
        Triple("连接车辆", "USB 优先，无线尽力。869 主机支持 1280×720；其他分辨率自动适配。", CarGlyph.USB),
        Triple("选择布局", "经典左 Dock、融合底部胶囊，或按车机宽高比自动选择。", CarGlyph.GRID),
        Triple("完成", "先看 UI 预览，真车 M0 通过后再接入实时投屏。", CarGlyph.CHECK),
    )
    Column(modifier = Modifier.fillMaxSize()) {
        CarTopBar("引导", onSkip, subtitle = "第 ${step + 1} / ${steps.size} 步", actions = { CarGhostButton("跳过", onSkip) })
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = dimens.pad), contentAlignment = Alignment.Center) {
            GlassCard(modifier = Modifier.fillMaxWidth(0.72f)) {
                Box(
                    modifier = Modifier
                        .size(dimens.touch + 28.dp)
                        .clip(RoundedCornerShape(dimens.cardRadius))
                        .background(palette.primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CarIcon(steps[step].third, size = dimens.iconLarge, tint = palette.primary)
                }
                Spacer(Modifier.height(dimens.gap))
                Text(steps[step].first, style = LocalCarTypography.current.display, color = palette.textPrimary)
                Spacer(Modifier.height(dimens.gapSmall))
                Text(steps[step].second, style = LocalCarTypography.current.body, color = palette.textSecondary)
                Spacer(Modifier.height(dimens.pad))
                Row(horizontalArrangement = Arrangement.spacedBy(dimens.gapSmall), verticalAlignment = Alignment.CenterVertically) {
                    repeat(steps.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == step) 22.dp else 8.dp, 8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (index == step) palette.primary else palette.textTertiary.copy(alpha = 0.3f)),
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    if (step > 0) {
                        CarGhostButton("上一步", { step -= 1 })
                    }
                    CarPrimaryButton(
                        if (step == steps.lastIndex) "进入桌面" else "下一步",
                        {
                            if (step == steps.lastIndex) onFinish() else step += 1
                        },
                        glyph = if (step == steps.lastIndex) CarGlyph.CHECK else CarGlyph.CHEVRON,
                    )
                }
            }
        }
    }
}

private fun CarLayoutMode.label(): String = when (this) {
    CarLayoutMode.AUTO -> "自动"
    CarLayoutMode.CLASSIC -> "经典左 Dock"
    CarLayoutMode.FUSION -> "融合底部胶囊"
}

private fun CarThemeMode.label(): String = when (this) {
    CarThemeMode.DARK -> "深空"
    CarThemeMode.LIGHT -> "晨雾"
    CarThemeMode.AUTO -> "自动"
}

private fun nextLayout(mode: CarLayoutMode): CarLayoutMode = when (mode) {
    CarLayoutMode.AUTO -> CarLayoutMode.CLASSIC
    CarLayoutMode.CLASSIC -> CarLayoutMode.FUSION
    CarLayoutMode.FUSION -> CarLayoutMode.AUTO
}
