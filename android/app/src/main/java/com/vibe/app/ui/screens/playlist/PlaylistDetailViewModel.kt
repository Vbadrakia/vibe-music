package com.vibe.app.ui.screens.playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibe.app.data.remote.UpdatePlaylistBody
import com.vibe.app.data.remote.UserApi
import com.vibe.app.data.remote.toPlaylist
import com.vibe.app.domain.model.Playlist
import com.vibe.app.domain.model.Song
import com.vibe.app.media.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistDetailUiState(val playlist: Playlist? = null, val isLoading: Boolean = true)

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userApi: UserApi,
    private val player: PlayerController
) : ViewModel() {
    private val playlistId: String = savedStateHandle["playlistId"] ?: ""
    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState

    init { load() }

    private fun load() {
        viewModelScope.launch {
            runCatching {
                val playlist = userApi.getPlaylistById(playlistId).data.toPlaylist()
                _uiState.value = PlaylistDetailUiState(playlist = playlist, isLoading = false)
            }.onFailure { _uiState.value = PlaylistDetailUiState(isLoading = false) }
        }
    }

    fun playAll() { _uiState.value.playlist?.songs?.let { player.playSongs(it) } }
    fun playSong(song: Song) = player.playSong(song)

    fun updatePlaylist(name: String, description: String) {
        viewModelScope.launch {
            runCatching { userApi.updatePlaylist(playlistId, UpdatePlaylistBody(name = name, description = description)) }
        }
    }
}
