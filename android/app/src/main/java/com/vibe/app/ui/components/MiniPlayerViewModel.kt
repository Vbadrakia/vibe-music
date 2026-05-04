package com.vibe.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.vibe.app.domain.model.Song
import com.vibe.app.media.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MiniPlayerViewModel @Inject constructor(
    private val player: PlayerController
) : ViewModel() {
    val currentSong: StateFlow<Song?> = player.currentSong
    val isPlaying: StateFlow<Boolean> = player.isPlaying
    val progress: StateFlow<Float> = player.progress

    private val _isLiked = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked

    fun togglePlayPause() = player.togglePlayPause()
    fun toggleLike() { _isLiked.value = !_isLiked.value }
}
