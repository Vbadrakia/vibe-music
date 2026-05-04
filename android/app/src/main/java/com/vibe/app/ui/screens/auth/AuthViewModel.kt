package com.vibe.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibe.app.data.auth.SupabaseAuthService
import com.vibe.app.di.TokenStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle    : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val msg: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: SupabaseAuthService,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill in all fields.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching { auth.signIn(email.trim(), password) }
                .onSuccess { session ->
                    tokenStore.accessToken  = session.accessToken
                    tokenStore.refreshToken = session.refreshToken
                    _uiState.value = AuthUiState.Success
                }
                .onFailure { e -> _uiState.value = AuthUiState.Error(e.message ?: "Login failed.") }
        }
    }

    fun register(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill in all fields.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching { auth.signUp(email.trim(), password, displayName = name.trim()) }
                .onSuccess { session ->
                    tokenStore.accessToken  = session.accessToken
                    tokenStore.refreshToken = session.refreshToken
                    _uiState.value = AuthUiState.Success
                }
                .onFailure { e -> _uiState.value = AuthUiState.Error(e.message ?: "Registration failed.") }
        }
    }

    fun forgotPassword(email: String, onSent: () -> Unit) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your email.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching { auth.resetPassword(email.trim()) }
                .onSuccess { _uiState.value = AuthUiState.Idle; onSent() }
                .onFailure { e -> _uiState.value = AuthUiState.Error(e.message ?: "Failed to send email.") }
        }
    }

    fun logout() {
        auth.signOut()
        _uiState.value = AuthUiState.Idle
    }

    fun resetState() { _uiState.value = AuthUiState.Idle }

    // Google / Apple OAuth — requires WebView flow; wired in Phase 10
    fun loginWithGoogle() { _uiState.value = AuthUiState.Error("Google sign-in coming soon.") }
    fun loginWithApple()  { _uiState.value = AuthUiState.Error("Apple sign-in coming soon.") }
}
