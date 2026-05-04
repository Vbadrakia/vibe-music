package com.vibe.app.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FileMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vibe.app.presentation.components.MiniPlayer
import com.vibe.app.presentation.viewmodels.UploadViewModel

@Composable
fun UploadScreen(
    viewModel: UploadViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.selectFile(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Song") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF121212),
        contentColor = Color.White,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // File Picker Section
            FilePickerSection(
                selectedFile = selectedFile,
                onPickFile = { filePicker.launch("audio/*") },
                isUploading = uiState.isUploading
            )

            // File Info Section
            if (selectedFile != null) {
                FileInfoSection(selectedFile!!)

                // Upload Button
                Button(
                    onClick = { viewModel.uploadSong(selectedFile!!) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1DB954)
                    ),
                    enabled = !uiState.isUploading && selectedFile != null
                ) {
                    if (uiState.isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload Song", fontWeight = FontWeight.Bold)
                    }
                }

                // Progress Indicator
                if (uiState.uploadProgress > 0 && uiState.uploadProgress < 100) {
                    LinearProgressIndicator(
                        progress = { uiState.uploadProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = Color(0xFF1DB954),
                    )
                    Text(
                        "${uiState.uploadProgress}%",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1DB954)
                    )
                }
            }

            // Status Messages
            if (uiState.errorMessage != null) {
                ErrorMessage(uiState.errorMessage!!)
            }

            if (uiState.successMessage != null) {
                SuccessMessage(uiState.successMessage!!)
            }
        }

        // Mini Player
        MiniPlayer()
    }
}

@Composable
private fun FilePickerSection(
    selectedFile: Uri?,
    onPickFile: () -> Unit,
    isUploading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF282828),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = !isUploading) { onPickFile() }
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Default.CloudUpload,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color(0xFF1DB954)
        )
        Text(
            if (selectedFile == null) "Select an audio file" else "File selected: ${selectedFile.lastPathSegment}",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color.White
        )
        if (selectedFile == null) {
            Text(
                "Tap to pick MP3, FLAC, AAC, OGG, or WAV",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF888888),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FileInfoSection(fileUri: Uri) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF282828),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "File Details",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("File:", color = Color(0xFF888888), style = MaterialTheme.typography.labelSmall)
            Text(fileUri.lastPathSegment ?: "Unknown", color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
        Text(
            "Metadata will be extracted automatically when you upload.",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF888888),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF5C2C2C)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            message,
            modifier = Modifier.padding(16.dp),
            color = Color(0xFFFF6B6B),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun SuccessMessage(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C5C3C)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            message,
            modifier = Modifier.padding(16.dp),
            color = Color(0xFF6BFF9D),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
