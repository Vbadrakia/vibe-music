package com.vibe.app.data.remote

import com.vibe.app.domain.model.*
import okhttp3.MultipartBody
import retrofit2.http.*

// ── Response wrappers ──────────────────────────────────────────────────────────
data class ApiResponse<T>(val success: Boolean, val data: T)
data class SearchResult(val topResult: Song?, val songs: List<Song>, val artists: List<Artist>, val albums: List<Album>)
data class HomeData(
    val recentlyPlayed: List<Song>,
    val madeForYou: List<PlaylistDto>,
    val newReleases: List<Album>,
    val featuredPlaylists: List<PlaylistDto>,
    val trendingShows: List<Show> = emptyList()
)
data class PlaylistDto(val id: String, val name: String, val description: String?, val coverUrl: String?, val songCount: Int)

// ── API interface ──────────────────────────────────────────────────────────────
interface CatalogApi {

    // Songs
    @GET("api/songs")
    suspend fun getSongs(
        @Query("genre")     genre: String?     = null,
        @Query("artist_id") artistId: String?  = null,
        @Query("album_id")  albumId: String?   = null,
        @Query("q")         query: String?     = null,
        @Query("limit")     limit: Int         = 50,
        @Query("offset")    offset: Int        = 0,
    ): ApiResponse<List<Song>>

    @GET("api/songs/{id}")
    suspend fun getSongById(@Path("id") id: String): ApiResponse<Song>

    // Albums
    @GET("api/albums")
    suspend fun getAlbums(
        @Query("artist_id") artistId: String? = null,
        @Query("limit")     limit: Int        = 20,
    ): ApiResponse<List<Album>>

    @GET("api/albums/{id}")
    suspend fun getAlbumById(@Path("id") id: String): ApiResponse<Album>

    // Artists
    @GET("api/artists")
    suspend fun getArtists(
        @Query("q")     query: String? = null,
        @Query("limit") limit: Int     = 20,
    ): ApiResponse<List<Artist>>

    @GET("api/artists/{id}")
    suspend fun getArtistById(@Path("id") id: String): ApiResponse<Artist>

    // Genres
    @GET("api/genres")
    suspend fun getGenres(): ApiResponse<List<Genre>>

    @GET("api/genres/{id}/songs")
    suspend fun getGenreSongs(@Path("id") genreId: String): ApiResponse<List<Song>>

    // Search
    @GET("api/search")
    suspend fun search(@Query("q") query: String): ApiResponse<SearchResult>

    // Home feed
    @GET("api/home")
    suspend fun getHome(): ApiResponse<HomeData>

    // Lyrics
    @GET("api/lyrics/{songId}")
    suspend fun getLyrics(@Path("songId") songId: String): ApiResponse<List<LyricLine>>

    // Shows & Podcasts
    @GET("api/shows")
    suspend fun getShows(
        @Query("q")     query: String? = null,
        @Query("limit") limit: Int     = 20,
    ): ApiResponse<List<Show>>

    @GET("api/shows/{id}")
    suspend fun getShowById(@Path("id") id: String): ApiResponse<Show>

    @GET("api/episodes/{id}")
    suspend fun getEpisodeById(@Path("id") id: String): ApiResponse<Episode>

    // Upload
    @Multipart
    @POST("api/songs/upload")
    suspend fun uploadSong(
        @Part file: MultipartBody.Part
    ): ApiResponse<Song>
}
