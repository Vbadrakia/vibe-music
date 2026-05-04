package com.vibe.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
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
import com.vibe.app.ui.navigation.Screen
import com.vibe.app.ui.theme.*

@Composable
fun MiniPlayer(
    navController: NavController,
    viewModel: MiniPlayerViewModel = hiltViewModel()
) {
    val song by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isLiked by viewModel.isLiked.collectAsState()

    song?.let { s ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(VibeElevated)
                .clickable { navController.navigate(Screen.NowPlaying.route) }
        ) {
            // Progress indicator line at top
            LinearProgressIndicator(
                progress = viewModel.progress.collectAsState().value,
                modifier = Modifier.fillMaxWidth().height(2.dp).align(Alignment.TopStart),
                color = VibeGreen,
                trackColor = VibeDivider
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album art
                AsyncImage(
                    model = s.coverUrl,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(10.dp))

                // Song info
                Column(modifier = Modifier.weight(1f)) {
                    Text(s.title, color = Color.White, fontSize = 13.sp,
                        fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(s.artist, color = VibeTextSecondary, fontSize = 11.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                // Like button
                IconButton(onClick = { viewModel.toggleLike() }) {
                    Icon(
                        if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) VibeGreen else VibeTextSecondary
                    )
                }

                // Play/Pause button
                IconButton(onClick = { viewModel.togglePlayPause() }) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
