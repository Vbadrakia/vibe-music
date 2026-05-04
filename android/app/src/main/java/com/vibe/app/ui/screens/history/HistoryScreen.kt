package com.vibe.app.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.vibe.app.ui.components.MiniPlayer
import com.vibe.app.ui.navigation.Screen
import com.vibe.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val history by viewModel.history.collectAsState()

    Scaffold(containerColor = VibeBg, bottomBar = { MiniPlayer(navController = navController) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
                Text("Listening History", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Text("Everything you've played recently", color = VibeTextSecondary, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(12.dp))

            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
                items(history) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.playSong(item.song)
                            navController.navigate(Screen.NowPlaying.route)
                        }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(model = item.song.coverUrl, contentDescription = null,
                            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.song.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, null, tint = VibeTextDisabled, modifier = Modifier.size(11.dp))
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    formatTimestamp(item.played_at),
                                    color = VibeTextDisabled, fontSize = 11.sp
                                )
                                Text("  ·  ${item.song.artist}", color = VibeTextSecondary, fontSize = 11.sp,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, null, tint = VibeTextSecondary) }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(ms: Long): String {
    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return sdf.format(Date(ms))
}
