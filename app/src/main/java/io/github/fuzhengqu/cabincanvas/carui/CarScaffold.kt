package io.github.fuzhengqu.cabincanvas.carui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.github.fuzhengqu.cabincanvas.data.CarGlyph
import io.github.fuzhengqu.cabincanvas.designsystem.CarIcon
import io.github.fuzhengqu.cabincanvas.designsystem.CarIconButton
import io.github.fuzhengqu.cabincanvas.designsystem.LocalCarDimens
import io.github.fuzhengqu.cabincanvas.designsystem.LocalCarPalette
import io.github.fuzhengqu.cabincanvas.designsystem.LocalCarTypography

@Composable
fun CarTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(dimens.topBarHeight)
            .padding(horizontal = dimens.pad),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CarIconButton(CarGlyph.BACK, "返回", onBack, modifier = Modifier.padding(end = dimens.gapSmall))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = LocalCarTypography.current.title, color = palette.textPrimary)
            if (subtitle != null) {
                Text(subtitle, style = LocalCarTypography.current.caption, color = palette.textSecondary)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(dimens.gapSmall), verticalAlignment = Alignment.CenterVertically) {
            actions()
        }
    }
}

@Composable
fun CarSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索",
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = LocalCarTypography.current.body.copy(color = palette.textPrimary),
        cursorBrush = SolidColor(palette.primary),
        modifier = modifier
            .fillMaxWidth()
            .height(dimens.touch)
            .clip(RoundedCornerShape(50))
            .background(palette.surfaceElevated.copy(alpha = 0.75f))
            .border(1.dp, palette.outline, RoundedCornerShape(50))
            .padding(horizontal = dimens.pad),
        decorationBox = { innerTextField ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                CarIcon(CarGlyph.SEARCH, size = dimens.iconSmall, tint = palette.textSecondary)
                Spacer(Modifier.width(dimens.gapSmall))
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(placeholder, style = LocalCarTypography.current.body, color = palette.textTertiary)
                    }
                    innerTextField()
                }
            }
        },
    )
}

@Composable
fun CarScreenSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val dimens = LocalCarDimens.current
    Box(modifier = modifier.padding(horizontal = dimens.pad)) {
        content()
    }
}

fun CharSequence.normalizedQuery(): String = toString().trim().lowercase()
