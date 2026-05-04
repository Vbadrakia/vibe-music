package com.vibe.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibe.app.data.remote.CatalogApi
import com.vibe.app.data.remote.SearchResult
import com.vibe.app.domain.model.Album
import com.vibe.app.domain.model.Artist
import com.vibe.app.domain.model.Genre
import com.vibe.app.domain.model.Song
import com.vibe.app.media.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchResultsUiState(
    val topResult: Song? = null,
    val songs: List<Song> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class SearchResultsViewModel @Inject constructor(
    private val api: CatalogApi,
    private val player: PlayerController
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchResultsUiState())
    val uiState: StateFlow<SearchResultsUiState> = _uiState

    fun search(query: String) {
        viewModelScope.launch {
            _uiState.value = SearchResultsUiState(isLoading = true)
            try {
                val result = api.search(query).data
                _uiState.value = SearchResultsUiState(
                    topResult = result.topResult,
                    songs = result.songs,
                    artists = result.artists,
                    albums = result.albums
                )
            } catch (e: Exception) { _uiState.value = SearchResultsUiState() }
        }
    }

    fun play(song: Song) = player.playSong(song)
}

@HiltViewModel
class SearchViewModel @Inject constructor(private val api: CatalogApi) : ViewModel() {
    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres

    init {
        viewModelScope.launch {
            runCatching { _genres.value = api.getGenres().data }
        }
    }
}
