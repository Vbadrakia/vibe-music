package com.vibe.app.ui.screens.show

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.vibe.app.domain.model.Episode
import com.vibe.app.ui.components.MiniPlayer
import com.vibe.app.ui.theme.*

@Composable
fun ShowDetailScreen(
    navController: NavController,
    viewModel: ShowDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val show = uiState.show ?: return

    Scaffold(
        containerColor = VibeBg,
        bottomBar = { MiniPlayer(navController = navController) }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Hero section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Brush.verticalGradient(listOf(VibeElevated, VibeBg)))
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
                        }
                        Spacer(Modifier.height(8.dp))
                        AsyncImage(
                            model = show.coverUrl, contentDescription = null,
                            modifier = Modifier.size(160.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(show.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text(show.publisher, color = VibeGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Description
            item {
                Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    Text(
                        text = show.description,
                        color = VibeTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { /* Follow */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VibeTextSecondary),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Following", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            // Episodes title
            item {
                Text(
                    "All Episodes",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }

            // Episode list
            items(uiState.episodes) { episode ->
                EpisodeRow(episode = episode) {
                    viewModel.playEpisode(episode)
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = VibeElevated, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun EpisodeRow(episode: Episode, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = episode.coverUrl,
            contentDescription = null,
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(episode.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(
                "${episode.releaseDate} • ${episode.durationSecs / 60} min",
                color = VibeTextSecondary,
                fontSize = 12.sp
            )
        }
        Icon(Icons.Default.PlayCircle, null, tint = Color.White, modifier = Modifier.size(28.dp))
    }
}
