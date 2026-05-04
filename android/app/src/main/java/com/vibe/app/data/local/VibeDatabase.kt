package com.vibe.app.data.local

import androidx.room.*
import com.vibe.app.domain.model.Song
import com.vibe.app.domain.model.Album
import com.vibe.app.domain.model.Artist
import kotlinx.coroutines.flow.Flow

// ── Entities ──────────────────────────────────────────────────────────────────

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val artistId: String,
    val album: String,
    val albumId: String,
    val durationSecs: Int,
    val coverUrl: String,
    val driveFileId: String,
    val genre: String?,
    val trackNumber: Int?,
    val year: Int?,
    val lyricsLrcUrl: String?,
    val isLiked: Boolean = false,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artistId: String,
    val artist: String,
    val year: Int,
    val coverUrl: String,
    val songCount: Int,
    val totalDurationSecs: Int
)

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val coverUrl: String,
    val bio: String?,
    val monthlyListeners: Long,
    val isVerified: Boolean
)

// ── Mappers ───────────────────────────────────────────────────────────────────

fun Song.toEntity(isLiked: Boolean = false) = SongEntity(
    id, title, artist, artistId, album, albumId,
    durationSecs, coverUrl, driveFileId, genre, trackNumber, year, lyricsLrcUrl, isLiked
)

fun SongEntity.toDomain() = Song(
    id, title, artist, artistId, album, albumId,
    durationSecs, coverUrl, driveFileId, genre, trackNumber, year, lyricsLrcUrl
)

// ── DAOs ──────────────────────────────────────────────────────────────────────

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isLiked = 1 ORDER BY cachedAt DESC")
    fun getLikedSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE albumId = :albumId ORDER BY trackNumber ASC")
    fun getSongsByAlbum(albumId: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE artistId = :artistId ORDER BY title ASC")
    fun getSongsByArtist(artistId: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: String): SongEntity?

    @Upsert
    suspend fun upsertSongs(songs: List<SongEntity>)

    @Query("UPDATE songs SET isLiked = :liked WHERE id = :id")
    suspend fun setLiked(id: String, liked: Boolean)

    @Query("DELETE FROM songs WHERE cachedAt < :before")
    suspend fun deleteOldCache(before: Long)
}

@Dao
interface AlbumDao {
    @Query("SELECT * FROM albums ORDER BY year DESC")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE id = :id")
    suspend fun getAlbumById(id: String): AlbumEntity?

    @Upsert
    suspend fun upsertAlbums(albums: List<AlbumEntity>)
}

@Dao
interface ArtistDao {
    @Query("SELECT * FROM artists ORDER BY name ASC")
    fun getAllArtists(): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM artists WHERE id = :id")
    suspend fun getArtistById(id: String): ArtistEntity?

    @Upsert
    suspend fun upsertArtists(artists: List<ArtistEntity>)
}

// ── Database ──────────────────────────────────────────────────────────────────

@Database(
    entities = [SongEntity::class, AlbumEntity::class, ArtistEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VibeDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
}
