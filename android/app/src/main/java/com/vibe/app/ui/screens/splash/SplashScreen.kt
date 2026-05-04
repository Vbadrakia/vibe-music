package com.vibe.app.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vibe.app.ui.theme.VibeBg
import com.vibe.app.ui.theme.VibeGreen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onAuthComplete: () -> Unit,
    onNeedsAuth: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()

    // Pulse animation on logo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(authState) {
        delay(1800)
        when (authState) {
            AuthState.Authenticated -> onAuthComplete()
            AuthState.Unauthenticated -> onNeedsAuth()
            AuthState.Loading -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VibeBg),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Logo — green circle with ▶
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(96.dp)
                    .background(VibeGreen, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("▶", fontSize = 40.sp, color = Color.Black)
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "vibe",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Continue your sonic journey.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
