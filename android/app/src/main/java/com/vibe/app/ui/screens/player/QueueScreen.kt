package com.vibe.app.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MusicNote
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
fun QueueScreen(
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val queue by viewModel.queue.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()

    Column(Modifier.fillMaxSize().background(VibeBg)) {
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
            Text("Queue", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f).padding(start = 4.dp))
        }

        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
            // Now Playing
            item {
                Text("Now Playing", color = VibeTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                Spacer(Modifier.height(8.dp))
            }
            currentSong?.let { song ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(VibeSurface).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(model = song.coverUrl, contentDescription = null,
                            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(song.title, color = VibeGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(song.artist, color = VibeTextSecondary, fontSize = 13.sp)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            // Next In Queue header
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Next In Queue", color = VibeTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    OutlinedButton(
                        onClick = { viewModel.clearQueue() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VibeGreen),
                        border = ButtonDefaults.outlinedButtonBorder,
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) { Text("CLEAR QUEUE", color = VibeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Queue items
            items(queue) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.playFromQueue(item) }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(model = item.song.coverUrl, contentDescription = null,
                        modifier = Modifier.size(52.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.song.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(item.song.artist, color = VibeTextSecondary, fontSize = 12.sp)
                    }
                    Icon(Icons.Default.DragHandle, "Reorder", tint = VibeTextSecondary)
                }
            }

            // Autoplay row
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(52.dp).clip(RoundedCornerShape(6.dp)).background(VibeElevated), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.MusicNote, null, tint = VibeTextDisabled, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Loading...", color = VibeTextSecondary, fontSize = 14.sp)
                        Text("Autoplay based on previous tracks", color = VibeTextDisabled, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
