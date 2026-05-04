package com.vibe.app.presentation.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibe.app.data.remote.CatalogApi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.parse
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

data class UploadUiState(
    val isUploading: Boolean = false,
    val uploadProgress: Int = 0,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class UploadViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val catalogApi: CatalogApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState

    private val _selectedFile = MutableStateFlow<Uri?>(null)
    val selectedFile: StateFlow<Uri?> = _selectedFile

    fun selectFile(uri: Uri) {
        _selectedFile.value = uri
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun uploadSong(fileUri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isUploading = true,
                    errorMessage = null,
                    successMessage = null
                )

                // Get the file from content URI
                val file = uriToFile(fileUri)

                // Determine MIME type from file extension
                val mimeType = getMimeTypeFromUri(fileUri)

                // Create request body
                val requestFile = file.asRequestBody(mimeType.parse())

                // Create multipart body
                val body = MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    requestFile
                )

                // Upload to backend
                val response = catalogApi.uploadSong(body)

                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        uploadProgress = 100,
                        successMessage = "Song '${response.data.title}' uploaded successfully!",
                    )
                    // Reset after 2 seconds
                    kotlinx.coroutines.delay(2000)
                    _selectedFile.value = null
                    _uiState.value = _uiState.value.copy(
                        uploadProgress = 0,
                        successMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        errorMessage = "Upload failed: ${response.data?.title ?: "Unknown error"}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    errorMessage = "Error: ${e.message ?: "Unknown error occurred"}"
                )
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Unable to open file")

        val fileName = "temp_audio_${System.currentTimeMillis()}.mp3"
        val tempFile = File(context.cacheDir, fileName)

        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }

    private fun getMimeTypeFromUri(uri: Uri): String {
        val contentType = context.contentResolver.getType(uri)
        return contentType ?: when {
            uri.path?.endsWith(".mp3") == true -> "audio/mpeg"
            uri.path?.endsWith(".flac") == true -> "audio/flac"
            uri.path?.endsWith(".aac") == true -> "audio/aac"
            uri.path?.endsWith(".ogg") == true -> "audio/ogg"
            uri.path?.endsWith(".wav") == true -> "audio/wav"
            else -> "audio/mpeg" // default
        }
    }
}
