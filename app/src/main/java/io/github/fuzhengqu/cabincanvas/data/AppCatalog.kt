package io.github.fuzhengqu.cabincanvas.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppCatalog {
    private val tints = listOf(
        Color(0xFF2F6BFF), Color(0xFF19A974), Color(0xFFE32E27), Color(0xFFFFB020),
        Color(0xFF8B5CF6), Color(0xFF0EA5E9), Color(0xFF6B7280), Color(0xFFEC4899),
    )

    suspend fun load(context: Context): List<CarAppEntry> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = runCatching {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }.getOrElse { emptyList() }

        resolved
            .asSequence()
            .mapNotNull { info ->
                val activity = info.activityInfo ?: return@mapNotNull null
                val packageName = activity.packageName ?: return@mapNotNull null
                if (packageName == context.packageName) return@mapNotNull null
                val label = runCatching { activity.loadLabel(pm).toString() }.getOrDefault(packageName)
                val icon = runCatching {
                    activity.loadIcon(pm).toBitmap(96, 96).asImageBitmap()
                }.getOrNull()
                CarAppEntry(
                    label = label,
                    packageName = packageName,
                    icon = icon,
                    tint = tints[packageName.hashCode().mod(tints.size)],
                    category = categoryOf(label, packageName),
                )
            }
            .distinctBy { it.packageName }
            .sortedWith(compareBy<CarAppEntry> { it.category.ordinal }.thenBy { it.label })
            .toList()
    }

    private fun categoryOf(label: String, packageName: String): CarAppCategory {
        val key = "$label $packageName".lowercase()
        return when {
            key.contains("地图") || key.contains("导航") || key.contains("amap") || key.contains("baidumap") ->
                CarAppCategory.NAVI
            key.contains("音乐") || key.contains("music") || key.contains("spotify") || key.contains("汽水") ->
                CarAppCategory.MUSIC
            key.contains("视频") || key.contains("抖音") || key.contains("bili") || key.contains("b站") ||
                key.contains("youtube") || key.contains("爱奇艺") || key.contains("优酷") ->
                CarAppCategory.VIDEO
            key.contains("设置") || key.contains("相机") || key.contains("相册") || key.contains("时钟") ||
                key.contains("日历") || key.contains("计算器") || key.contains("文件") || key.contains("settings") ->
                CarAppCategory.TOOL
            else -> CarAppCategory.MINE
        }
    }
}
