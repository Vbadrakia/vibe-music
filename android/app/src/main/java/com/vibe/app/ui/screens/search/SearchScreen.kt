package com.vibe.app.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.vibe.app.domain.model.Genre
import com.vibe.app.ui.components.MiniPlayer
import com.vibe.app.ui.components.VibeBottomNav
import com.vibe.app.ui.navigation.Screen
import com.vibe.app.ui.theme.*
import androidx.navigation.NavController

@Composable
fun SearchScreen(
    onQuerySubmit: (String) -> Unit,
    onGenreClick: (String) -> Unit,
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val genres by viewModel.genres.collectAsState()

    Scaffold(
        containerColor = VibeBg,
        bottomBar = {
            Column {
                MiniPlayer(navController = navController)
                VibeBottomNav(navController = navController, currentRoute = Screen.Search.route)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(20.dp))
            Text("Search", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(12.dp))

            // Search bar
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Artists, songs, or podcasts", color = VibeTextDisabled) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = VibeTextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = VibeGreen,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = VibeGreen,
                    focusedContainerColor = VibeElevated,
                    unfocusedContainerColor = VibeElevated
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (query.isNotBlank()) {
                        focusManager.clearFocus()
                        onQuerySubmit(query)
                    }
                })
            )
            Spacer(Modifier.height(24.dp))

            Text("Browse all", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(genres) { genre -> GenreCard(genre = genre, onClick = { onGenreClick(genre.id) }) }
            }
        }
    }
}

@Composable
fun GenreCard(genre: Genre, onClick: () -> Unit) {
    val bgColor = runCatching { Color(android.graphics.Color.parseColor(genre.color)) }.getOrElse { VibeGreen }
    val textColor = if (bgColor.luminance() > 0.4f) Color.Black else Color.White

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.BottomStart
    ) {
        AsyncImage(model = genre.coverUrl, contentDescription = null,
            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(bgColor.copy(alpha = 0.5f)))
        Text(
            text = genre.name,
            color = textColor,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            modifier = Modifier.padding(10.dp)
        )
    }
}
