package io.github.fuzhengqu.cabincanvas.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.fuzhengqu.cabincanvas.core.CarDestination
import io.github.fuzhengqu.cabincanvas.core.CarDockMode
import io.github.fuzhengqu.cabincanvas.core.CarViewport
import io.github.fuzhengqu.cabincanvas.data.CarGlyph
import io.github.fuzhengqu.cabincanvas.data.CarWeather
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = LocalCarDimens.current.cardPadding,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    val shape = RoundedCornerShape(dimens.cardRadius)
    Column(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        palette.glass,
                        palette.glass.copy(alpha = (palette.glass.alpha * 0.88f).coerceAtLeast(0.55f)),
                    ),
                ),
            )
            .border(1.dp, palette.outline, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        content = content,
    )
}

@Composable
fun CarPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glyph: CarGlyph? = null,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = dimens.touch),
        shape = RoundedCornerShape(dimens.cardRadiusSmall),
        colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = palette.onPrimary),
        contentPadding = PaddingValues(horizontal = dimens.cardPadding, vertical = dimens.padSmall),
    ) {
        if (glyph != null) {
            CarIcon(glyph, size = dimens.iconSmall, tint = palette.onPrimary)
            Spacer(Modifier.width(dimens.gapSmall))
        }
        Text(text, style = LocalCarTypography.current.button)
    }
}

@Composable
fun CarGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glyph: CarGlyph? = null,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = dimens.touch),
        shape = RoundedCornerShape(dimens.cardRadiusSmall),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.textPrimary),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.outline),
        contentPadding = PaddingValues(horizontal = dimens.cardPadding, vertical = dimens.padSmall),
    ) {
        if (glyph != null) {
            CarIcon(glyph, size = dimens.iconSmall, tint = palette.textPrimary)
            Spacer(Modifier.width(dimens.gapSmall))
        }
        Text(text, style = LocalCarTypography.current.button)
    }
}

@Composable
fun CarIconButton(
    glyph: CarGlyph,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    tint: Color = LocalCarPalette.current.textPrimary,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Box(
        modifier = modifier
            .size(dimens.touch)
            .clip(CircleShape)
            .background(if (active) palette.primary.copy(alpha = 0.18f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        CarIcon(glyph, size = dimens.icon, tint = if (active) palette.primary else tint, contentDescription = contentDescription)
    }
}

@Composable
fun CarChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) palette.primary else palette.surfaceElevated.copy(alpha = 0.72f))
            .border(1.dp, if (selected) palette.primary else palette.outline, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = dimens.padSmall + 2.dp, vertical = dimens.padSmall)
            .heightIn(min = dimens.touch - 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = LocalCarTypography.current.caption,
            color = if (selected) palette.onPrimary else palette.textSecondary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

@Composable
fun CarStatusBar(
    weather: CarWeather,
    modifier: Modifier = Modifier,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    var time by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            time = LocalTime.now()
            delay(30_000)
        }
    }
    val timeText = time.format(DateTimeFormatter.ofPattern("HH:mm"))
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(dimens.statusBarHeight)
            .padding(horizontal = dimens.pad),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(timeText, style = LocalCarTypography.current.title, color = palette.textPrimary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(dimens.gap))
        Text(
            "${weather.temperature}° ${weather.condition}",
            style = LocalCarTypography.current.caption,
            color = palette.textSecondary,
        )
        Spacer(Modifier.weight(1f))
        CarIcon(CarGlyph.SIGNAL, size = dimens.iconSmall, tint = palette.textSecondary)
        Spacer(Modifier.width(dimens.gapSmall))
        Text("5G", style = LocalCarTypography.current.caption, color = palette.textSecondary)
        Spacer(Modifier.width(dimens.gap))
        CarIcon(CarGlyph.WIFI, size = dimens.iconSmall, tint = palette.textSecondary)
        Spacer(Modifier.width(dimens.gap))
        CarIcon(CarGlyph.BLUETOOTH, size = dimens.iconSmall, tint = palette.success)
        Spacer(Modifier.width(dimens.gap))
        Text("82%", style = LocalCarTypography.current.caption, color = palette.textSecondary)
        Spacer(Modifier.width(dimens.gapSmall))
        CarIcon(CarGlyph.BATTERY, size = dimens.icon, tint = palette.success)
    }
}

@Composable
fun CarDock(
    viewport: CarViewport,
    selected: CarDestination,
    onSelect: (CarDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        CarDestination.HOME,
        CarDestination.APPS,
        CarDestination.MEDIA,
        CarDestination.PHONE,
        CarDestination.SETTINGS,
        CarDestination.VOICE,
    )
    when (viewport.layout.dockMode) {
        CarDockMode.LEFT -> LeftDock(items, selected, onSelect, viewport, modifier)
        CarDockMode.BOTTOM_CAPSULE -> BottomCapsule(items, selected, onSelect, viewport, modifier)
        CarDockMode.BOTTOM_BAR -> BottomBar(items, selected, onSelect, viewport, modifier)
    }
}

@Composable
private fun LeftDock(
    items: List<CarDestination>,
    selected: CarDestination,
    onSelect: (CarDestination) -> Unit,
    viewport: CarViewport,
    modifier: Modifier,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Column(
        modifier = modifier
            .width(dimens.dockWidth)
            .fillMaxHeight()
            .padding(vertical = dimens.padSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.gapSmall),
    ) {
        items.forEach { item ->
            DockItem(item, selected == item, viewport.layout.showDockLabels, onSelect, vertical = true)
        }
    }
}

