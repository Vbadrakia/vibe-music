package com.vibe.app.ui.screens.playlist

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.vibe.app.domain.model.Song
import com.vibe.app.ui.components.MiniPlayer
import com.vibe.app.ui.navigation.Screen
import com.vibe.app.ui.theme.*

@Composable
fun PlaylistDetailScreen(
    navController: NavController,
    viewModel: PlaylistDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val playlist = uiState.playlist ?: return

    Scaffold(containerColor = VibeBg, bottomBar = { MiniPlayer(navController = navController) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Hero
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(320.dp)
                        .background(Brush.verticalGradient(listOf(Color(0xFF1A1A2E), VibeBg)))
                ) {
                    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
                            IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, null, tint = Color.White) }
                        }
                        // Cover
                        Box(Modifier.size(160.dp).clip(RoundedCornerShape(8.dp)).background(VibeElevated)) {
                            if (playlist.coverUrl != null) {
                                AsyncImage(model = playlist.coverUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } else {
                                Icon(Icons.Default.MusicNote, null, tint = VibeTextSecondary, modifier = Modifier.align(Alignment.Center).size(60.dp))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(playlist.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(playlist.ownerName, color = VibeTextSecondary, fontSize = 13.sp)
                            Text("  ·  ${playlist.songCount} songs", color = VibeTextSecondary, fontSize = 13.sp)
                        }
                        playlist.description?.let { Text(it, color = VibeTextSecondary, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                    }
                }
            }

            // Action row
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {}) { Icon(Icons.Default.FavoriteBorder, "Like", tint = VibeTextSecondary, modifier = Modifier.size(26.dp)) }
                    IconButton(onClick = {}) { Icon(Icons.Default.Download, "Download", tint = VibeTextSecondary, modifier = Modifier.size(26.dp)) }
                    IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, null, tint = VibeTextSecondary, modifier = Modifier.size(26.dp)) }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = {}) { Icon(Icons.Default.Shuffle, "Shuffle", tint = VibeTextSecondary) }
                    Box(Modifier.size(52.dp).background(VibeGreen, CircleShape).clickable { viewModel.playAll() }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayArrow, "Play", tint = Color.Black, modifier = Modifier.size(30.dp))
                    }
                }
            }

            items(playlist.songs) { song ->
                PlaylistSongRow(song = song, onClick = { viewModel.playSong(song); navController.navigate(Screen.NowPlaying.route) })
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun PlaylistSongRow(song: Song, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(model = song.coverUrl, contentDescription = null,
            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, color = VibeTextSecondary, fontSize = 12.sp)
        }
        IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, null, tint = VibeTextSecondary) }
    }
}
