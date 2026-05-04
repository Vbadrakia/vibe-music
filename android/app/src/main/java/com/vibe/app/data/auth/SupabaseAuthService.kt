package com.vibe.app.data.auth

import com.vibe.app.BuildConfig
import com.vibe.app.di.TokenStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lightweight Supabase Auth client using raw HTTP calls —
 * avoids pulling in the full Supabase SDK just for auth.
 *
 * Replace with supabase-kt Auth if you want richer features
 * (OAuth, session refresh, etc.).
 */
@Singleton
class SupabaseAuthService @Inject constructor(
    private val tokenStore: TokenStore
) {
    private val baseUrl = BuildConfig.SUPABASE_URL
    private val anonKey = BuildConfig.SUPABASE_ANON_KEY

    data class AuthResult(
        val accessToken: String,
        val refreshToken: String,
        val userId: String,
        val email: String
    )

    /** Sign in with email + password. */
    suspend fun signIn(email: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            val body = JSONObject().apply {
                put("email", email)
                put("password", password)
            }.toString()
            val json = post("$baseUrl/auth/v1/token?grant_type=password", body)
            parseSession(json)
        }

    /** Sign up with email + password. */
    suspend fun signUp(email: String, password: String, displayName: String? = null): AuthResult =
        withContext(Dispatchers.IO) {
            val bodyObj = JSONObject().apply {
                put("email", email)
                put("password", password)
                if (displayName != null) {
                    put("data", JSONObject().apply {
                        put("display_name", displayName)
                    })
                }
            }
            val json = post("$baseUrl/auth/v1/signup", bodyObj.toString())
            // Supabase returns session immediately if email confirmation is disabled
            if (json.has("access_token")) parseSession(json)
            else throw Exception("Please check your email to confirm your account.")
        }

    /** Send password reset email. */
    suspend fun resetPassword(email: String) = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("email", email)
        }.toString()
        post("$baseUrl/auth/v1/recover", body)
    }

    /** Refresh the access token using refresh_token. */
    suspend fun refreshSession(): AuthResult? = withContext(Dispatchers.IO) {
        val refresh = tokenStore.refreshToken ?: return@withContext null
        runCatching {
            val body = JSONObject().apply {
                put("refresh_token", refresh)
            }.toString()
            val json = post("$baseUrl/auth/v1/token?grant_type=refresh_token", body)
            val result = parseSession(json)
            tokenStore.accessToken = result.accessToken
            tokenStore.refreshToken = result.refreshToken
            result
        }.getOrNull()
    }

    /** Sign out (clears local tokens). */
    fun signOut() {
        tokenStore.accessToken = null
        tokenStore.refreshToken = null
    }

    val isLoggedIn: Boolean get() = !tokenStore.accessToken.isNullOrBlank()

    // ── Internal ───────────────────────────────────────────────────────────────
    private fun post(urlStr: String, body: String): JSONObject {
        Log.d("SupabaseAuth", "POST to $urlStr body: $body")
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("apikey", anonKey)
        conn.doOutput = true

        OutputStreamWriter(conn.outputStream).use { it.write(body) }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val response = stream.bufferedReader().readText()
        Log.d("SupabaseAuth", "Response code $code, body: $response")
        val json = JSONObject(response)

        if (code !in 200..299) {
            val msg = json.optString("error_description")
                .ifBlank { json.optString("msg", "Auth failed ($code)") }
            throw Exception(msg)
        }
        return json
    }

    private fun parseSession(json: JSONObject): AuthResult {
        val user = json.optJSONObject("user")
        return AuthResult(
            accessToken  = json.getString("access_token"),
            refreshToken = json.optString("refresh_token"),
            userId       = user?.optString("id") ?: json.optString("id", ""),
            email        = user?.optString("email") ?: json.optString("email", "")
        )
    }
}
