package com.vibe.app.data.remote

import com.vibe.app.domain.model.Playlist
import com.vibe.app.domain.model.Song

/**
 * Maps the DTO received from the backend to our clean domain [Playlist] model.
 * Keeps the network/domain boundary explicit.
 */
fun PlaylistDetailDto.toDomain(): Playlist = Playlist(
    id               = id,
    name             = name,
    description      = description,
    coverUrl         = cover_url,
    ownerId          = owner_id,
    ownerName        = owner_name,
    songCount        = song_count,
    totalDurationSecs = total_duration_secs,
    songs            = songs,
    createdAt        = created_at
)

/**
 * Extension to map a list of [PlaylistDetailDto] to [Playlist] domain objects.
 */
fun List<PlaylistDetailDto>.toDomainList(): List<Playlist> = map { it.toDomain() }
