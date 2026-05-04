package com.vibe.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibe.app.data.remote.UserApi
import com.vibe.app.data.remote.toPlaylist
import com.vibe.app.domain.model.Artist
import com.vibe.app.domain.model.Playlist
import com.vibe.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val userApi: UserApi) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _recentArtists = MutableStateFlow<List<Artist>>(emptyList())
    val recentArtists: StateFlow<List<Artist>> = _recentArtists

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists

    init {
        viewModelScope.launch {
            runCatching { _playlists.value = userApi.getMyPlaylists().data.map { it.toPlaylist() } }
        }
    }
}
