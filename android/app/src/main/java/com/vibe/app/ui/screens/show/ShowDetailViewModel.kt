package com.vibe.app.ui.screens.show

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibe.app.data.remote.CatalogApi
import com.vibe.app.domain.model.Show
import com.vibe.app.domain.model.Episode
import com.vibe.app.media.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShowDetailUiState(
    val isLoading: Boolean = true,
    val show: Show? = null,
    val episodes: List<Episode> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ShowDetailViewModel @Inject constructor(
    private val api: CatalogApi,
    private val player: PlayerController,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val showId: String = checkNotNull(savedStateHandle["showId"])

    private val _uiState = MutableStateFlow(ShowDetailUiState())
    val uiState: StateFlow<ShowDetailUiState> = _uiState.asStateFlow()

    init {
        loadShow()
    }

    private fun loadShow() {
        viewModelScope.launch {
            _uiState.value = ShowDetailUiState(isLoading = true)
            runCatching {
                val show = api.getShowById(showId).data
                _uiState.value = ShowDetailUiState(
                    isLoading = false,
                    show = show,
                    episodes = show.episodes
                )
            }.onFailure { e ->
                _uiState.value = ShowDetailUiState(isLoading = false, error = e.message)
            }
        }
    }

    fun playEpisode(episode: Episode) {
        // Since Episodes are slightly different from Songs, we'll need to wrap them or update PlayerController
        // For now, let's assume we can play them if we map them to a Song-like structure
    }
}
