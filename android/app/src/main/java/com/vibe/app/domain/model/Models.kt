package com.vibe.app.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    @Json(name = "artist_id") val artistId: String,
    val album: String,
    @Json(name = "album_id") val albumId: String,
    @Json(name = "duration_secs") val durationSecs: Int,
    @Json(name = "cover_url") val coverUrl: String,
    @Json(name = "drive_file_id") val driveFileId: String,
    val genre: String? = null,
    @Json(name = "track_number") val trackNumber: Int? = null,
    val year: Int? = null,
    @Json(name = "lyrics_lrc_url") val lyricsLrcUrl: String? = null
) {
    val streamUrl: String get() = "/api/stream/$driveFileId"
    val durationFormatted: String get() {
        val m = durationSecs / 60
        val s = durationSecs % 60
        return "%d:%02d".format(m, s)
    }
}

@JsonClass(generateAdapter = true)
data class Album(
    val id: String,
    val title: String,
    @Json(name = "artist_id") val artistId: String,
    val artist: String,
    val year: Int? = null,
    @Json(name = "cover_url") val coverUrl: String,
    @Json(name = "song_count") val songCount: Int? = null,
    @Json(name = "total_duration_secs") val totalDurationSecs: Int? = null,
    val songs: List<Song> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Artist(
    val id: String,
    val name: String,
    @Json(name = "cover_url") val coverUrl: String,
    val bio: String? = null,
    @Json(name = "monthly_listeners") val monthlyListeners: Long = 0L,
    @Json(name = "is_verified") val isVerified: Boolean = false,
    val genres: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Genre(
    val id: String,
    val name: String,
    val description: String,
    @Json(name = "cover_url") val coverUrl: String,
    val color: String = "#1DB954"
)

data class Playlist(
    val id: String,
    val name: String,
    val description: String? = null,
    val coverUrl: String? = null,
    val ownerId: String,
    val ownerName: String,
    val songCount: Int = 0,
    val totalDurationSecs: Int = 0,
    val songs: List<Song> = emptyList(),
    val isLiked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val followingCount: Int = 0,
    val followersCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class Show(
    val id: String,
    val name: String,
    val publisher: String,
    val description: String,
    @Json(name = "cover_url") val coverUrl: String,
    @Json(name = "is_explicit") val isExplicit: Boolean = false,
    val episodes: List<Episode> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Episode(
    val id: String,
    val title: String,
    val description: String,
    @Json(name = "duration_secs") val durationSecs: Int,
    @Json(name = "release_date") val releaseDate: String,
    @Json(name = "audio_url") val audioUrl: String,
    @Json(name = "cover_url") val coverUrl: String,
    @Json(name = "show_id") val showId: String,
    @Json(name = "show_name") val showName: String
)

data class LyricLine(
    val timeMs: Long,
    val text: String
)

data class QueueItem(
    val song: Song,
    val id: String = java.util.UUID.randomUUID().toString()
)
