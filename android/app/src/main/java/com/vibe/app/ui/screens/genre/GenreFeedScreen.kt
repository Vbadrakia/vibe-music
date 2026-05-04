package com.vibe.app.ui.screens.genre

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.vibe.app.ui.components.MiniPlayer
import com.vibe.app.ui.navigation.Screen
import com.vibe.app.ui.theme.*

@Composable
fun GenreFeedScreen(
    navController: NavController,
    viewModel: GenreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val genre = uiState.genre ?: return

    val bgColor = runCatching { Color(android.graphics.Color.parseColor(genre.color)) }.getOrElse { VibeGreen }

    Scaffold(containerColor = VibeBg, bottomBar = { MiniPlayer(navController = navController) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Hero
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(260.dp)
                        .background(Brush.verticalGradient(listOf(bgColor.copy(0.8f), Color(0xFF0D0D18))))
                ) {
                    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.Bottom) {
                        Spacer(Modifier.height(40.dp))
                        Text(genre.name, color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(6.dp))
                        Text(genre.description, color = Color.White.copy(0.7f), fontSize = 14.sp, lineHeight = 20.sp, maxLines = 2)
                        Spacer(Modifier.height(16.dp))
                        Row {
                            Box(Modifier.size(48.dp).background(VibeGreen, CircleShape).clickable { viewModel.playAll() }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PlayArrow, "Play", tint = Color.Black, modifier = Modifier.size(28.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Box(Modifier.size(48.dp).background(VibeSurface, CircleShape).clickable {}, contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.MoreHoriz, null, tint = Color.White)
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }

            // Popular Playlists header
            item {
                Text("Popular Playlists", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
            }

            // 2-column playlist grid
            item {
                val playlists = uiState.playlists
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 1000.dp).padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(playlists) { playlist ->
                        Column(modifier = Modifier.clickable { navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id)) }) {
                            AsyncImage(model = playlist.coverUrl, contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
                            Spacer(Modifier.height(4.dp))
                            Text(playlist.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(playlist.description ?: "", color = VibeTextSecondary, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}
