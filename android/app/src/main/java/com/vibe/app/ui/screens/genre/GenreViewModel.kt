package com.vibe.app.ui.screens.genre

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibe.app.data.remote.CatalogApi
import com.vibe.app.data.remote.PlaylistDto
import com.vibe.app.domain.model.Genre
import com.vibe.app.media.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GenreUiState(val genre: Genre? = null, val playlists: List<PlaylistDto> = emptyList())

@HiltViewModel
class GenreViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val api: CatalogApi,
    private val player: PlayerController
) : ViewModel() {
    private val genreId: String = savedStateHandle["genreId"] ?: ""
    private val _uiState = MutableStateFlow(GenreUiState())
    val uiState: StateFlow<GenreUiState> = _uiState

    init {
        viewModelScope.launch {
            runCatching {
                val genres = api.getGenres().data
                val genre = genres.firstOrNull { it.id == genreId }
                val songs = api.getGenreSongs(genreId).data
                _uiState.value = GenreUiState(genre = genre)
            }
        }
    }

    fun playAll() { /* player.playSongs(songs) */ }
}
