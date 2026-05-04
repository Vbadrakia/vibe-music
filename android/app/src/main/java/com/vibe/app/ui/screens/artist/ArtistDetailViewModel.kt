package com.vibe.app.ui.screens.artist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibe.app.data.remote.CatalogApi
import com.vibe.app.domain.model.Album
import com.vibe.app.domain.model.Artist
import com.vibe.app.domain.model.Song
import com.vibe.app.media.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArtistDetailUiState(
    val artist: Artist? = null,
    val popularSongs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val isFollowing: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val api: CatalogApi,
    private val player: PlayerController
) : ViewModel() {
    private val artistId: String = savedStateHandle["artistId"] ?: ""
    private val _uiState = MutableStateFlow(ArtistDetailUiState())
    val uiState: StateFlow<ArtistDetailUiState> = _uiState

    init { load() }

    private fun load() {
        viewModelScope.launch {
            runCatching {
                val artist = api.getArtistById(artistId).data
                val songs = api.getSongs(artistId = artistId).data
                val albums = api.getAlbums(artistId = artistId).data
                _uiState.value = ArtistDetailUiState(artist = artist, popularSongs = songs, albums = albums, isLoading = false)
            }.onFailure { _uiState.value = ArtistDetailUiState(isLoading = false) }
        }
    }

    fun playAll() { player.playSongs(uiState.value.popularSongs) }
    fun playSong(song: Song) = player.playSong(song)
    fun toggleFollow() { _uiState.value = _uiState.value.copy(isFollowing = !_uiState.value.isFollowing) }
}
