package io.github.fuzhengqu.cabincanvas.carui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import io.github.fuzhengqu.cabincanvas.data.CarNotification
import io.github.fuzhengqu.cabincanvas.data.FakeCarData
import io.github.fuzhengqu.cabincanvas.data.real.CarCallStatus
import io.github.fuzhengqu.cabincanvas.data.real.MediaRepository
import io.github.fuzhengqu.cabincanvas.data.real.NotificationReplyTest
import io.github.fuzhengqu.cabincanvas.data.real.NotificationStore
import io.github.fuzhengqu.cabincanvas.data.real.PhoneRepository
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
    permissionEpoch: Int = 0,
    notificationAccess: Boolean = false,
    phoneAccess: Boolean = false,
    onOpenNotificationAccess: () -> Unit = {},
    onRequestPhonePermissions: () -> Unit = {},
) {
    val context = LocalContext.current
    var screen by remember(initialScreen) { mutableStateOf(initialScreen) }
    var mediaIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0.18f) }
    var selectedApp by remember { mutableStateOf<CarAppEntry?>(null) }
    var projectionMode by remember { mutableStateOf(CarProjectionMode.FULL) }
    var voiceVisible by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<CarNotification?>(null) }
    var cards by remember { mutableStateOf(CarCardType.entries.map { CarCardConfig(it) }) }

    val mediaRepo = remember { MediaRepository(context) }
    val phoneRepo = remember { PhoneRepository(context) }
    DisposableEffect(Unit) {
        mediaRepo.start()
        phoneRepo.start()
        onDispose {
            mediaRepo.stop()
            phoneRepo.stop()
        }
    }
    LaunchedEffect(permissionEpoch) {
        mediaRepo.refresh()
        phoneRepo.refresh()
        NotificationStore.requestRebind(context)
    }

    val mediaState by mediaRepo.state.collectAsState()
    val notifications by NotificationStore.notifications.collectAsState()
    val contacts by phoneRepo.contacts.collectAsState()
    val recent by phoneRepo.recent.collectAsState()
    val callState by phoneRepo.call.collectAsState()

    val installed by produceState(initialValue = emptyList<CarAppEntry>()) {
        value = AppCatalog.load(context)
    }
    val apps = remember(installed) {
        if (installed.size >= 6) installed else FakeCarData.fallbackApps
    }

    val demoMedia = FakeCarData.media[mediaIndex]
    val realMedia = mediaState.media
    val media = realMedia ?: demoMedia
    val playing = if (realMedia != null) mediaState.isPlaying else isPlaying
    val mediaProgress = if (realMedia != null) {
        (mediaState.positionMs.toFloat() / media.durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        progress
    }

    LaunchedEffect(isPlaying, mediaIndex, realMedia) {
        if (realMedia == null && isPlaying) {
            while (true) {
                delay(1000)
                progress += 1000f / demoMedia.durationMs.toFloat()
                if (progress >= 1f) {
                    progress = 0f
                    mediaIndex = (mediaIndex + 1) % FakeCarData.media.size
                }
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
            callState.status != CarCallStatus.IDLE -> phoneRepo.hangup()
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
                        isPlaying = playing,
                        progress = mediaProgress,
                        weather = FakeCarData.weather,
                        route = FakeCarData.route,
                        suggestions = FakeCarData.suggestions,
                        notifications = notifications,
                        onTogglePlay = {
                            if (realMedia != null) mediaRepo.toggle() else isPlaying = !isPlaying
                        },
                        onNext = {
                            if (realMedia != null) {
                                mediaRepo.next()
                            } else {
                                mediaIndex = (mediaIndex + 1) % FakeCarData.media.size
                                progress = 0f
                            }
                        },
                        onPrev = {
                            if (realMedia != null) {
                                mediaRepo.previous()
                            } else {
                                mediaIndex = (mediaIndex - 1 + FakeCarData.media.size) % FakeCarData.media.size
                                progress = 0f
                            }
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
                                isPlaying = playing,
                                progress = mediaProgress,
                                onBack = { screen = CarScreen.APPS },
                                onHome = { screen = CarScreen.HOME },
                                onApps = { screen = CarScreen.APPS },
                                onModeChange = { projectionMode = it },
                                onTogglePlay = {
                                    if (realMedia != null) mediaRepo.toggle() else isPlaying = !isPlaying
                                },
                                onNext = {
                                    if (realMedia != null) {
                                        mediaRepo.next()
                                    } else {
                                        mediaIndex = (mediaIndex + 1) % FakeCarData.media.size
                                        progress = 0f
                                    }
                                },
                                onPrev = {
                                    if (realMedia != null) {
                                        mediaRepo.previous()
                                    } else {
                                        mediaIndex = (mediaIndex - 1 + FakeCarData.media.size) % FakeCarData.media.size
                                        progress = 0f
                                    }
                                },
                                onVoice = { voiceVisible = true },
                            )
                        }
                    }
                    CarScreen.MEDIA -> CarMediaScreen(
                        viewport = viewport,
                        media = media,
                        isPlaying = playing,
                        progress = mediaProgress,
                        onBack = { screen = CarScreen.HOME },
                        onTogglePlay = {
                            if (realMedia != null) mediaRepo.toggle() else isPlaying = !isPlaying
                        },
                        onNext = {
                            if (realMedia != null) {
                                mediaRepo.next()
                            } else {
                                mediaIndex = (mediaIndex + 1) % FakeCarData.media.size
                                progress = 0f
                            }
                        },
                        onPrev = {
                            if (realMedia != null) {
                                mediaRepo.previous()
                            } else {
                                mediaIndex = (mediaIndex - 1 + FakeCarData.media.size) % FakeCarData.media.size
                                progress = 0f
                            }
                        },
                        onSeek = {
                            if (realMedia != null) {
                                mediaRepo.seekTo((it * media.durationMs).toLong())
                            } else {
                                progress = it
                            }
                        },
                        onSelectTrack = {
                            if (realMedia == null) {
                                mediaIndex = it
                                progress = 0f
                            }
                        },
                    )
                    CarScreen.PHONE -> CarPhoneScreen(
                        viewport = viewport,
                        contacts = contacts,
                        recent = recent,
                        onBack = { screen = CarScreen.HOME },
                        onCall = { phoneRepo.demoIncoming(it) },
                    )
                    CarScreen.NOTIFICATIONS -> CarNotificationsScreen(
                        notifications = notifications,
                        onBack = { screen = CarScreen.HOME },
                        onClear = { NotificationStore.clear() },
                        onReply = { notification, text ->
                            val ok = NotificationStore.reply(context, notification.key, text)
                            showToast(
                                if (ok) "已发送回复" else "无法回复",
                                if (ok) "回复已发送给 ${notification.app}" else "该通知不支持快捷回复，或已失效",
                            )
                        },
                        onSimulate = {
                            val n = FakeCarData.notifications.random()
                            NotificationStore.addDemo(n)
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
                            NotificationStore.addDemo(n)
                            toast = n
                        },
                        notificationAccess = notificationAccess,
                        phoneAccess = phoneAccess,
                        onOpenNotificationAccess = onOpenNotificationAccess,
                        onRequestPhonePermissions = onRequestPhonePermissions,
                        onRefreshData = {
                            mediaRepo.refresh()
                            phoneRepo.refresh()
                            showToast("已刷新数据源", "媒体会话与联系人已重新读取")
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
                            NotificationStore.addDemo(n)
                            toast = n
                        },
                        onTestReply = {
                            val ok = NotificationReplyTest.post(context)
                            showToast(
                                if (ok) "已发送回复测试通知" else "无法发送测试通知",
                                if (ok) "在通知中心点“快捷回复”，发送后查看 logcat：CabinCanvasReply" else "请先授予通知权限",
                            )
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
                            command.contains("播放") -> {
                                if (realMedia != null) mediaRepo.toggle() else isPlaying = true
                                screen = CarScreen.MEDIA
                            }
                            command.contains("电话") -> screen = CarScreen.PHONE
                            command.contains("应用") -> screen = CarScreen.APPS
                            command.contains("天气") -> showToast("上海 26° 多云", "今天 21°–29°，适合出行")
                            else -> showToast("已收到", command)
                        }
                    },
                    onDismiss = { voiceVisible = false },
                )
            }
            val callContact = callState.contact
            if (callState.status == CarCallStatus.RINGING && callContact != null) {
                IncomingCallOverlay(
                    contact = callContact,
                    onAnswer = { phoneRepo.answer() },
                    onDecline = {
                        phoneRepo.hangup()
                        showToast("已挂断", "未接来电：${callContact.name}")
                    },
                )
            }
            if (callState.status == CarCallStatus.OFFHOOK && callContact != null) {
                InCallOverlay(
                    contact = callContact,
                    onHangup = {
                        phoneRepo.hangup()
                        showToast("通话结束", callContact.name)
                    },
                )
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
