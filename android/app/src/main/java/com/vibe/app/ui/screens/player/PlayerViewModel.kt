package com.vibe.app.ui.screens.player

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibe.app.domain.model.LyricLine
import com.vibe.app.domain.model.QueueItem
import com.vibe.app.domain.model.Song
import com.vibe.app.media.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val player: PlayerController
) : ViewModel() {

    val currentSong: StateFlow<Song?> = player.currentSong
    val isPlaying: StateFlow<Boolean> = player.isPlaying
    val progress: StateFlow<Float> = player.progress
    val queue: StateFlow<List<QueueItem>> = player.queue
    val shuffleOn: StateFlow<Boolean> = player.shuffleOn
    val repeatMode: StateFlow<Int> = player.repeatMode
    val lyrics: StateFlow<List<LyricLine>> = player.lyrics

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked

    private val _dominantColor = MutableStateFlow<Color?>(null)
    val dominantColor: StateFlow<Color?> = _dominantColor

    private val _currentLyricIndex = MutableStateFlow(-1)
    val currentLyricIndex: StateFlow<Int> = _currentLyricIndex

    val positionFormatted: String get() = player.getPositionFormatted()

    init {
        // Poll progress every 500ms
        viewModelScope.launch {
            while (true) {
                delay(500)
                // progress updated by PlayerController internally
            }
        }
    }

    fun togglePlayPause() = player.togglePlayPause()
    fun skipNext() = player.skipNext()
    fun skipPrev() = player.skipPrev()
    fun seekTo(f: Float) = player.seekTo(f)
    fun toggleShuffle() = player.toggleShuffle()
    fun cycleRepeat() = player.cycleRepeat()
    fun clearQueue() = player.clearQueue()
    fun playFromQueue(item: QueueItem) = player.playFromQueue(item)

    fun toggleLike() { _isLiked.value = !_isLiked.value }
}
