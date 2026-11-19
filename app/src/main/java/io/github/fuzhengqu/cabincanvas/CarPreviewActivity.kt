package io.github.fuzhengqu.cabincanvas

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import io.github.fuzhengqu.cabincanvas.carui.CarApp
import io.github.fuzhengqu.cabincanvas.carui.CarScreen
import io.github.fuzhengqu.cabincanvas.core.CarAspectClass
import io.github.fuzhengqu.cabincanvas.core.CarLayoutMode
import io.github.fuzhengqu.cabincanvas.core.CarSizeClass
import io.github.fuzhengqu.cabincanvas.core.CarViewport
import io.github.fuzhengqu.cabincanvas.core.carViewportOf
import io.github.fuzhengqu.cabincanvas.data.real.DataPermissions
import io.github.fuzhengqu.cabincanvas.designsystem.CarThemeMode
import io.github.fuzhengqu.cabincanvas.designsystem.CarWallpaper
import io.github.fuzhengqu.cabincanvas.ui.theme.CabinCanvasTheme
import kotlin.math.min

private data class PreviewProfile(
    val label: String,
    val width: Int,
    val height: Int,
    val dpi: Int,
)

private val previewProfiles = listOf(
    PreviewProfile("800×480", 800, 480, 160),
    PreviewProfile("1024×600", 1024, 600, 160),
    PreviewProfile("1280×720", 1280, 720, 160),
    PreviewProfile("1920×720", 1920, 720, 160),
    PreviewProfile("1920×1080", 1920, 1080, 240),
    PreviewProfile("2560×1440", 2560, 1440, 240),
    PreviewProfile("3840×2160", 3840, 2160, 320),
    PreviewProfile("6016×3384", 6016, 3384, 320),
    PreviewProfile("7680×4320", 7680, 4320, 320),
    PreviewProfile("1080×1920 竖屏", 1080, 1920, 320),
)

private val previewDpis = listOf(120, 160, 213, 240, 320, 480)

