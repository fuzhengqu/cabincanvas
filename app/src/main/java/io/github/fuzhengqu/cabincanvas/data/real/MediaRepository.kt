package io.github.fuzhengqu.cabincanvas.data.real

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.SystemClock
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import io.github.fuzhengqu.cabincanvas.data.CarMedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MediaUiState(
    val media: CarMedia? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val packageName: String? = null,
)

/**
 * Real media sessions via MediaSessionManager. Requires notification-listener
 * access; without it, the UI falls back to demo data.
 */
class MediaRepository(private val context: Context) {
    private val manager = context.getSystemService(MediaSessionManager::class.java)
    private val listenerComponent = ComponentName(context, CarNotificationListenerService::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(MediaUiState())
    val state: StateFlow<MediaUiState> = _state.asStateFlow()

    private var controller: MediaController? = null
    private var lastPosition = 0L
    private var lastTickAt = SystemClock.elapsedRealtime()
    private var started = false

    private val callback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = update()
        override fun onPlaybackStateChanged(state: PlaybackState?) = update()
        override fun onSessionDestroyed() {
            controller = null
            _state.value = MediaUiState()
            refresh()
        }
    }

    private val sessionsChanged = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        scope.launch { bind(controllers) }
    }

    fun start() {
        if (started) return
        started = true
        runCatching {
            manager?.addOnActiveSessionsChangedListener(sessionsChanged, listenerComponent)
        }
        refresh()
        scope.launch {
            while (true) {
                delay(1000)
                tick()
            }
        }
    }

    fun refresh() {
        runCatching {
            bind(manager?.getActiveSessions(listenerComponent))
        }
    }

    fun stop() {
        if (!started) return
        started = false
        runCatching { manager?.removeOnActiveSessionsChangedListener(sessionsChanged) }
        runCatching { controller?.unregisterCallback(callback) }
        controller = null
        scope.cancel()
    }

    fun toggle() {
        val state = _state.value
        if (state.media == null) return
        if (state.isPlaying) controller?.transportControls?.pause() else controller?.transportControls?.play()
    }

    fun next() {
        controller?.transportControls?.skipToNext()
    }

    fun previous() {
        controller?.transportControls?.skipToPrevious()
    }

    fun seekTo(positionMs: Long) {
        controller?.transportControls?.seekTo(positionMs.coerceAtLeast(0L))
    }

    private fun bind(controllers: List<MediaController>?) {
        val list = controllers.orEmpty()
        if (list.isEmpty()) {
            runCatching { controller?.unregisterCallback(callback) }
            controller = null
            _state.value = MediaUiState()
            return
        }
        val chosen = list.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING } ?: list.first()
        if (chosen.sessionToken != controller?.sessionToken) {
            runCatching { controller?.unregisterCallback(callback) }
            controller = chosen
            runCatching { chosen.registerCallback(callback) }
        }
        update()
    }

    private fun update() {
        val current = controller
        val metadata = current?.metadata
        val playback = current?.playbackState
        if (current == null || metadata == null) {
            _state.value = MediaUiState()
            return
        }
        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty().ifBlank { "未知曲目" }
        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST).orEmpty().ifBlank { "未知艺术家" }
        val album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM).orEmpty()
        val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION).coerceAtLeast(1L)
        val artwork = artwork(metadata)
        lastPosition = playback?.position?.coerceAtLeast(0L) ?: 0L
        lastTickAt = SystemClock.elapsedRealtime()
        _state.value = MediaUiState(
            media = CarMedia(
                title = title,
                artist = artist,
                album = album,
                durationMs = duration,
                accent = accentFor(current.packageName),
                artwork = artwork,
                packageName = current.packageName,
            ),
            isPlaying = playback?.state == PlaybackState.STATE_PLAYING,
            positionMs = lastPosition,
            packageName = current.packageName,
        )
    }

    private fun tick() {
        val state = _state.value
        if (state.media == null || !state.isPlaying) return
        val now = SystemClock.elapsedRealtime()
        val position = (lastPosition + (now - lastTickAt)).coerceAtMost(state.media.durationMs)
        _state.value = state.copy(positionMs = position)
        if (position >= state.media.durationMs) {
            lastPosition = 0L
            lastTickAt = now
        }
    }

    private fun artwork(metadata: MediaMetadata): androidx.compose.ui.graphics.ImageBitmap? {
        val bitmap = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: return null
        val max = 512
        val width = bitmap.width.coerceAtLeast(1)
        val height = bitmap.height.coerceAtLeast(1)
        val scale = minOf(max.toFloat() / width, max.toFloat() / height, 1f)
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(bitmap, (width * scale).toInt().coerceAtLeast(1), (height * scale).toInt().coerceAtLeast(1), true)
        } else {
            bitmap
        }
        return scaled.asImageBitmap()
    }

    private fun accentFor(packageName: String): Color {
        val colors = listOf(
            Color(0xFF2F6BFF), Color(0xFF19A974), Color(0xFFE32E27),
            Color(0xFFFFB020), Color(0xFF8B5CF6), Color(0xFF0EA5E9),
        )
        return colors[packageName.hashCode().mod(colors.size)]
    }
}
