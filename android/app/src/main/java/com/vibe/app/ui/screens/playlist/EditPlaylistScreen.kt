package com.vibe.app.ui.screens.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.vibe.app.ui.screens.auth.VibeTextField
import com.vibe.app.ui.theme.*

@Composable
fun EditPlaylistScreen(
    onBack: () -> Unit,
    viewModel: PlaylistDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val playlist = uiState.playlist ?: return

    var name by remember { mutableStateOf(playlist.name) }
    var description by remember { mutableStateOf(playlist.description ?: "") }

    Column(
        modifier = Modifier.fillMaxSize().background(VibeBg).verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        // Toolbar
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("Cancel", color = VibeTextSecondary, fontSize = 15.sp) }
            Text("Edit details", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = { viewModel.updatePlaylist(name, description); onBack() }) {
                Text("Save", color = VibeGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(24.dp))

        // Cover image
        Box(Modifier.size(140.dp).align(Alignment.CenterHorizontally).clip(RoundedCornerShape(8.dp)).background(VibeElevated)) {
            if (playlist.coverUrl != null) AsyncImage(model = playlist.coverUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Icon(Icons.Default.MusicNote, null, tint = VibeTextSecondary, modifier = Modifier.align(Alignment.Center).size(48.dp))
            // Edit overlay
            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.4f)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Text("Change photo", color = Color.White, fontSize = 11.sp)
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        // Name field
        VibeTextField(value = name, onValueChange = { name = it }, label = "Playlist name")
        Spacer(Modifier.height(12.dp))

        // Description field
        OutlinedTextField(
            value = description, onValueChange = { description = it },
            label = { Text("Add an optional description", color = VibeTextSecondary, fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedBorderColor = VibeGreen, unfocusedBorderColor = VibeBorder,
                cursorColor = VibeGreen, focusedContainerColor = VibeSurface, unfocusedContainerColor = VibeSurface
            ),
            maxLines = 4
        )
    }
}
