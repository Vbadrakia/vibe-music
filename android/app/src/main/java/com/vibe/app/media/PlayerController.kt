package com.vibe.app.media

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.vibe.app.BuildConfig
import com.vibe.app.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

    private val _queue = MutableStateFlow<List<com.vibe.app.domain.model.QueueItem>>(emptyList())
    val queue: StateFlow<List<com.vibe.app.domain.model.QueueItem>> = _queue

    private val _shuffleOn = MutableStateFlow(false)
    val shuffleOn: StateFlow<Boolean> = _shuffleOn

    private val _repeatMode = MutableStateFlow(0)
    val repeatMode: StateFlow<Int> = _repeatMode

    private val _lyrics = MutableStateFlow<List<com.vibe.app.domain.model.LyricLine>>(emptyList())
    val lyrics: StateFlow<List<com.vibe.app.domain.model.LyricLine>> = _lyrics

    fun connectToService() {
        val token = SessionToken(context, ComponentName(context, VibeMediaService::class.java))
        controllerFuture = MediaController.Builder(context, token).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            setupListeners()
        }, MoreExecutors.directExecutor())
    }

    private fun setupListeners() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { _isPlaying.value = playing }
        })
    }

    fun playSong(song: Song) {
        _currentSong.value = song
        val item = MediaItem.Builder()
            .setUri("${buildBaseUrl()}${song.streamUrl.removePrefix("/")}")
            .setMediaId(song.id)
            .build()
        mediaController?.apply {
            setMediaItem(item)
            prepare()
            play()
        }
        _isPlaying.value = true
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        _currentSong.value = songs[startIndex]
        _queue.value = songs.mapIndexed { i, s -> com.vibe.app.domain.model.QueueItem(s) }
        val items = songs.map { MediaItem.fromUri("${buildBaseUrl()}${it.streamUrl.removePrefix("/")}") }
        mediaController?.apply {
            setMediaItems(items, startIndex, 0L)
            prepare()
            play()
        }
    }

    fun togglePlayPause() {
        mediaController?.let { ctrl ->
            if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
        }
        _isPlaying.value = !(_isPlaying.value)
    }

    fun skipNext() { mediaController?.seekToNextMediaItem() }
    fun skipPrev() { mediaController?.seekToPreviousMediaItem() }

    fun seekTo(fraction: Float) {
        val duration = mediaController?.duration ?: return
        mediaController?.seekTo((fraction * duration).toLong())
    }

    fun toggleShuffle() {
        val v = !_shuffleOn.value
        _shuffleOn.value = v
        mediaController?.shuffleModeEnabled = v
    }

    fun cycleRepeat() {
        _repeatMode.value = (_repeatMode.value + 1) % 3
        mediaController?.repeatMode = when (_repeatMode.value) {
            0 -> Player.REPEAT_MODE_OFF
            1 -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_ONE
        }
    }

    fun clearQueue() { _queue.value = emptyList() }

    fun playFromQueue(item: com.vibe.app.domain.model.QueueItem) {
        playSong(item.song)
    }

    fun getPositionFraction(): Float {
        val ctrl = mediaController ?: return 0f
        val dur = ctrl.duration.takeIf { it > 0 } ?: return 0f
        return (ctrl.currentPosition.toFloat() / dur).coerceIn(0f, 1f)
    }

    fun getPositionFormatted(): String {
        val pos = (mediaController?.currentPosition ?: 0L) / 1000
        val m = pos / 60; val s = pos % 60
        return "%d:%02d".format(m, s)
    }

    fun release() { MediaController.releaseFuture(controllerFuture ?: return) }

    private fun buildBaseUrl(): String = BuildConfig.BASE_API_URL.trimEnd('/') + "/"
}