@Composable
private fun BottomCapsule(
    items: List<CarDestination>,
    selected: CarDestination,
    onSelect: (CarDestination) -> Unit,
    viewport: CarViewport,
    modifier: Modifier,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.pad, vertical = dimens.padSmall),
        horizontalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(palette.dock)
                .border(1.dp, palette.outline, RoundedCornerShape(50))
                .padding(horizontal = dimens.padSmall, vertical = dimens.padSmall),
            horizontalArrangement = Arrangement.spacedBy(dimens.gapSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                DockItem(item, selected == item, false, onSelect, vertical = false)
            }
        }
    }
}

@Composable
private fun BottomBar(
    items: List<CarDestination>,
    selected: CarDestination,
    onSelect: (CarDestination) -> Unit,
    viewport: CarViewport,
    modifier: Modifier,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(dimens.dockHeight)
            .background(palette.dock)
            .border(1.dp, palette.outline),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            DockItem(item, selected == item, true, onSelect, vertical = false)
        }
    }
}

@Composable
private fun DockItem(
    item: CarDestination,
    active: Boolean,
    showLabel: Boolean,
    onSelect: (CarDestination) -> Unit,
    vertical: Boolean,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    val shape = RoundedCornerShape(dimens.cardRadiusSmall)
    if (vertical) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(if (active) palette.primary.copy(alpha = 0.16f) else Color.Transparent)
                .clickable { onSelect(item) }
                .padding(vertical = dimens.padSmall),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CarIcon(item.glyph, size = dimens.icon, tint = if (active) palette.primary else palette.textSecondary)
            if (showLabel) {
                Spacer(Modifier.height(4.dp))
                Text(
                    item.label,
                    style = LocalCarTypography.current.caption,
                    color = if (active) palette.primary else palette.textSecondary,
                    maxLines = 1,
                )
            }
        }
    } else {
        Row(
            modifier = Modifier
                .clip(shape)
                .background(if (active) palette.primary.copy(alpha = 0.16f) else Color.Transparent)
                .clickable { onSelect(item) }
                .padding(horizontal = dimens.padSmall + 4.dp, vertical = dimens.padSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CarIcon(item.glyph, size = dimens.icon, tint = if (active) palette.primary else palette.textSecondary)
            if (showLabel && active) {
                Spacer(Modifier.width(dimens.gapSmall))
                Text(item.label, style = LocalCarTypography.current.caption, color = palette.primary)
            }
        }
    }
}

@Composable
fun PageDots(count: Int, selected: Int, modifier: Modifier = Modifier) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(dimens.gapSmall)) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == selected) dimens.pageDot + 2.dp else dimens.pageDot)
                    .clip(CircleShape)
                    .background(if (index == selected) palette.primary else palette.textTertiary.copy(alpha = 0.35f)),
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    val palette = LocalCarPalette.current
    Column(modifier = modifier) {
        Text(title, style = LocalCarTypography.current.title, color = palette.textPrimary, fontWeight = FontWeight.SemiBold)
        if (subtitle != null) {
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = LocalCarTypography.current.bodySmall, color = palette.textSecondary)
        }
    }
}

@Composable
fun SettingRow(
    glyph: CarGlyph,
    title: String,
    subtitle: String? = null,
    trailing: String? = null,
    switch: Boolean? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    var switchState by remember(switch) { mutableStateOf(switch ?: false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.cardRadiusSmall))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = dimens.padSmall, vertical = dimens.padSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(dimens.touch - 6.dp)
                .clip(RoundedCornerShape(dimens.cardRadiusSmall))
                .background(palette.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            CarIcon(glyph, size = dimens.iconSmall, tint = palette.primary)
        }
        Spacer(Modifier.width(dimens.gap))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = LocalCarTypography.current.body, color = palette.textPrimary, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(subtitle, style = LocalCarTypography.current.caption, color = palette.textSecondary, maxLines = 1)
            }
        }
        when {
            switch != null -> Switch(
                checked = switchState,
                onCheckedChange = {
                    switchState = it
                    onClick?.invoke()
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = palette.onPrimary,
                    checkedTrackColor = palette.primary,
                    uncheckedThumbColor = palette.textTertiary,
                    uncheckedTrackColor = palette.surfaceElevated,
                ),
            )
            trailing != null -> Text(trailing, style = LocalCarTypography.current.caption, color = palette.textSecondary)
            onClick != null -> CarIcon(CarGlyph.CHEVRON, size = dimens.iconSmall, tint = palette.textTertiary)
        }
    }
}

@Composable
fun Avatar(letter: String, color: Color, modifier: Modifier = Modifier, size: Dp = LocalCarDimens.current.touch) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(letter, style = LocalCarTypography.current.cardTitle, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun CarProgress(progress: Float, modifier: Modifier = Modifier) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(CircleShape)
            .background(palette.textTertiary.copy(alpha = 0.2f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(CircleShape)
                .background(palette.primary),
        )
    }
}

@Composable
fun EmptyState(glyph: CarGlyph, title: String, subtitle: String, modifier: Modifier = Modifier) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Column(
        modifier = modifier.padding(dimens.pad),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(dimens.touch + 12.dp)
                .clip(CircleShape)
                .background(palette.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            CarIcon(glyph, size = dimens.iconLarge, tint = palette.primary)
        }
        Spacer(Modifier.height(dimens.gap))
        Text(title, style = LocalCarTypography.current.cardTitle, color = palette.textPrimary)
        Spacer(Modifier.height(4.dp))
        Text(
            subtitle,
            style = LocalCarTypography.current.bodySmall,
            color = palette.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun CarBadge(text: String, color: Color = LocalCarPalette.current.primary, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(text, style = LocalCarTypography.current.caption, color = color, fontWeight = FontWeight.Medium)
    }
}
