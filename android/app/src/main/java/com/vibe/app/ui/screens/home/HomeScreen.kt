package com.vibe.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.vibe.app.domain.model.Show
import com.vibe.app.ui.components.MiniPlayer
import com.vibe.app.ui.components.VibeBottomNav
import com.vibe.app.ui.navigation.Screen
import com.vibe.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = remember { listOf("Music", "Podcasts & Shows", "Audiobooks") }

    Scaffold(
        containerColor = VibeBg,
        bottomBar = {
            Column {
                MiniPlayer(navController = navController)
                VibeBottomNav(navController = navController, currentRoute = Screen.Home.route)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = VibeGreen
                    )
                }
                uiState.error != null -> {
                    HomeErrorContent(
                        error = uiState.error ?: "Unknown error",
                        onRetry = { viewModel.load() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    HomeContent(
                        uiState = uiState,
                        greeting = greeting,
                        tabs = tabs,
                        selectedTabIndex = selectedTabIndex,
                        onTabSelected = { selectedTabIndex = it },
                        onSongClick = { song ->
                            viewModel.play(song)
                            navController.navigate(Screen.NowPlaying.route)
                        },
                        onPlaylistClick = { playlistId ->
                            navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                        },
                        onAlbumClick = { albumId ->
                            navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                        },
                        onShowClick = { showId ->
                            navController.navigate(Screen.ShowDetail.createRoute(showId))
                        },
                        onSettingsClick = {
                            navController.navigate(Screen.Settings.route)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = Color.Red,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Oops! Something went wrong",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            error,
            color = Color.White.copy(0.6f),
            fontSize = 12.sp
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = VibeGreen)
        ) {
            Text("Retry", color = Color.Black)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    greeting: String,
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onSongClick: (Song) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    onShowClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // ── Header ──────────────────────────────────────────────────────
        item {
            HomeHeader(
                greeting = greeting,
                onSettingsClick = onSettingsClick
            )
        }

        // ── Tab chips ───────────────────────────────────────────────────
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs.size) { i ->
                    FilterChip(
                        selected = selectedTabIndex == i,
                        onClick = { onTabSelected(i) },
                        label = { Text(tabs[i], fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VibeGreen,
                            selectedLabelColor = Color.Black,
                            containerColor = VibeElevated,
                            labelColor = Color.White
                        ),
                        shape = CircleShape,
                        border = null
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Recently Played 2×2 grid ────────────────────────────────────
        item {
            val recentSongs = uiState.recentlyPlayed.take(4)
            if (recentSongs.isNotEmpty()) {
                RecentlyPlayedGrid(
                    songs = recentSongs,
                    onSongClick = onSongClick
                )
                Spacer(Modifier.height(24.dp))
            }
        }

        // ── Recommended Songs ───────────────────────────────────────────
        if (uiState.recommendedSongs.isNotEmpty()) {
            item {
                Text(
                    "Recommended for you",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.recommendedSongs.size) { index ->
                        val song = uiState.recommendedSongs[index]
                        SongCard(song = song) {
                            onSongClick(song)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        // ── Made For You ────────────────────────────────────────────────
        if (uiState.madeForYou.isNotEmpty()) {
            item {
                Text(
                    "Made For You",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(12.dp))
            }

            item {
                uiState.madeForYou.firstOrNull()?.let { playlist ->
                    FeaturedCard(
                        title = playlist.name,
                        description = playlist.description ?: "",
                        coverUrl = playlist.coverUrl ?: "",
                        onClick = { onPlaylistClick(playlist.id) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        // Release Radar card
        if (uiState.newReleases.isNotEmpty()) {
            item {
                uiState.newReleases.firstOrNull()?.let { album ->
                    ReleaseRadarCard(
                        albumCoverUrl = album.coverUrl,
                        onClick = { onAlbumClick(album.id) }
                    )
                }
            }
        }

        // ── Trending Shows ──────────────────────────────────────────────
        if (uiState.trendingShows.isNotEmpty()) {
            item {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Trending Shows",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.trendingShows.size) { index ->
                        val show = uiState.trendingShows[index]
                        ShowCard(show = show) {
                            onShowClick(show.id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    greeting: String,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            greeting,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black
        )
        Row {
            IconButton(onClick = {}) { Icon(Icons.Default.Cast, null, tint = Color.White) }
            IconButton(onClick = {}) { Icon(Icons.Default.Notifications, null, tint = Color.White) }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, null, tint = Color.White)
            }
        }
    }
}

@Composable
private fun RecentlyPlayedGrid(
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            songs.take(2).forEach { song ->
                RecentCard(song = song, modifier = Modifier.weight(1f)) {
                    onSongClick(song)
                }
            }
            if (songs.size < 2) {
                repeat(2 - songs.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        if (songs.size > 2) {
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                songs.drop(2).forEach { song ->
                    RecentCard(song = song, modifier = Modifier.weight(1f)) {
                        onSongClick(song)
                    }
                }
                repeat(2 - (songs.size - 2)) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ReleaseRadarCard(
    albumCoverUrl: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = albumCoverUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.7f))))
        )
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                "Release Radar",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                "Catch up on the latest from artists you follow.",
                color = Color.White.copy(0.7f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun SongCard(song: Song, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = song.coverUrl,
            contentDescription = null,
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = song.title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            color = Color.White.copy(0.7f),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ShowCard(show: Show, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = show.coverUrl,
            contentDescription = null,
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = show.name,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = show.publisher,
            color = Color.White.copy(0.7f),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RecentCard(song: Song, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .background(VibeElevated, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.coverUrl,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(8.dp))
        Text(
            song.title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}

@Composable
private fun FeaturedCard(
    title: String,
    description: String,
    coverUrl: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = coverUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.85f))))
        )
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                "Discover Weekly",
                color = VibeGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                description,
                color = Color.White.copy(0.7f),
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
