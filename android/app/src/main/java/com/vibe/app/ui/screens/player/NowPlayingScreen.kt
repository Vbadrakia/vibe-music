package com.vibe.app.ui.screens.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.vibe.app.ui.theme.*

@Composable
fun NowPlayingScreen(
    onBack: () -> Unit,
    onQueue: () -> Unit,
    onLyrics: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val song by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val shuffleOn by viewModel.shuffleOn.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val isLiked by viewModel.isLiked.collectAsState()
    val dominantColor by viewModel.dominantColor.collectAsState()

    val bgColor = dominantColor ?: Color(0xFF1A1A2E)

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(bgColor, VibeBg), startY = 0f, endY = 900f)
        )
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(20.dp))

            // Top bar
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.KeyboardArrowDown, "Back", tint = Color.White, modifier = Modifier.size(32.dp)) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PLAYING FROM PLAYLIST", color = VibeTextSecondary, fontSize = 10.sp, letterSpacing = 1.sp)
                    Text(song?.album ?: "", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = {}) { Icon(Icons.Default.MoreHoriz, null, tint = Color.White) }
            }
            Spacer(Modifier.height(32.dp))

            // Album art — rotating when playing
            val rotation by rememberInfiniteTransition(label = "spin").animateFloat(
                initialValue = 0f, targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing)),
                label = "rotate"
            )
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .rotate(if (isPlaying) rotation else 0f)
                        .clip(CircleShape)
                        .background(VibeElevated)
                ) {
                    AsyncImage(
                        model = song?.coverUrl, contentDescription = null,
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(Modifier.height(32.dp))

            // Song info + like
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(song?.title ?: "", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song?.artist ?: "", color = VibeTextSecondary, fontSize = 14.sp)
                }
                IconButton(onClick = { viewModel.toggleLike() }) {
                    Icon(
                        if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        "Like", tint = if (isLiked) VibeGreen else VibeTextSecondary, modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            // Seek bar
            Slider(
                value = progress,
                onValueChange = { viewModel.seekTo(it) },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = VibeDivider
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(viewModel.positionFormatted, color = VibeTextSecondary, fontSize = 11.sp)
                Text(song?.durationFormatted ?: "--:--", color = VibeTextSecondary, fontSize = 11.sp)
            }
            Spacer(Modifier.height(16.dp))

            // Controls
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.toggleShuffle() }) {
                    Icon(Icons.Default.Shuffle, "Shuffle", tint = if (shuffleOn) VibeGreen else VibeTextSecondary, modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = { viewModel.skipPrev() }) {
                    Icon(Icons.Default.SkipPrevious, "Prev", tint = Color.White, modifier = Modifier.size(36.dp))
                }
                // Play/Pause
                Box(
                    modifier = Modifier.size(64.dp).background(VibeGreen, CircleShape).clickable { viewModel.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        if (isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
                IconButton(onClick = { viewModel.skipNext() }) {
                    Icon(Icons.Default.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = { viewModel.cycleRepeat() }) {
                    Icon(
                        when (repeatMode) { 2 -> Icons.Default.RepeatOne; else -> Icons.Default.Repeat },
                        "Repeat", tint = if (repeatMode > 0) VibeGreen else VibeTextSecondary, modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            // Extra controls row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = {}) { Icon(Icons.Default.DevicesOther, "Devices", tint = VibeTextSecondary) }
                IconButton(onClick = onQueue) { Icon(Icons.Default.QueueMusic, "Queue", tint = VibeTextSecondary) }
                IconButton(onClick = onLyrics) { Icon(Icons.Default.Lyrics, "Lyrics", tint = VibeTextSecondary) }
                IconButton(onClick = {}) { Icon(Icons.Default.Share, "Share", tint = VibeTextSecondary) }
            }
        }
    }
}
