package com.vibe.app.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
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
import com.vibe.app.domain.model.Album
import com.vibe.app.domain.model.Artist
import com.vibe.app.domain.model.Song
import com.vibe.app.ui.navigation.Screen
import com.vibe.app.ui.theme.*

@Composable
fun SearchResultsScreen(
    query: String,
    navController: NavController,
    viewModel: SearchResultsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Top", "Songs", "Artists", "Albums")

    LaunchedEffect(query) { viewModel.search(query) }

    Column(Modifier.fillMaxSize().background(VibeBg)) {
        // Header with search bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .background(VibeElevated, CircleShape)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(query, color = Color.White, fontSize = 14.sp)
            }
            AsyncImage(model = "", contentDescription = null,
                modifier = Modifier.size(36.dp).padding(start = 8.dp).clip(CircleShape))
        }

        // Filter tabs
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tabs.size) { i ->
                FilterChip(
                    selected = selectedTab == i,
                    onClick = { selectedTab = i },
                    label = { Text(tabs[i]) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VibeGreen, selectedLabelColor = Color.Black,
                        containerColor = VibeElevated, labelColor = Color.White
                    ),
                    shape = CircleShape, border = null
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
            when (selectedTab) {
                0 -> { // Top
                    uiState.topResult?.let { song ->
                        item {
                            Text("Top result", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Spacer(Modifier.height(8.dp))
                            TopResultCard(song = song, onClick = {
                                viewModel.play(song)
                                navController.navigate(Screen.NowPlaying.route)
                            })
                            Spacer(Modifier.height(20.dp))
                        }
                    }
                    if (uiState.songs.isNotEmpty()) {
                        item { Text("Songs", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black) }
                        items(uiState.songs.take(3)) { song ->
                            SongRow(song = song, isActive = song == uiState.topResult, onClick = {
                                viewModel.play(song)
                                navController.navigate(Screen.NowPlaying.route)
                            })
                        }
                    }
                    if (uiState.artists.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            Text("Artists", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Spacer(Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(uiState.artists.take(4)) { artist -> ArtistCircleCard(artist, onClick = { navController.navigate(Screen.ArtistDetail.createRoute(artist.id)) }) }
                            }
                        }
                    }
                    if (uiState.albums.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            Text("Albums", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Spacer(Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(uiState.albums.take(4)) { album -> AlbumCard(album, onClick = { navController.navigate(Screen.AlbumDetail.createRoute(album.id)) }) }
                            }
                        }
                    }
                }
                1 -> items(uiState.songs) { song -> SongRow(song, onClick = { viewModel.play(song); navController.navigate(Screen.NowPlaying.route) }) }
                2 -> items(uiState.artists) { artist -> ArtistListRow(artist, onClick = { navController.navigate(Screen.ArtistDetail.createRoute(artist.id)) }) }
                3 -> items(uiState.albums) { album -> AlbumListRow(album, onClick = { navController.navigate(Screen.AlbumDetail.createRoute(album.id)) }) }
            }
        }
    }
}

@Composable
fun TopResultCard(song: Song, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(VibeSurface)
            .clickable(onClick = onClick).padding(16.dp)
    ) {
        Column {
            AsyncImage(model = song.coverUrl, contentDescription = null,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
            Spacer(Modifier.height(12.dp))
            Text(song.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.background(VibeElevated, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text("SONG", color = VibeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text("  •  ${song.artist}", color = VibeTextSecondary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun SongRow(song: Song, isActive: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(model = song.coverUrl, contentDescription = null,
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = if (isActive) VibeGreen else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, color = VibeTextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, null, tint = VibeTextSecondary) }
    }
}

@Composable
fun ArtistCircleCard(artist: Artist, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).width(80.dp)) {
        AsyncImage(model = artist.coverUrl, contentDescription = null,
            modifier = Modifier.size(72.dp).clip(CircleShape), contentScale = ContentScale.Crop)
        Spacer(Modifier.height(4.dp))
        Text(artist.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("Artist", color = VibeTextSecondary, fontSize = 11.sp)
    }
}

@Composable
fun AlbumCard(album: Album, onClick: () -> Unit) {
    Column(modifier = Modifier.width(130.dp).clickable(onClick = onClick)) {
        AsyncImage(model = album.coverUrl, contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
        Spacer(Modifier.height(4.dp))
        Text(album.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(album.artist, color = VibeTextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun ArtistListRow(artist: Artist, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(model = artist.coverUrl, contentDescription = null, modifier = Modifier.size(56.dp).clip(CircleShape), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(12.dp))
        Column { Text(artist.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium); Text("Artist", color = VibeTextSecondary, fontSize = 12.sp) }
    }
}

@Composable
fun AlbumListRow(album: Album, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(model = album.coverUrl, contentDescription = null, modifier = Modifier.size(56.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(12.dp))
        Column { Text(album.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium); Text("Album • ${album.artist}", color = VibeTextSecondary, fontSize = 12.sp) }
    }
}
