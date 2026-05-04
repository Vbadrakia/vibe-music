package com.vibe.app.ui.screens.liked

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
fun LikedSongsScreen(
    navController: NavController,
    viewModel: LikedSongsViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsState()

    Scaffold(containerColor = VibeBg, bottomBar = { MiniPlayer(navController = navController) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Gradient hero
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(280.dp)
                        .background(Brush.verticalGradient(listOf(Color(0xFF4A0080), Color(0xFF1E0040), VibeBg)))
                ) {
                    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Bottom) {
                        Icon(Icons.Default.Favorite, null, tint = Color.White, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Liked Songs", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                        Text("${songs.size} songs", color = Color.White.copy(0.7f), fontSize = 13.sp)
                    }
                }
            }

            // Action row
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = {}) { Icon(Icons.Default.Shuffle, "Shuffle", tint = Color.White, modifier = Modifier.size(28.dp)) }
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.size(52.dp).background(VibeGreen, CircleShape).clickable { viewModel.playAll() }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayArrow, "Play", tint = Color.Black, modifier = Modifier.size(30.dp))
                    }
                }
            }

            items(songs) { song ->
                LikedSongRow(song = song, onClick = {
                    viewModel.playSong(song)
                    navController.navigate(Screen.NowPlaying.route)
                }, onUnlike = { viewModel.unlike(song) })
            }
        }
    }
}

@Composable
private fun LikedSongRow(song: Song, onClick: () -> Unit, onUnlike: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(model = song.coverUrl, contentDescription = null,
            modifier = Modifier.size(52.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, color = VibeTextSecondary, fontSize = 12.sp)
        }
        IconButton(onClick = onUnlike) { Icon(Icons.Default.Favorite, "Unlike", tint = VibeGreen) }
        IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, null, tint = VibeTextSecondary) }
    }
}
