package com.vibe.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibe.app.data.remote.HistoryDto
import com.vibe.app.data.remote.UserApi
import com.vibe.app.domain.model.Song
import com.vibe.app.media.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val userApi: UserApi,
    private val player: PlayerController
) : ViewModel() {
    private val _history = MutableStateFlow<List<HistoryDto>>(emptyList())
    val history: StateFlow<List<HistoryDto>> = _history

    init {
        viewModelScope.launch { runCatching { _history.value = userApi.getListeningHistory().data } }
    }

    fun playSong(song: Song) = player.playSong(song)
}