class CarPreviewActivity : ComponentActivity() {
    private var permissionEpoch by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val initialScreen = runCatching {
            CarScreen.valueOf(intent.getStringExtra("screen") ?: "HOME")
        }.getOrDefault(CarScreen.HOME)
        setContent {
            CabinCanvasTheme {
                CarPreviewScreen(
                    onExit = { finish() },
                    onOpenProbe = ::openProbe,
                    initialScreen = initialScreen,
                    permissionEpoch = permissionEpoch,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        permissionEpoch++
    }

    private fun openProbe() {
        val intent = Intent().setClassName(
            "io.github.fuzhengqu.cabincanvas.probe.c",
            "io.github.fuzhengqu.cabincanvas.probe.c.ProbeActivity",
        )
        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "未安装 M0 探针 C", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun CarPreviewScreen(
    onExit: () -> Unit,
    onOpenProbe: () -> Unit,
    initialScreen: CarScreen,
    permissionEpoch: Int,
) {
    val context = LocalContext.current
    var permissionTick by remember { mutableIntStateOf(0) }
    val effectiveEpoch = permissionEpoch + permissionTick
    val notificationAccess = remember(effectiveEpoch) { DataPermissions.hasNotificationAccess(context) }
    val phoneAccess = remember(effectiveEpoch) { DataPermissions.hasPhoneAccess(context) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissionTick++ }
    var profile by remember { mutableStateOf(previewProfiles[2]) }
    var dpi by remember { mutableIntStateOf(profile.dpi) }
    var themeMode by remember { mutableStateOf(CarThemeMode.DARK) }
    var wallpaper by remember { mutableStateOf(CarWallpaper.AURORA) }
    var layoutMode by remember { mutableStateOf(CarLayoutMode.AUTO) }
    var overscan by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF05070C))) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("CabinCanvas Preview", color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    "分辨率无关布局 · 800×480 → 8K · 实时切换",
                    color = Color(0xFF8A91A0),
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                )
            }
            TextButton(onClick = onOpenProbe) { Text("M0 探针", color = Color(0xFF2F6BFF)) }
            TextButton(onClick = onExit) { Text("退出", color = Color.White) }
        }

        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(previewProfiles) { item ->
                FilterChip(
                    selected = profile == item,
                    onClick = {
                        profile = item
                        dpi = item.dpi
                    },
                    label = { Text(item.label) },
                )
            }
        }
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(previewDpis) { item ->
                FilterChip(
                    selected = dpi == item,
                    onClick = { dpi = item },
                    label = { Text("${item}dpi") },
                )
            }
            item {
                FilterChip(
                    selected = false,
                    onClick = {
                        wallpaper = CarWallpaper.entries[(wallpaper.ordinal + 1) % CarWallpaper.entries.size]
                    },
                    label = { Text("壁纸：${wallpaper.label}") },
                )
            }
            item {
                FilterChip(
                    selected = false,
                    onClick = {
                        themeMode = when (themeMode) {
                            CarThemeMode.DARK -> CarThemeMode.LIGHT
                            CarThemeMode.LIGHT -> CarThemeMode.AUTO
                            CarThemeMode.AUTO -> CarThemeMode.DARK
                        }
                    },
                    label = { Text("主题：${themeMode.label()}") },
                )
            }
            item {
                FilterChip(
                    selected = false,
                    onClick = {
                        layoutMode = when (layoutMode) {
                            CarLayoutMode.AUTO -> CarLayoutMode.CLASSIC
                            CarLayoutMode.CLASSIC -> CarLayoutMode.FUSION
                            CarLayoutMode.FUSION -> CarLayoutMode.AUTO
                        }
                    },
                    label = { Text("布局：${layoutMode.label()}") },
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            val targetDensity = dpi / 160f
            val widthDp = profile.width / targetDensity
            val heightDp = profile.height / targetDensity
            val density = LocalDensity.current
            val availableWidthPx = with(density) { maxWidth.toPx() }
            val availableHeightPx = with(density) { maxHeight.toPx() }
            val scale = min(
                availableWidthPx / profile.width.toFloat(),
                availableHeightPx / profile.height.toFloat(),
            ).coerceIn(0.05f, 1.25f)
            val previewDensity = (targetDensity * scale).coerceAtLeast(0.05f)
            val viewport = remember(profile, dpi, layoutMode, overscan) {
                carViewportOf(profile.width, profile.height, dpi, layoutMode, overscan.toFloat())
            }

            androidx.compose.runtime.CompositionLocalProvider(
                LocalDensity provides Density(previewDensity, 1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(widthDp.dp, heightDp.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(10.dp)),
                ) {
                    CarApp(
                        viewport = viewport,
                        themeMode = themeMode,
                        wallpaper = wallpaper,
                        layoutMode = layoutMode,
                        overscanPercent = overscan,
                        initialScreen = initialScreen,
                        onThemeChange = { themeMode = it },
                        onWallpaperChange = { wallpaper = it },
                        onLayoutModeChange = { layoutMode = it },
                        onOverscanChange = { overscan = it },
                        onOpenProbe = onOpenProbe,
                        permissionEpoch = effectiveEpoch,
                        notificationAccess = notificationAccess,
                        phoneAccess = phoneAccess,
                        onOpenNotificationAccess = {
                            context.startActivity(
                                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        },
                        onRequestPhonePermissions = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.READ_CONTACTS,
                                    Manifest.permission.READ_CALL_LOG,
                                    Manifest.permission.ANSWER_PHONE_CALLS,
                                    Manifest.permission.CALL_PHONE,
                                ),
                            )
                        },
                    )
                }
            }
        }

        PreviewInfoBar(profile, dpi, layoutMode, overscan)
    }
}

@Composable
private fun PreviewInfoBar(profile: PreviewProfile, dpi: Int, layoutMode: CarLayoutMode, overscan: Int) {
    val viewport: CarViewport = remember(profile, dpi, layoutMode, overscan) {
        carViewportOf(profile.width, profile.height, dpi, layoutMode, overscan.toFloat())
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0B0D12))
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            "${profile.width}×${profile.height} · ${dpi}dpi",
            color = Color.White,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            "${viewport.sizeClass.name} · ${viewport.aspectClass.name}",
            color = Color(0xFFA7ADBD),
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
        )
        Text(
            "${viewport.layout.columns} 列 · ${viewport.layout.dockMode.name}",
            color = Color(0xFFA7ADBD),
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
        )
        Text(
            "widthDp ${viewport.widthDp.toInt()} · overscan ${overscan}%",
            color = Color(0xFF6F7688),
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
        )
    }
}

private fun CarThemeMode.label(): String = when (this) {
    CarThemeMode.DARK -> "深空"
    CarThemeMode.LIGHT -> "晨雾"
    CarThemeMode.AUTO -> "自动"
}

private fun CarLayoutMode.label(): String = when (this) {
    CarLayoutMode.AUTO -> "自动"
    CarLayoutMode.CLASSIC -> "经典"
    CarLayoutMode.FUSION -> "融合"
}
