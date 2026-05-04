package com.vibe.app.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibe.app.data.remote.CreatePlaylistBody
import com.vibe.app.data.remote.UserApi
import com.vibe.app.data.remote.toPlaylist
import com.vibe.app.domain.model.Playlist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(private val userApi: UserApi) : ViewModel() {
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists

    init { load() }

    private fun load() {
        viewModelScope.launch { runCatching { _playlists.value = userApi.getMyPlaylists().data.map { it.toPlaylist() } } }
    }

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            runCatching {
                val playlist = userApi.createPlaylist(CreatePlaylistBody(name)).data.toPlaylist()
                _playlists.value = listOf(playlist) + _playlists.value
            }
        }
    }
}
