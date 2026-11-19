package io.github.fuzhengqu.cabincanvas.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fuzhengqu.cabincanvas.core.CarSizeClass
import io.github.fuzhengqu.cabincanvas.core.CarViewport

enum class CarThemeMode { DARK, LIGHT, AUTO }

enum class CarWallpaper(val label: String) {
    AURORA("极光"),
    NEBULA("星云"),
    SUNSET("暖阳"),
    FOREST("森林"),
    GRAPHITE("石墨"),
    OCEAN("深海"),
}

@Immutable
data class CarPalette(
    val background: Color,
    val backgroundAlt: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val glass: Color,
    val dock: Color,
    val primary: Color,
    val onPrimary: Color,
    val accent: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val outline: Color,
    val cardHighlight: Color,
    val mapLand: Color,
    val mapBlock: Color,
    val mapRoad: Color,
    val mapRoute: Color,
    val scrim: Color,
)

val CarDarkPalette = CarPalette(
    background = Color(0xFF0B0D12),
    backgroundAlt = Color(0xFF111522),
    surface = Color(0xFF151823),
    surfaceElevated = Color(0xFF1E2230),
    glass = Color(0xD9151823),
    dock = Color(0xEE1A1E2B),
    primary = Color(0xFF2F6BFF),
    onPrimary = Color.White,
    accent = Color(0xFF6C8DFF),
    success = Color(0xFF19A974),
    warning = Color(0xFFFFB020),
    danger = Color(0xFFE32E27),
    textPrimary = Color(0xFFE8EAF2),
    textSecondary = Color(0xFFA7ADBD),
    textTertiary = Color(0xFF6F7688),
    outline = Color(0x33FFFFFF),
    cardHighlight = Color(0x14FFFFFF),
    mapLand = Color(0xFF111826),
    mapBlock = Color(0xFF1B2436),
    mapRoad = Color(0xFF2C3750),
    mapRoute = Color(0xFF2F6BFF),
    scrim = Color(0xCC05070C),
)

val CarLightPalette = CarPalette(
    background = Color(0xFFF3F4F6),
    backgroundAlt = Color(0xFFE8EDF5),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFF8FAFD),
    glass = Color(0xE8FFFFFF),
    dock = Color(0xF5FFFFFF),
    primary = Color(0xFF2F6BFF),
    onPrimary = Color.White,
    accent = Color(0xFF4E7BFF),
    success = Color(0xFF0F8F62),
    warning = Color(0xFFDD8A0B),
    danger = Color(0xFFD32720),
    textPrimary = Color(0xFF111318),
    textSecondary = Color(0xFF5F6675),
    textTertiary = Color(0xFF8A91A0),
    outline = Color(0x18000000),
    cardHighlight = Color(0xFFFFFFFF),
    mapLand = Color(0xFFE9EEF6),
    mapBlock = Color(0xFFDCE4F0),
    mapRoad = Color(0xFFFFFFFF),
    mapRoute = Color(0xFF2F6BFF),
    scrim = Color(0x66000000),
)

@Immutable
data class CarDimens(
    val pad: Dp,
    val padSmall: Dp,
    val gap: Dp,
    val gapSmall: Dp,
    val cardRadius: Dp,
    val cardRadiusSmall: Dp,
    val dockWidth: Dp,
    val statusBarHeight: Dp,
    val dockHeight: Dp,
    val topBarHeight: Dp,
    val floatingDockHeight: Dp,
    val icon: Dp,
    val iconSmall: Dp,
    val iconLarge: Dp,
    val touch: Dp,
    val cardPadding: Dp,
    val pageDot: Dp,
    val typeScale: Float,
)

@Immutable
data class CarTypography(
    val display: TextStyle,
    val title: TextStyle,
    val cardTitle: TextStyle,
    val body: TextStyle,
    val bodySmall: TextStyle,
    val caption: TextStyle,
    val button: TextStyle,
    val mono: TextStyle,
)

val LocalCarPalette = staticCompositionLocalOf { CarDarkPalette }
val LocalCarDimens = staticCompositionLocalOf { carDimensFor(null) }
val LocalCarTypography = staticCompositionLocalOf { carTypographyFor(carDimensFor(null)) }

