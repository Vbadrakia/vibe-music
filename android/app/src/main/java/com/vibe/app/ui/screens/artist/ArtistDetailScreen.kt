package com.vibe.app.ui.screens.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
fun ArtistDetailScreen(
    navController: NavController,
    viewModel: ArtistDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val artist = uiState.artist ?: return

    Scaffold(containerColor = VibeBg, bottomBar = { MiniPlayer(navController = navController) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Hero — full bleed photo
            item {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    AsyncImage(model = artist.coverUrl, contentDescription = null,
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.8f)))))
                    // Back & more
                    Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
                        IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, null, tint = Color.White) }
                    }
                    // Artist info at bottom
                    Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                        if (artist.isVerified) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, null, tint = Color(0xFF1D9BF0), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("VERIFIED ARTIST", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                        Text(artist.name, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                        Text("%,d monthly listeners".format(artist.monthlyListeners), color = Color.White.copy(0.7f), fontSize = 13.sp)
                    }
                }
            }

            // Action buttons
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(50.dp).background(VibeGreen, CircleShape).clickable { viewModel.playAll() }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayArrow, "Play", tint = Color.Black, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = { viewModel.toggleFollow() },
                        shape = CircleShape,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) { Text(if (uiState.isFollowing) "Following" else "Follow", fontSize = 14.sp) }
                }
            }

            // Popular Songs
            item { Text("Popular Songs", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 20.dp)) }
            itemsIndexed(uiState.popularSongs.take(5)) { index, song ->
                ArtistSongRow(index = index + 1, song = song, onClick = {
                    viewModel.playSong(song)
                    navController.navigate(Screen.NowPlaying.route)
                })
            }
            item { Spacer(Modifier.height(12.dp)) }

            // Discography
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Discography", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    TextButton(onClick = {}) { Text("Show all", color = VibeTextSecondary, fontSize = 13.sp) }
                }
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.albums) { album ->
                        Column(modifier = Modifier.width(140.dp).clickable { navController.navigate(Screen.AlbumDetail.createRoute(album.id)) }) {
                            AsyncImage(model = album.coverUrl, contentDescription = null,
                                modifier = Modifier.size(140.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                            Spacer(Modifier.height(6.dp))
                            Text(album.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${album.year}  •  Album", color = VibeTextSecondary, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // About
            item {
                Text("About", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                        .background(VibeSurface, RoundedCornerShape(12.dp)).padding(16.dp)
                ) {
                    Column {
                        Text("%,d monthly listeners".format(artist.monthlyListeners), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(artist.bio ?: "No biography available.", color = VibeTextSecondary, fontSize = 13.sp, lineHeight = 19.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = {}, shape = CircleShape, border = ButtonDefaults.outlinedButtonBorder,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                            Text("Read more", fontSize = 12.sp)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ArtistSongRow(index: Int, song: Song, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("$index", color = VibeTextSecondary, fontSize = 14.sp, modifier = Modifier.width(24.dp))
        AsyncImage(model = song.coverUrl, contentDescription = null, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.album, color = VibeTextSecondary, fontSize = 12.sp)
        }
        Text(song.durationFormatted, color = VibeTextSecondary, fontSize = 13.sp)
    }
}
