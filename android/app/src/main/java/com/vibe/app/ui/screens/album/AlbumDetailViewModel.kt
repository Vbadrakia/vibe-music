package com.vibe.app.ui.screens.album

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibe.app.data.remote.CatalogApi
import com.vibe.app.domain.model.Album
import com.vibe.app.domain.model.Song
import com.vibe.app.media.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumDetailUiState(val album: Album? = null, val songs: List<Song> = emptyList(), val isLoading: Boolean = true)

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val api: CatalogApi,
    private val player: PlayerController
) : ViewModel() {
    private val albumId: String = savedStateHandle["albumId"] ?: ""
    private val _uiState = MutableStateFlow(AlbumDetailUiState())
    val uiState: StateFlow<AlbumDetailUiState> = _uiState

    private val _dominantColor = MutableStateFlow<Color?>(null)
    val dominantColor: StateFlow<Color?> = _dominantColor

    val currentSongId: String? get() = player.currentSong.value?.id

    init { load() }

    private fun load() {
        viewModelScope.launch {
            runCatching {
                val album = api.getAlbumById(albumId).data
                _uiState.value = AlbumDetailUiState(album = album, songs = album.songs, isLoading = false)
            }.onFailure { _uiState.value = AlbumDetailUiState(isLoading = false) }
        }
    }

    fun playAll() { uiState.value.songs.takeIf { it.isNotEmpty() }?.let { player.playSongs(it) } }
    fun playSong(song: Song) = player.playSong(song)
}
