package com.vibe.app.di

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "vibe_prefs")

object PrefKeys {
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    val USER_ID = stringPreferencesKey("user_id")
    val USER_EMAIL = stringPreferencesKey("user_email")
}

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var _accessToken: String? = null
    private var _refreshToken: String? = null

    init {
        runBlocking {
            val prefs = context.dataStore.data.first()
            _accessToken = prefs[PrefKeys.ACCESS_TOKEN]
            _refreshToken = prefs[PrefKeys.REFRESH_TOKEN]
        }
    }

    var accessToken: String?
        get() = _accessToken
        set(value) {
            _accessToken = value
            runBlocking {
                context.dataStore.edit { prefs ->
                    if (value == null) prefs.remove(PrefKeys.ACCESS_TOKEN)
                    else prefs[PrefKeys.ACCESS_TOKEN] = value
                }
            }
        }

    var refreshToken: String?
        get() = _refreshToken
        set(value) {
            _refreshToken = value
            runBlocking {
                context.dataStore.edit { prefs ->
                    if (value == null) prefs.remove(PrefKeys.REFRESH_TOKEN)
                    else prefs[PrefKeys.REFRESH_TOKEN] = value
                }
            }
        }
}

@Singleton
class TokenInterceptor @Inject constructor(
    private val tokenStore: TokenStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenStore.accessToken
        val request = if (token.isNullOrBlank()) chain.request()
        else chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}

