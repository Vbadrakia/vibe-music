package com.vibe.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import coil.compose.AsyncImage
import com.vibe.app.ui.theme.*

@Composable
fun ProfileScreen(
    onSettings: () -> Unit,
    onHistory: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val recentArtists by viewModel.recentArtists.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

    LazyColumn(Modifier.fillMaxSize().background(VibeBg)) {
        item {
            // Header
            Box(Modifier.fillMaxWidth().height(240.dp).background(
                androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFF1A1A3E), VibeBg))
            )) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Spacer(Modifier.width(48.dp))
                    Text("Profile", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterVertically))
                    IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, null, tint = Color.White) }
                }
                Column(Modifier.align(Alignment.BottomStart).padding(horizontal = 20.dp, vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    // Avatar
                    Box(Modifier.size(80.dp).clip(CircleShape).background(VibeElevated)) {
                        user?.avatarUrl?.let {
                            AsyncImage(model = it, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } ?: run { Icon(Icons.Default.Person, null, tint = VibeTextSecondary, modifier = Modifier.align(Alignment.Center).size(40.dp)) }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(user?.displayName ?: "User", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        StatItem("${user?.followersCount ?: 0}", "Followers")
                        StatItem("${user?.followingCount ?: 0}", "Following")
                    }
                }
            }
        }

        // Recent listening
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Recent listening", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                TextButton(onClick = onHistory) { Text("Show all", color = VibeTextSecondary, fontSize = 13.sp) }
            }
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(recentArtists) { artist ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
                        AsyncImage(model = artist.coverUrl, contentDescription = null,
                            modifier = Modifier.size(72.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        Spacer(Modifier.height(4.dp))
                        Text(artist.name, color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Artist", color = VibeTextSecondary, fontSize = 10.sp)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Playlists
        item { Text("Playlists", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 16.dp)) }
        items(playlists) { playlist ->
            Row(modifier = Modifier.fillMaxWidth().clickable {}.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(52.dp).clip(RoundedCornerShape(6.dp)).background(VibeElevated)) {
                    if (playlist.coverUrl != null) AsyncImage(model = playlist.coverUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
                Spacer(Modifier.width(12.dp))
                Column { Text(playlist.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium); Text("${playlist.songCount} songs", color = VibeTextSecondary, fontSize = 12.sp) }
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
        Text(label, color = VibeTextSecondary, fontSize = 12.sp)
    }
}
