package com.vibe.app.ui.screens.liked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibe.app.data.remote.UserApi
import com.vibe.app.domain.model.Song
import com.vibe.app.media.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LikedSongsViewModel @Inject constructor(
    private val userApi: UserApi,
    private val player: PlayerController
) : ViewModel() {
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs

    init { load() }

    private fun load() {
        viewModelScope.launch { runCatching { _songs.value = userApi.getLikedSongs().data } }
    }

    fun playAll() = player.playSongs(_songs.value)
    fun playSong(song: Song) = player.playSong(song)
    fun unlike(song: Song) {
        viewModelScope.launch {
            runCatching { userApi.unlikeSong(song.id) }
            _songs.value = _songs.value.filter { it.id != song.id }
        }
    }
}
