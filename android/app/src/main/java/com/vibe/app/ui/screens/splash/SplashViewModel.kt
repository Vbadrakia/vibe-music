package com.vibe.app.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.vibe.app.di.TokenStore

sealed class AuthState { object Loading : AuthState(); object Authenticated : AuthState(); object Unauthenticated : AuthState() }

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenStore: TokenStore
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    init {
        viewModelScope.launch {
            _authState.value = if (!tokenStore.accessToken.isNullOrBlank()) AuthState.Authenticated
                               else AuthState.Unauthenticated
        }
    }
}
