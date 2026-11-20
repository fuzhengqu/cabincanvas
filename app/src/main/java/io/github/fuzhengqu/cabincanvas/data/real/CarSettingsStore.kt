package io.github.fuzhengqu.cabincanvas.data.real

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.fuzhengqu.cabincanvas.core.CarLayoutMode
import io.github.fuzhengqu.cabincanvas.designsystem.CarThemeMode
import io.github.fuzhengqu.cabincanvas.designsystem.CarWallpaper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.carSettingsDataStore by preferencesDataStore(name = "car_settings")

data class CarSettings(
    val themeMode: CarThemeMode = CarThemeMode.DARK,
    val wallpaper: CarWallpaper = CarWallpaper.AURORA,
    val layoutMode: CarLayoutMode = CarLayoutMode.AUTO,
    val overscanPercent: Int = 0,
)

class CarSettingsStore(private val context: Context) {
    val settings: Flow<CarSettings> = context.carSettingsDataStore.data.map { prefs ->
        CarSettings(
            themeMode = prefs.enumValue(KEY_THEME, CarThemeMode.DARK),
            wallpaper = prefs.enumValue(KEY_WALLPAPER, CarWallpaper.AURORA),
            layoutMode = prefs.enumValue(KEY_LAYOUT, CarLayoutMode.AUTO),
            overscanPercent = (prefs[KEY_OVERSCAN] ?: 0).coerceIn(0, 8),
        )
    }

    suspend fun setThemeMode(mode: CarThemeMode) {
        context.carSettingsDataStore.edit { it[KEY_THEME] = mode.name }
    }

    suspend fun setWallpaper(wallpaper: CarWallpaper) {
        context.carSettingsDataStore.edit { it[KEY_WALLPAPER] = wallpaper.name }
    }

    suspend fun setLayoutMode(mode: CarLayoutMode) {
        context.carSettingsDataStore.edit { it[KEY_LAYOUT] = mode.name }
    }

    suspend fun setOverscan(percent: Int) {
        context.carSettingsDataStore.edit { it[KEY_OVERSCAN] = percent.coerceIn(0, 8) }
    }

    private inline fun <reified T : Enum<T>> Preferences.enumValue(key: Preferences.Key<String>, fallback: T): T {
        val raw = this[key] ?: return fallback
        return runCatching { enumValueOf<T>(raw) }.getOrDefault(fallback)
    }

    private companion object {
        val KEY_THEME = stringPreferencesKey("theme_mode")
        val KEY_WALLPAPER = stringPreferencesKey("wallpaper")
        val KEY_LAYOUT = stringPreferencesKey("layout_mode")
        val KEY_OVERSCAN = intPreferencesKey("overscan_percent")
    }
}
