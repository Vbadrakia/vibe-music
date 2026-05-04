package com.vibe.app.ui.screens.library

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.vibe.app.domain.model.Playlist
import com.vibe.app.ui.components.MiniPlayer
import com.vibe.app.ui.components.VibeBottomNav
import com.vibe.app.ui.navigation.Screen
import com.vibe.app.ui.theme.*

@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val playlists by viewModel.playlists.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val uploadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.uploadSong(uri)
    }

    LaunchedEffect(uploadState.message, uploadState.error) {
        uploadState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUploadStatus()
        }
        uploadState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUploadStatus()
        }
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = VibeSurface,
            title = { Text("Create Playlist", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName, onValueChange = { newPlaylistName = it },
                    label = { Text("Playlist name", color = VibeTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = VibeGreen, unfocusedBorderColor = VibeBorder)
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.createPlaylist(newPlaylistName); showCreateDialog = false; newPlaylistName = "" }) {
                    Text("Create", color = VibeGreen)
                }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancel", color = VibeTextSecondary) } }
        )
    }

    Scaffold(
        containerColor = VibeBg,
        bottomBar = { Column { MiniPlayer(navController = navController); VibeBottomNav(navController, Screen.Library.route) } }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(horizontal = 16.dp)) {
            item {
                Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Your Library", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Row {
                        IconButton(onClick = { uploadLauncher.launch(arrayOf("audio/*")) }) { Icon(Icons.Default.FileUpload, null, tint = Color.White) }
                        IconButton(onClick = { showCreateDialog = true }) { Icon(Icons.Default.Add, null, tint = Color.White) }
                    }
                }
                if (uploadState.isUploading) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = VibeGreen, trackColor = VibeDivider)
                }
            }

            // Filter chips
            item {
                var selected by remember { mutableStateOf(0) }
                val filters = listOf("Playlists", "Artists", "Albums", "Podcasts")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    filters.forEachIndexed { i, label ->
                        FilterChip(
                            selected = selected == i, onClick = { selected = i },
                            label = { Text(label, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = VibeGreen, selectedLabelColor = Color.Black, containerColor = VibeElevated, labelColor = Color.White),
                            shape = CircleShape, border = null
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Liked Songs pinned item
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Screen.LikedSongs.route) }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(52.dp).clip(RoundedCornerShape(6.dp)).background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFF4A0080), Color(0xFF1DB954)))),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Favorite, null, tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Liked Songs", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Playlist", color = VibeTextSecondary, fontSize = 12.sp)
                    }
                }
            }

            // User playlists
            items(playlists) { playlist -> LibraryPlaylistRow(playlist, onClick = { navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id)) }) }
        }
    }
}

@Composable
private fun LibraryPlaylistRow(playlist: Playlist, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(52.dp).clip(RoundedCornerShape(6.dp)).background(VibeElevated)) {
            if (playlist.coverUrl != null) AsyncImage(model = playlist.coverUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Icon(Icons.Default.MusicNote, null, tint = VibeTextSecondary, modifier = Modifier.align(Alignment.Center))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(playlist.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Playlist · ${playlist.songCount} songs", color = VibeTextSecondary, fontSize = 12.sp)
        }
    }
}
