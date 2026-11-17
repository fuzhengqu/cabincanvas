package io.github.fuzhengqu.cabincanvas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CanvasDark = darkColorScheme(
    primary = Color(0xFF2F6BFF),
    onPrimary = Color.White,
    secondary = Color(0xFF19A974),
    onSecondary = Color.White,
    background = Color(0xFF0B0D12),
    onBackground = Color(0xFFE8EAF2),
    surface = Color(0xFF151823),
    onSurface = Color(0xFFE8EAF2),
    surfaceVariant = Color(0xFF1E2230),
    onSurfaceVariant = Color(0xFFA7ADBD),
    outline = Color(0xFF343A4D),
)

@Composable
fun CabinCanvasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CanvasDark,
        content = content,
    )
}
