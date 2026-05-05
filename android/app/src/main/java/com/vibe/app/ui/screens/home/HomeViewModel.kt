package com.vibe.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibe.app.data.remote.CatalogApi
import com.vibe.app.data.remote.PlaylistDto
import com.vibe.app.domain.model.Album
import com.vibe.app.domain.model.Song
import com.vibe.app.domain.model.Show
import com.vibe.app.media.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val recentlyPlayed: List<Song> = emptyList(),
    val recommendedSongs: List<Song> = emptyList(),
    val madeForYou: List<PlaylistDto> = emptyList(),
    val newReleases: List<Album> = emptyList(),
    val featuredPlaylists: List<PlaylistDto> = emptyList(),
    val trendingShows: List<Show> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val api: CatalogApi,
    private val player: PlayerController
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val currentSong = player.currentSong
    val isPlaying   = player.isPlaying

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true, error = null)
            runCatching {
                val home = api.getHome().data
                val recommended = api.getSongs(limit = 10).data
                _uiState.value = HomeUiState(
                    isLoading        = false,
                    recentlyPlayed   = home.recentlyPlayed,
                    recommendedSongs = recommended,
                    madeForYou       = home.madeForYou,
                    newReleases      = home.newReleases,
                    featuredPlaylists = home.featuredPlaylists,
                    trendingShows     = home.trendingShows
                )
            }.onFailure { e ->
                _uiState.value = HomeUiState(isLoading = false, error = e.message)
            }
        }
    }

    fun play(song: Song) = player.playSong(song)
    fun playAll(songs: List<Song>) { if (songs.isNotEmpty()) player.playSongs(songs) }
}
