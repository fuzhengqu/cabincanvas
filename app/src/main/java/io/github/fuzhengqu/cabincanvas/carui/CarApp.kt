package io.github.fuzhengqu.cabincanvas.carui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.fuzhengqu.cabincanvas.core.CarLayoutMode
import io.github.fuzhengqu.cabincanvas.core.CarViewport
import io.github.fuzhengqu.cabincanvas.data.AppCatalog
import io.github.fuzhengqu.cabincanvas.data.CarAppCategory
import io.github.fuzhengqu.cabincanvas.data.CarAppEntry
import io.github.fuzhengqu.cabincanvas.data.CarCardConfig
import io.github.fuzhengqu.cabincanvas.data.CarCardSize
import io.github.fuzhengqu.cabincanvas.data.CarCardType
import io.github.fuzhengqu.cabincanvas.data.CarContact
import io.github.fuzhengqu.cabincanvas.data.CarNotification
import io.github.fuzhengqu.cabincanvas.data.FakeCarData
import io.github.fuzhengqu.cabincanvas.designsystem.CarTheme
import io.github.fuzhengqu.cabincanvas.designsystem.CarDarkPalette
import io.github.fuzhengqu.cabincanvas.designsystem.CarThemeMode
import io.github.fuzhengqu.cabincanvas.designsystem.CarWallpaper
import io.github.fuzhengqu.cabincanvas.designsystem.LocalCarPalette
import io.github.fuzhengqu.cabincanvas.designsystem.carWallpaperBrush
import kotlinx.coroutines.delay

enum class CarScreen {
    HOME, APPS, PROJECTION, MEDIA, PHONE, NOTIFICATIONS, SETTINGS,
    THEME, CARDS, CALIBRATION, DIAGNOSTICS, ONBOARDING,
}