@Composable
fun CarTheme(
    viewport: CarViewport,
    mode: CarThemeMode = CarThemeMode.DARK,
    content: @Composable () -> Unit,
) {
    val dark = when (mode) {
        CarThemeMode.DARK -> true
        CarThemeMode.LIGHT -> false
        CarThemeMode.AUTO -> isSystemInDarkTheme()
    }
    val palette = if (dark) CarDarkPalette else CarLightPalette
    val dimens = remember(viewport) { carDimensFor(viewport) }
    val typography = remember(dimens) { carTypographyFor(dimens) }
    val scheme = if (dark) {
        darkColorScheme(
            primary = palette.primary,
            onPrimary = palette.onPrimary,
            secondary = palette.success,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceElevated,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.outline,
            error = palette.danger,
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            onPrimary = palette.onPrimary,
            secondary = palette.success,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceElevated,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.outline,
            error = palette.danger,
        )
    }
    CompositionLocalProvider(
        LocalCarPalette provides palette,
        LocalCarDimens provides dimens,
        LocalCarTypography provides typography,
    ) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}

fun carDimensFor(viewport: CarViewport?): CarDimens {
    val scale = viewport?.layout?.typeScale ?: 1f
    val compact = viewport?.sizeClass == CarSizeClass.COMPACT
    fun d(value: Float): Dp = (value * scale).dp
    return CarDimens(
        pad = d(if (compact) 14f else 20f),
        padSmall = d(if (compact) 8f else 12f),
        gap = d(if (compact) 8f else 12f),
        gapSmall = d(if (compact) 4f else 8f),
        cardRadius = d(if (compact) 18f else 24f),
        cardRadiusSmall = d(if (compact) 12f else 16f),
        dockWidth = d(if (compact) 58f else 82f),
        statusBarHeight = d(if (compact) 42f else 54f),
        dockHeight = d(if (compact) 56f else 70f),
        topBarHeight = d(if (compact) 42f else 50f),
        floatingDockHeight = d(if (compact) 52f else 64f),
        icon = d(if (compact) 18f else 21f),
        iconSmall = d(if (compact) 14f else 17f),
        iconLarge = d(if (compact) 28f else 34f),
        touch = d(if (compact) 42f else 48f),
        cardPadding = d(if (compact) 12f else 18f),
        pageDot = d(if (compact) 6f else 8f),
        typeScale = scale,
    )
}

fun carTypographyFor(d: CarDimens): CarTypography {
    val s = d.typeScale
    fun style(size: Float, weight: FontWeight, line: Float = size * 1.28f, tracking: Float = -0.2f) = TextStyle(
        fontSize = (size * s).sp,
        lineHeight = (line * s).sp,
        fontWeight = weight,
        letterSpacing = tracking.sp,
        fontFamily = FontFamily.SansSerif,
    )
    return CarTypography(
        display = style(36f, FontWeight.Bold, 42f, -0.6f),
        title = style(24f, FontWeight.SemiBold, 30f, -0.3f),
        cardTitle = style(18f, FontWeight.SemiBold, 23f, -0.2f),
        body = style(14f, FontWeight.Normal, 20f, 0f),
        bodySmall = style(12.5f, FontWeight.Normal, 17f, 0f),
        caption = style(11f, FontWeight.Medium, 15f, 0.1f),
        button = style(14f, FontWeight.SemiBold, 18f, 0f),
        mono = style(12f, FontWeight.Normal, 17f, 0f).copy(fontFamily = FontFamily.Monospace),
    )
}

fun carWallpaperBrush(wallpaper: CarWallpaper, palette: CarPalette): Brush {
    val accent = palette.primary.copy(alpha = 0.34f)
    val second = when (wallpaper) {
        CarWallpaper.AURORA -> Color(0xFF19A974).copy(alpha = 0.24f)
        CarWallpaper.NEBULA -> Color(0xFF8B5CF6).copy(alpha = 0.26f)
        CarWallpaper.SUNSET -> Color(0xFFFF8A3D).copy(alpha = 0.22f)
        CarWallpaper.FOREST -> Color(0xFF2BB673).copy(alpha = 0.20f)
        CarWallpaper.GRAPHITE -> Color(0xFF6B7280).copy(alpha = 0.16f)
        CarWallpaper.OCEAN -> Color(0xFF0EA5E9).copy(alpha = 0.24f)
    }
    val third = when (wallpaper) {
        CarWallpaper.AURORA -> Color(0xFF0EA5E9).copy(alpha = 0.18f)
        CarWallpaper.NEBULA -> Color(0xFFEC4899).copy(alpha = 0.18f)
        CarWallpaper.SUNSET -> Color(0xFFE32E27).copy(alpha = 0.16f)
        CarWallpaper.FOREST -> Color(0xFF84CC16).copy(alpha = 0.14f)
        CarWallpaper.GRAPHITE -> Color(0xFF94A3B8).copy(alpha = 0.12f)
        CarWallpaper.OCEAN -> Color(0xFF22D3EE).copy(alpha = 0.16f)
    }
    return Brush.linearGradient(
        colors = listOf(palette.background, palette.backgroundAlt, accent, third, palette.background),
    )
}
