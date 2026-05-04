package com.vibe.app.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.*
import com.vibe.app.ui.theme.*
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val emoji: String,
    val title: String,
    val subtitle: String
)

private val pages = listOf(
    OnboardingPage("🎵", "Discover Your Sound", "Explore millions of songs, playlists and artists tailored just for you."),
    OnboardingPage("🎧", "Listen Anywhere, Anytime", "Download your favorite tracks and enjoy music even without an internet connection."),
    OnboardingPage("✨", "Your Vibe, Your Music", "Create playlists, follow artists, and let your music define who you are.")
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit
) {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VibeBg)
    ) {
        // Skip
        if (!isLastPage) {
            TextButton(
                onClick = { scope.launch { pagerState.animateScrollToPage(pages.lastIndex) } },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Text("Skip", color = VibeTextSecondary, fontSize = 14.sp)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HorizontalPager(count = pages.size, state = pagerState) { page ->
                OnboardingPageContent(page = pages[page])
            }
            Spacer(Modifier.height(40.dp))

            // Dot indicators
            HorizontalPagerIndicator(
                pagerState = pagerState,
                activeColor = VibeGreen,
                inactiveColor = VibeElevated,
                indicatorWidth = 20.dp,
                indicatorHeight = 6.dp,
                spacing = 8.dp
            )
            Spacer(Modifier.height(40.dp))

            // CTA button
            Button(
                onClick = {
                    if (isLastPage) onGetStarted()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VibeGreen),
                shape = CircleShape
            ) {
                Text(
                    text = if (isLastPage) "Get Started" else "Next →",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(16.dp))

            // Login link
            Row(horizontalArrangement = Arrangement.Center) {
                Text("Already have an account? ", color = VibeTextSecondary, fontSize = 14.sp)
                TextButton(onClick = onLogin, contentPadding = PaddingValues(0.dp)) {
                    Text("Log In", color = VibeGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
    ) {
        // Illustration placeholder using emoji in green circle
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(VibeGreen.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(page.emoji, fontSize = 80.sp)
        }
        Spacer(Modifier.height(40.dp))
        Text(
            text = page.title,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = page.subtitle,
            color = VibeTextSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}