@Composable
fun CarApp(
    viewport: CarViewport,
    themeMode: CarThemeMode,
    wallpaper: CarWallpaper,
    layoutMode: CarLayoutMode,
    overscanPercent: Int,
    initialScreen: CarScreen = CarScreen.HOME,
    onThemeChange: (CarThemeMode) -> Unit,
    onWallpaperChange: (CarWallpaper) -> Unit,
    onLayoutModeChange: (CarLayoutMode) -> Unit,
    onOverscanChange: (Int) -> Unit,
    onOpenProbe: () -> Unit,
) {
    val context = LocalContext.current
    var screen by remember(initialScreen) { mutableStateOf(initialScreen) }
    var mediaIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0.18f) }
    var selectedApp by remember { mutableStateOf<CarAppEntry?>(null) }
    var projectionMode by remember { mutableStateOf(CarProjectionMode.FULL) }
    var voiceVisible by remember { mutableStateOf(false) }
    var incomingCall by remember { mutableStateOf<CarContact?>(null) }
    var inCall by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<CarNotification?>(null) }
    var notifications by remember { mutableStateOf(FakeCarData.notifications) }
    var cards by remember { mutableStateOf(CarCardType.entries.map { CarCardConfig(it) }) }

    val installed by produceState(initialValue = emptyList<CarAppEntry>()) {
        value = AppCatalog.load(context)
    }
    val apps = remember(installed) {
        if (installed.size >= 6) installed else FakeCarData.fallbackApps
    }
    val media = FakeCarData.media[mediaIndex]

    LaunchedEffect(isPlaying, mediaIndex) {
        while (isPlaying) {
            delay(1000)
            progress += 1000f / media.durationMs.toFloat()
            if (progress >= 1f) {
                progress = 0f
                mediaIndex = (mediaIndex + 1) % FakeCarData.media.size
            }
        }
    }
    LaunchedEffect(toast) {
        if (toast != null) {
            delay(3000)
            toast = null
        }
    }

    fun showToast(title: String, body: String) {
        toast = CarNotification("CabinCanvas", title, body, "刚刚", CarDarkPalette.primary)
    }

    fun openNavi() {
        val nav = apps.firstOrNull { it.category == CarAppCategory.NAVI } ?: FakeCarData.fallbackApps.first()
        selectedApp = nav
        projectionMode = CarProjectionMode.FULL
        screen = CarScreen.PROJECTION
    }

    fun openApp(app: CarAppEntry, compat: Boolean) {
        selectedApp = app
        projectionMode = if (compat) CarProjectionMode.COMPAT else CarProjectionMode.FULL
        screen = CarScreen.PROJECTION
    }

    BackHandler(enabled = true) {
        when {
            voiceVisible -> voiceVisible = false
            inCall -> inCall = false
            incomingCall != null -> incomingCall = null
            screen == CarScreen.PROJECTION -> screen = CarScreen.APPS
            screen != CarScreen.HOME -> screen = CarScreen.HOME
        }
    }

    CarTheme(viewport = viewport, mode = themeMode) {
        val palette = LocalCarPalette.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(carWallpaperBrush(wallpaper, palette)),
        ) {
            Crossfade(targetState = screen, animationSpec = tween(220), label = "car-screen") { target ->
                when (target) {
                    CarScreen.HOME -> CarHome(
                        viewport = viewport,
                        media = media,
                        isPlaying = isPlaying,
                        progress = progress,
                        weather = FakeCarData.weather,
                        route = FakeCarData.route,
                        suggestions = FakeCarData.suggestions,
                        notifications = notifications,
                        onTogglePlay = { isPlaying = !isPlaying },
                        onNext = {
                            mediaIndex = (mediaIndex + 1) % FakeCarData.media.size
                            progress = 0f
                        },
                        onPrev = {
                            mediaIndex = (mediaIndex - 1 + FakeCarData.media.size) % FakeCarData.media.size
                            progress = 0f
                        },
                        onOpenMedia = { screen = CarScreen.MEDIA },
                        onOpenApps = { screen = CarScreen.APPS },
                        onOpenPhone = { screen = CarScreen.PHONE },
                        onOpenSettings = { screen = CarScreen.SETTINGS },
                        onOpenNavi = { openNavi() },
                        onOpenNotifications = { screen = CarScreen.NOTIFICATIONS },
                        onVoice = { voiceVisible = true },
                    )
                    CarScreen.APPS -> CarAppsScreen(
                        viewport = viewport,
                        apps = apps,
                        onBack = { screen = CarScreen.HOME },
                        onOpenApp = { app, compat -> openApp(app, compat) },
                        onOpenSettings = { screen = CarScreen.SETTINGS },
                    )
                    CarScreen.PROJECTION -> {
                        val app = selectedApp
                        if (app == null) {
                            LaunchedEffect(Unit) { screen = CarScreen.APPS }
                        } else {
                            CarProjectionScreen(
                                viewport = viewport,
                                app = app,
                                mode = projectionMode,
                                media = media,
                                isPlaying = isPlaying,
                                progress = progress,
                                onBack = { screen = CarScreen.APPS },
                                onHome = { screen = CarScreen.HOME },
                                onApps = { screen = CarScreen.APPS },
                                onModeChange = { projectionMode = it },
                                onTogglePlay = { isPlaying = !isPlaying },
                                onNext = {
                                    mediaIndex = (mediaIndex + 1) % FakeCarData.media.size
                                    progress = 0f
                                },
                                onPrev = {
                                    mediaIndex = (mediaIndex - 1 + FakeCarData.media.size) % FakeCarData.media.size
                                    progress = 0f
                                },
                                onVoice = { voiceVisible = true },
                            )
                        }
                    }
                    CarScreen.MEDIA -> CarMediaScreen(
                        viewport = viewport,
                        media = media,
                        isPlaying = isPlaying,
                        progress = progress,
                        onBack = { screen = CarScreen.HOME },
                        onTogglePlay = { isPlaying = !isPlaying },
                        onNext = {
                            mediaIndex = (mediaIndex + 1) % FakeCarData.media.size
                            progress = 0f
                        },
                        onPrev = {
                            mediaIndex = (mediaIndex - 1 + FakeCarData.media.size) % FakeCarData.media.size
                            progress = 0f
                        },
                        onSeek = { progress = it },
                        onSelectTrack = { mediaIndex = it; progress = 0f },
                    )
                    CarScreen.PHONE -> CarPhoneScreen(
                        viewport = viewport,
                        contacts = FakeCarData.contacts,
                        onBack = { screen = CarScreen.HOME },
                        onCall = { incomingCall = it },
                    )
                    CarScreen.NOTIFICATIONS -> CarNotificationsScreen(
                        notifications = notifications,
                        onBack = { screen = CarScreen.HOME },
                        onClear = { notifications = emptyList() },
                        onReply = {
                            showToast("已发送快捷回复", "给 ${it.app} 的回复已进入发送队列")
                        },
                        onSimulate = {
                            val n = FakeCarData.notifications.random()
                            notifications = listOf(n) + notifications
                            toast = n
                        },
                    )
                    CarScreen.SETTINGS -> CarSettingsScreen(
                        viewport = viewport,
                        themeMode = themeMode,
                        wallpaper = wallpaper,
                        layoutMode = layoutMode,
                        onBack = { screen = CarScreen.HOME },
                        onTheme = { screen = CarScreen.THEME },
                        onWallpaper = { screen = CarScreen.THEME },
                        onLayoutMode = onLayoutModeChange,
                        onCardManager = { screen = CarScreen.CARDS },
                        onCalibration = { screen = CarScreen.CALIBRATION },
                        onDiagnostics = { screen = CarScreen.DIAGNOSTICS },
                        onOnboarding = { screen = CarScreen.ONBOARDING },
                        onSimulateNotification = {
                            val n = FakeCarData.notifications.random()
                            notifications = listOf(n) + notifications
                            toast = n
                        },
                    )
                    CarScreen.THEME -> CarThemePickerScreen(
                        viewport = viewport,
                        themeMode = themeMode,
                        wallpaper = wallpaper,
                        onBack = { screen = CarScreen.SETTINGS },
                        onTheme = onThemeChange,
                        onWallpaper = onWallpaperChange,
                    )
                    CarScreen.CARDS -> CarCardManagerScreen(
                        cards = cards,
                        onBack = { screen = CarScreen.SETTINGS },
                        onToggle = { type ->
                            cards = cards.map { if (it.type == type) it.copy(enabled = !it.enabled) else it }
                        },
                        onSize = { type, size ->
                            cards = cards.map { if (it.type == type) it.copy(size = size) else it }
                        },
                        onAdd = {
                            showToast("已添加空白卡", "卡片管理将在 M1 接入真实拖拽排序")
                        },
                    )
                    CarScreen.CALIBRATION -> CarCalibrationScreen(
                        onBack = { screen = CarScreen.SETTINGS },
                        onSave = {
                            onOverscanChange(it)
                            showToast("校准已保存", "四边安全区已应用到投屏容器")
                            screen = CarScreen.SETTINGS
                        },
                    )
                    CarScreen.DIAGNOSTICS -> CarDiagnosticsScreen(
                        onBack = { screen = CarScreen.SETTINGS },
                        onOpenProbe = onOpenProbe,
                        onShareLog = { showToast("日志已准备", "预览模式不会写出真实日志；M0 探针会导出 probe.log") },
                        onSimulateNotification = {
                            val n = FakeCarData.notifications.random()
                            notifications = listOf(n) + notifications
                            toast = n
                        },
                    )
                    CarScreen.ONBOARDING -> CarOnboardingScreen(
                        onFinish = { screen = CarScreen.HOME },
                        onSkip = { screen = CarScreen.HOME },
                    )
                }
            }

            if (voiceVisible) {
                VoiceOverlay(
                    onCommand = { command ->
                        voiceVisible = false
                        when {
                            command.contains("导航") -> openNavi()
                            command.contains("播放") -> { isPlaying = true; screen = CarScreen.MEDIA }
                            command.contains("电话") -> screen = CarScreen.PHONE
                            command.contains("应用") -> screen = CarScreen.APPS
                            command.contains("天气") -> showToast("上海 26° 多云", "今天 21°–29°，适合出行")
                            else -> showToast("已收到", command)
                        }
                    },
                    onDismiss = { voiceVisible = false },
                )
            }
            val call = incomingCall
            if (call != null && !inCall) {
                IncomingCallOverlay(
                    contact = call,
                    onAnswer = { incomingCall = null; inCall = true },
                    onDecline = { incomingCall = null; showToast("已挂断", "未接来电：${call.name}") },
                )
            }
            if (inCall && call != null) {
                InCallOverlay(contact = call, onHangup = { inCall = false; showToast("通话结束", call.name) })
            }
            val currentToast = toast
            if (currentToast != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    NotificationToast(currentToast) { toast = null }
                }
            }
        }
    }
}
