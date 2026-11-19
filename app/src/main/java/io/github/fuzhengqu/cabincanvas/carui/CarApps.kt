package io.github.fuzhengqu.cabincanvas.carui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.fuzhengqu.cabincanvas.core.CarViewport
import io.github.fuzhengqu.cabincanvas.data.CarAppCategory
import io.github.fuzhengqu.cabincanvas.data.CarAppEntry
import io.github.fuzhengqu.cabincanvas.data.CarGlyph
import io.github.fuzhengqu.cabincanvas.designsystem.CarChip
import io.github.fuzhengqu.cabincanvas.designsystem.CarIcon
import io.github.fuzhengqu.cabincanvas.designsystem.CarPrimaryButton
import io.github.fuzhengqu.cabincanvas.designsystem.EmptyState
import io.github.fuzhengqu.cabincanvas.designsystem.LocalCarDimens
import io.github.fuzhengqu.cabincanvas.designsystem.LocalCarPalette
import io.github.fuzhengqu.cabincanvas.designsystem.LocalCarTypography

@Composable
fun CarAppsScreen(
    viewport: CarViewport,
    apps: List<CarAppEntry>,
    onBack: () -> Unit,
    onOpenApp: (CarAppEntry, Boolean) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(CarAppCategory.ALL) }
    var selected by remember { mutableStateOf<CarAppEntry?>(null) }

    val filtered = remember(apps, query, category) {
        apps.filter { app ->
            (category == CarAppCategory.ALL || app.category == category) &&
                (query.isBlank() || app.label.contains(query, ignoreCase = true) || app.packageName.contains(query, ignoreCase = true))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CarTopBar(
            title = "应用",
            onBack = onBack,
            subtitle = "共 ${filtered.size} 个 · ${viewport.layout.columns} 列",
            actions = {
                CarPrimaryButton("设置", onOpenSettings, glyph = CarGlyph.SETTINGS)
            },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.pad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.gap),
        ) {
            CarSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "搜索应用、包名",
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(dimens.gapSmall))
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = dimens.pad),
            horizontalArrangement = Arrangement.spacedBy(dimens.gapSmall),
        ) {
            items(CarAppCategory.entries.size) { index ->
                val item = CarAppCategory.entries[index]
                CarChip(item.label, category == item, onClick = { category = item })
            }
        }
        Spacer(Modifier.height(dimens.gapSmall))
        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    glyph = CarGlyph.SEARCH,
                    title = "没有找到应用",
                    subtitle = "换个关键词，或切换到「全部」分类",
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(viewport.layout.columns),
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = dimens.pad,
                    end = dimens.pad,
                    top = dimens.padSmall,
                    bottom = dimens.pad,
                ),
                horizontalArrangement = Arrangement.spacedBy(dimens.gap),
                verticalArrangement = Arrangement.spacedBy(dimens.gap),
            ) {
                items(filtered, key = { it.packageName }) { app ->
                    AppTile(
                        app = app,
                        cellDp = viewport.layout.gridCellDp,
                        onClick = { onOpenApp(app, false) },
                        onLongClick = { selected = app },
                    )
                }
            }
        }
    }

    val app = selected
    if (app != null) {
        AlertDialog(
            onDismissRequest = { selected = null },
            containerColor = palette.surfaceElevated,
            title = { Text(app.label, style = LocalCarTypography.current.cardTitle, color = palette.textPrimary) },
            text = {
                Column {
                    Text(app.packageName, style = LocalCarTypography.current.caption, color = palette.textSecondary)
                    Spacer(Modifier.height(dimens.gap))
                    Text(
                        "选择投屏方式：全屏按车机比例显示；兼容模式保持手机 16:9，左右留黑；手机比例保持竖屏窗口。",
                        style = LocalCarTypography.current.bodySmall,
                        color = palette.textSecondary,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    selected = null
                    onOpenApp(app, false)
                }) { Text("全屏打开", color = palette.primary) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        selected = null
                        onOpenApp(app, true)
                    }) { Text("兼容模式", color = palette.textSecondary) }
                    TextButton(onClick = { selected = null }) { Text("取消", color = palette.textSecondary) }
                }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppTile(
    app: CarAppEntry,
    cellDp: Float,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val palette = LocalCarPalette.current
    val dimens = LocalCarDimens.current
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(dimens.cardRadiusSmall))
            .background(palette.surface.copy(alpha = 0.34f))
            .border(1.dp, palette.outline, RoundedCornerShape(dimens.cardRadiusSmall))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = dimens.padSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size((cellDp * 0.72f).dp)
                .clip(RoundedCornerShape(dimens.cardRadiusSmall))
                .background(
                    Brush.linearGradient(
                        listOf(app.tint.copy(alpha = 0.9f), app.tint.copy(alpha = 0.45f)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (app.icon != null) {
                Image(
                    bitmap = app.icon,
                    contentDescription = app.label,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                )
            } else {
                Text(
                    app.label.take(1),
                    style = LocalCarTypography.current.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(Modifier.height(dimens.gapSmall))
        Text(
            app.label,
            style = LocalCarTypography.current.bodySmall,
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        )
    }
}
