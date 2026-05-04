package com.vibe.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vibe.app.ui.theme.*

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    Box(Modifier.fillMaxSize().background(VibeBg)) {
        // Back button
        IconButton(onClick = onBack, modifier = Modifier.padding(8.dp)) {
            Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!sent) {
                Box(Modifier.size(72.dp).background(VibeGreen.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Email, null, tint = VibeGreen, modifier = Modifier.size(36.dp))
                }
                Spacer(Modifier.height(20.dp))
                Text("Forgot Password?", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Enter the email associated with your account and we'll send you a reset link.",
                    color = VibeTextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 20.sp
                )
                Spacer(Modifier.height(32.dp))

                VibeTextField(
                    value = email, onValueChange = { email = it },
                    label = "Email address",
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = VibeTextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done)
                )

                if (uiState is AuthUiState.Error) {
                    Spacer(Modifier.height(8.dp))
                    Text((uiState as AuthUiState.Error).msg, color = VibeError, fontSize = 13.sp, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.forgotPassword(email) { sent = true } },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = uiState !is AuthUiState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = VibeGreen),
                    shape = CircleShape
                ) {
                    if (uiState is AuthUiState.Loading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                    else Text("Send Reset Link", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            } else {
                // Success state
                Box(Modifier.size(80.dp).background(VibeGreen.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                    Text("✓", fontSize = 40.sp, color = VibeGreen)
                }
                Spacer(Modifier.height(20.dp))
                Text("Check your email", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text("We've sent a password reset link to $email", color = VibeTextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
                Spacer(Modifier.height(32.dp))
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VibeGreen), shape = CircleShape) {
                    Text("Back to Login", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
