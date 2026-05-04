package com.vibe.app.ui.screens.library

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibe.app.data.remote.CreatePlaylistBody
import com.vibe.app.data.remote.UserApi
import com.vibe.app.data.remote.toPlaylist
import com.vibe.app.domain.model.Playlist
import com.vibe.app.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.source
import java.io.IOException
import javax.inject.Inject

data class UploadUiState(
    val isUploading: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val uploadedSong: Song? = null,
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userApi: UserApi
) : ViewModel() {
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists

    private val _uploadState = MutableStateFlow(UploadUiState())
    val uploadState: StateFlow<UploadUiState> = _uploadState.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            runCatching {
                _playlists.value = userApi.getMyPlaylists().data.map { it.toPlaylist() }
            }
        }
    }

    fun clearUploadStatus() {
        _uploadState.update { it.copy(message = null, error = null, uploadedSong = null) }
    }

    fun uploadSong(uri: Uri) {
        viewModelScope.launch {
            _uploadState.value = UploadUiState(isUploading = true)

            runCatching {
                withContext(Dispatchers.IO) {
                    val fileName = resolveFileName(uri)
                    val mimeType = context.contentResolver.getType(uri) ?: "audio/mpeg"
                    val tags = extractMetadata(uri)

                    val requestBody = object : RequestBody() {
                        override fun contentType() = mimeType.toMediaTypeOrNull()

                        override fun writeTo(sink: okio.BufferedSink) {
                            val stream = context.contentResolver.openInputStream(uri)
                                ?: throw IOException("Unable to open selected audio file")
                            stream.use { input -> sink.writeAll(input.source()) }
                        }
                    }

                    val part = MultipartBody.Part.createFormData("file", fileName, requestBody)

                    userApi.uploadSong(
                        file = part,
                        title = tags.title.toRequestBody("text/plain".toMediaTypeOrNull()),
                        artist = tags.artist.toRequestBody("text/plain".toMediaTypeOrNull()),
                        album = tags.album.toRequestBody("text/plain".toMediaTypeOrNull()),
                        genre = tags.genre.toRequestBody("text/plain".toMediaTypeOrNull()),
                        year = tags.year.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                        trackNumber = tags.trackNumber.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                        durationSecs = tags.durationSecs.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    ).data
                }
            }.onSuccess { song ->
                _uploadState.value = UploadUiState(
                    isUploading = false,
                    message = "Uploaded ${song.title}",
                    uploadedSong = song
                )
                load()
            }.onFailure { error ->
                _uploadState.value = UploadUiState(
                    isUploading = false,
                    error = error.message ?: "Upload failed"
                )
            }
        }
    }

    private data class SongTags(
        val title: String,
        val artist: String,
        val album: String,
        val genre: String,
        val year: Int,
        val trackNumber: Int,
        val durationSecs: Int,
    )

    private fun extractMetadata(uri: Uri): SongTags {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)

            val displayName = resolveFileName(uri)
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?.takeIf { it.isNotBlank() }
                ?: displayName.substringBeforeLast('.')
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?.takeIf { it.isNotBlank() }
                ?: "Unknown Artist"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?.takeIf { it.isNotBlank() }
                ?: "Single"
            val genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                ?.takeIf { it.isNotBlank() }
                ?: ""
            val year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                ?.toIntOrNull()
                ?: 0
            val trackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                ?.substringBefore('/')
                ?.toIntOrNull()
                ?: 0
            val durationSecs = (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?.div(1000L)
                ?: 0L)
                .toInt()

            return SongTags(
                title = title,
                artist = artist,
                album = album,
                genre = genre,
                year = year,
                trackNumber = trackNumber,
                durationSecs = durationSecs.coerceAtLeast(1),
            )
        } finally {
            retriever.release()
        }
    }

    private fun resolveFileName(uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val nameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameColumn >= 0 && cursor.moveToFirst()) {
                val name = cursor.getString(nameColumn)
                if (!name.isNullOrBlank()) return name
            }
        }
        return "upload_${System.currentTimeMillis()}.mp3"
    }

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            runCatching {
                val playlist = userApi.createPlaylist(CreatePlaylistBody(name)).data.toPlaylist()
                _playlists.value = listOf(playlist) + _playlists.value
            }
        }
    }
}
