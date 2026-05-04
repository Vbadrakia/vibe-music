package com.vibe.app.ui.screens.album

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
fun AlbumDetailScreen(
    navController: NavController,
    viewModel: AlbumDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dominantColor by viewModel.dominantColor.collectAsState()
    val album = uiState.album ?: return

    val heroBg = dominantColor ?: Color(0xFF1A4A1A)

    Scaffold(
        containerColor = VibeBg,
        bottomBar = { MiniPlayer(navController = navController) }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Hero section
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(340.dp)
                        .background(Brush.verticalGradient(listOf(heroBg, VibeBg)))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top bar
                        Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                            }
                            IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, null, tint = Color.White) }
                        }
                        Spacer(Modifier.height(8.dp))
                        AsyncImage(
                            model = album.coverUrl, contentDescription = null,
                            modifier = Modifier.size(180.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("ALBUM", color = VibeTextSecondary, fontSize = 11.sp, letterSpacing = 1.sp)
                        Text(album.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(model = "", contentDescription = null, modifier = Modifier.size(20.dp).clip(CircleShape))
                            Spacer(Modifier.width(6.dp))
                            Text(album.artist, color = VibeTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("  ·  ${album.year}  ·  ${album.songCount} songs, ${album.totalDurationSecs / 60} min",
                                color = VibeTextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Action row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(52.dp).background(VibeGreen, CircleShape).clickable { viewModel.playAll() }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayArrow, "Play", tint = Color.Black, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    IconButton(onClick = {}) { Icon(Icons.Default.Add, "Add", tint = VibeTextSecondary, modifier = Modifier.size(28.dp)) }
                    IconButton(onClick = {}) { Icon(Icons.Default.Download, "Download", tint = VibeTextSecondary, modifier = Modifier.size(28.dp)) }
                }
            }

            // Track list
            itemsIndexed(uiState.songs) { index, song ->
                AlbumTrackRow(
                    index = index + 1,
                    song = song,
                    isActive = song.id == viewModel.currentSongId,
                    onClick = {
                        viewModel.playSong(song)
                        navController.navigate(Screen.NowPlaying.route)
                    }
                )
            }

            // Footer: release info
            item {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text("${album.year}", color = VibeTextDisabled, fontSize = 12.sp)
                    Text("© ${album.year} ${album.artist}", color = VibeTextDisabled, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun AlbumTrackRow(index: Int, song: Song, isActive: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$index", color = if (isActive) VibeGreen else VibeTextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.width(24.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = if (isActive) VibeGreen else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, color = VibeTextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(song.durationFormatted, color = VibeTextSecondary, fontSize = 13.sp)
    }
}
