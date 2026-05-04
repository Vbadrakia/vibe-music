package com.vibe.app.data.remote

import com.vibe.app.domain.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

// ── Request bodies ─────────────────────────────────────────────────────────────
data class CreatePlaylistBody(val name: String, val description: String? = null)
data class UpdatePlaylistBody(val name: String? = null, val description: String? = null, val song_ids: List<String>? = null)
data class AddSongToPlaylistBody(val songId: String)
data class HistoryBody(val songId: String, val durationMs: Long)

data class PlaylistDetailDto(
    val id: String,
    val name: String,
    val description: String?,
    val cover_url: String?,
    val owner_id: String,
    val owner_name: String,
    val song_count: Int,
    val total_duration_secs: Int,
    val songs: List<Song>,
    val created_at: Long
)

fun PlaylistDetailDto.toPlaylist() = Playlist(
    id = id,
    name = name,
    description = description,
    coverUrl = cover_url,
    ownerId = owner_id,
    ownerName = owner_name,
    songCount = song_count,
    totalDurationSecs = total_duration_secs,
    songs = songs,
    createdAt = created_at
)

data class HistoryDto(val song: Song, val played_at: Long, val type: String)

// ── API interface ──────────────────────────────────────────────────────────────
interface UserApi {

    // ── Playlists ──────────────────────────────────────────────────────────────
    @GET("api/me/playlists")
    suspend fun getMyPlaylists(): ApiResponse<List<PlaylistDetailDto>>
    
    // ── User uploads ─────────────────────────────────────────────────────────
    @Multipart
    @POST("api/me/songs")
    suspend fun uploadSong(
        @Part file: MultipartBody.Part,
        @Part("title") title: RequestBody,
        @Part("artist") artist: RequestBody,
        @Part("album") album: RequestBody,
        @Part("genre") genre: RequestBody,
        @Part("year") year: RequestBody,
        @Part("track_number") trackNumber: RequestBody,
        @Part("duration_secs") durationSecs: RequestBody,
    ): ApiResponse<Song>

    @GET("api/me/playlists/{id}")
    suspend fun getPlaylistById(@Path("id") id: String): ApiResponse<PlaylistDetailDto>

    @POST("api/me/playlists")
    suspend fun createPlaylist(@Body body: CreatePlaylistBody): ApiResponse<PlaylistDetailDto>

    @PUT("api/me/playlists/{id}")
    suspend fun updatePlaylist(@Path("id") id: String, @Body body: UpdatePlaylistBody): ApiResponse<PlaylistDetailDto>

    @DELETE("api/me/playlists/{id}")
    suspend fun deletePlaylist(@Path("id") id: String): ApiResponse<Unit?>

    @POST("api/me/playlists/{id}/songs")
    suspend fun addSongToPlaylist(@Path("id") id: String, @Body body: AddSongToPlaylistBody): ApiResponse<Unit?>

    @DELETE("api/me/playlists/{playlist_id}/songs/{song_id}")
    suspend fun removeSongFromPlaylist(@Path("playlist_id") playlistId: String, @Path("song_id") songId: String): ApiResponse<Unit?>

    // ── Liked Songs ────────────────────────────────────────────────────────────
    @GET("api/me/liked")
    suspend fun getLikedSongs(): ApiResponse<List<Song>>

    @POST("api/me/liked/{song_id}")
    suspend fun likeSong(@Path("song_id") songId: String): ApiResponse<Unit?>

    @DELETE("api/me/liked/{song_id}")
    suspend fun unlikeSong(@Path("song_id") songId: String): ApiResponse<Unit?>

    // ── History ────────────────────────────────────────────────────────────────
    @GET("api/me/history")
    suspend fun getListeningHistory(): ApiResponse<List<HistoryDto>>

    @POST("api/me/history")
    suspend fun recordPlay(@Body body: HistoryBody): ApiResponse<Unit?>

    // ── Following ──────────────────────────────────────────────────────────────
    @GET("api/me/following/artists")
    suspend fun getFollowedArtists(): ApiResponse<List<String>>

    @POST("api/me/following/artists/{id}")
    suspend fun followArtist(@Path("id") artistId: String): ApiResponse<Unit?>

    @DELETE("api/me/following/artists/{id}")
    suspend fun unfollowArtist(@Path("id") artistId: String): ApiResponse<Unit?>
}
