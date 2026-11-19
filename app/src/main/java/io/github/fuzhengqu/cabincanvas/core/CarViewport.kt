package io.github.fuzhengqu.cabincanvas.core

import androidx.compose.runtime.Immutable

enum class CarSizeClass { COMPACT, MEDIUM, EXPANDED, ULTRA }
enum class CarAspectClass { PORTRAIT, STANDARD, WIDE, ULTRA_WIDE }
enum class CarDockMode { LEFT, BOTTOM_CAPSULE, BOTTOM_BAR }
enum class CarLayoutMode { AUTO, CLASSIC, FUSION }

@Immutable
data class CarLayout(
    val dockMode: CarDockMode,
    val columns: Int,
    val cardRows: Int,
    val mapWeight: Float,
    val typeScale: Float,
    val showDockLabels: Boolean,
    val pageCount: Int,
    val contentMaxWidthDp: Float,
    val gridCellDp: Float,
    val isCompact: Boolean,
    val showExtraColumn: Boolean,
)

@Immutable
data class CarViewport(
    val widthPx: Int,
    val heightPx: Int,
    val densityDpi: Int,
    val widthDp: Float,
    val heightDp: Float,
    val aspectRatio: Float,
    val sizeClass: CarSizeClass,
    val aspectClass: CarAspectClass,
    val layout: CarLayout,
    val overscanLeftDp: Float = 0f,
    val overscanTopDp: Float = 0f,
    val overscanRightDp: Float = 0f,
    val overscanBottomDp: Float = 0f,
) {
    val label: String
        get() = "${widthPx}x${heightPx} @ ${densityDpi}dpi"
}

/**
 * Resolution-independent layout policy. Pixel size never selects a layout directly:
 * dp (physical size) selects type/spacing, while aspect ratio and width decide dock,
 * columns and card composition. This keeps 800x480 readable and 8K from becoming
 * a wall of tiny columns.
 */
fun carViewportOf(
    widthPx: Int,
    heightPx: Int,
    densityDpi: Int,
    layoutMode: CarLayoutMode = CarLayoutMode.AUTO,
    overscanPercent: Float = 0f,
): CarViewport {
    val safeWidth = widthPx.coerceAtLeast(1)
    val safeHeight = heightPx.coerceAtLeast(1)
    val dpi = densityDpi.coerceAtLeast(80)
    val density = dpi / 160f
    val widthDp = safeWidth / density
    val heightDp = safeHeight / density
    val aspect = safeWidth.toFloat() / safeHeight

    val sizeClass = when {
        widthDp < 960f -> CarSizeClass.COMPACT
        widthDp < 1440f -> CarSizeClass.MEDIUM
        widthDp < 1920f -> CarSizeClass.EXPANDED
        else -> CarSizeClass.ULTRA
    }
    val aspectClass = when {
        aspect < 1f -> CarAspectClass.PORTRAIT
        aspect < 1.5f -> CarAspectClass.STANDARD
        aspect < 2.2f -> CarAspectClass.WIDE
        else -> CarAspectClass.ULTRA_WIDE
    }

    val dockMode = when (layoutMode) {
        CarLayoutMode.CLASSIC -> CarDockMode.LEFT
        CarLayoutMode.FUSION -> if (aspectClass == CarAspectClass.PORTRAIT) {
            CarDockMode.BOTTOM_BAR
        } else {
            CarDockMode.BOTTOM_CAPSULE
        }
        CarLayoutMode.AUTO -> when {
            aspectClass == CarAspectClass.PORTRAIT -> CarDockMode.BOTTOM_BAR
            aspectClass == CarAspectClass.STANDARD -> CarDockMode.BOTTOM_CAPSULE
            aspectClass == CarAspectClass.ULTRA_WIDE -> CarDockMode.BOTTOM_CAPSULE
            sizeClass == CarSizeClass.COMPACT -> CarDockMode.LEFT
            else -> CarDockMode.LEFT
        }
    }

    val baseColumns = when (sizeClass) {
        CarSizeClass.COMPACT -> 3
        CarSizeClass.MEDIUM -> 4
        CarSizeClass.EXPANDED -> 6
        CarSizeClass.ULTRA -> 8
    }
    val ultraWideBonus = if (aspectClass == CarAspectClass.ULTRA_WIDE && sizeClass != CarSizeClass.COMPACT) 1 else 0
    val columns = (baseColumns + ultraWideBonus).coerceIn(3, 10)

    val mapWeight = when (aspectClass) {
        CarAspectClass.PORTRAIT -> 1f
        CarAspectClass.STANDARD -> 0.56f
        CarAspectClass.WIDE -> 0.58f
        CarAspectClass.ULTRA_WIDE -> 0.52f
    }

    val rawScale = widthDp / 1280f
    val typeScale = rawScale.coerceIn(0.82f, 2.0f)

    val gridCell = when (sizeClass) {
        CarSizeClass.COMPACT -> 82f
        CarSizeClass.MEDIUM -> 94f
        CarSizeClass.EXPANDED -> 108f
        CarSizeClass.ULTRA -> 124f
    }

    val overscan = overscanPercent.coerceIn(0f, 8f) / 100f
    val overX = widthDp * overscan
    val overY = heightDp * overscan

    return CarViewport(
        widthPx = safeWidth,
        heightPx = safeHeight,
        densityDpi = dpi,
        widthDp = widthDp,
        heightDp = heightDp,
        aspectRatio = aspect,
        sizeClass = sizeClass,
        aspectClass = aspectClass,
        layout = CarLayout(
            dockMode = dockMode,
            columns = columns,
            cardRows = when (sizeClass) {
                CarSizeClass.COMPACT -> 1
                CarSizeClass.MEDIUM -> 2
                CarSizeClass.EXPANDED -> 2
                CarSizeClass.ULTRA -> 3
            },
            mapWeight = mapWeight,
            typeScale = typeScale,
            showDockLabels = widthDp >= 900f && aspectClass != CarAspectClass.ULTRA_WIDE,
            pageCount = if (sizeClass == CarSizeClass.COMPACT) 2 else 3,
            contentMaxWidthDp = if (sizeClass == CarSizeClass.ULTRA) 2800f else widthDp,
            gridCellDp = gridCell,
            isCompact = sizeClass == CarSizeClass.COMPACT,
            showExtraColumn = sizeClass == CarSizeClass.EXPANDED || sizeClass == CarSizeClass.ULTRA,
        ),
        overscanLeftDp = overX,
        overscanTopDp = overY,
        overscanRightDp = overX,
        overscanBottomDp = overY,
    )
}
