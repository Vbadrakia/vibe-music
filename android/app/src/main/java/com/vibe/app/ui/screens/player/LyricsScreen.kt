package com.vibe.app.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.vibe.app.ui.theme.*

@Composable
fun LyricsScreen(
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val song by viewModel.currentSong.collectAsState()
    val lyrics by viewModel.lyrics.collectAsState()
    val currentLineIndex by viewModel.currentLyricIndex.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val dominantColor by viewModel.dominantColor.collectAsState()

    val bgColor = dominantColor ?: Color(0xFF4A0080)
    val listState = rememberLazyListState()

    // Auto-scroll to active lyric
    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex >= 0 && lyrics.isNotEmpty()) {
            listState.animateScrollToItem(currentLineIndex.coerceAtMost(lyrics.lastIndex), scrollOffset = -200)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(bgColor.copy(alpha = 0.9f), Color.Black), startY = 0f, endY = 1500f)
        )
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 16.dp).align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.KeyboardArrowDown, "Back", tint = Color.White, modifier = Modifier.size(32.dp)) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PLAYING FROM PLAYLIST", color = Color.White.copy(0.6f), fontSize = 10.sp, letterSpacing = 1.sp)
                Text(song?.album ?: "", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = {}) { Icon(Icons.Default.MoreHoriz, null, tint = Color.White) }
        }

        // Lyrics list
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(top = 80.dp, start = 24.dp, end = 24.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(lyrics) { index, line ->
                val isActive = index == currentLineIndex
                Text(
                    text = line.text,
                    color = if (isActive) Color.White else Color.White.copy(alpha = 0.4f),
                    fontSize = if (isActive) 30.sp else 26.sp,
                    fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
                    lineHeight = if (isActive) 36.sp else 32.sp
                )
            }
        }

        // Mini player at bottom
        song?.let { s ->
            Row(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(model = s.coverUrl, contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = Color.White,
                        trackColor = Color.White.copy(0.3f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(viewModel.positionFormatted, color = Color.White.copy(0.6f), fontSize = 10.sp)
                        Text(s.durationFormatted, color = Color.White.copy(0.6f), fontSize = 10.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                IconButton(onClick = { viewModel.togglePlayPause() }) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        null, tint = Color.White, modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
