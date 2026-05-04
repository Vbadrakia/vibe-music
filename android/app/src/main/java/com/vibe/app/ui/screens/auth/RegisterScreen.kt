package com.vibe.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vibe.app.ui.theme.*

@Composable
fun RegisterScreen(
    onSuccess: () -> Unit,
    onLogin: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) { if (uiState is AuthUiState.Success) onSuccess() }

    Box(Modifier.fillMaxSize().background(VibeBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))
            Box(Modifier.size(64.dp).background(VibeGreen, CircleShape), contentAlignment = Alignment.Center) {
                Text("▶", fontSize = 26.sp, color = Color.Black)
            }
            Spacer(Modifier.height(10.dp))
            Text("Create your account", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(4.dp))
            Text("Start your free journey today.", color = VibeTextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(28.dp))

            VibeTextField(value = name, onValueChange = { name = it }, label = "Display name",
                leadingIcon = { Icon(Icons.Default.Person, null, tint = VibeTextSecondary) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
            Spacer(Modifier.height(10.dp))
            VibeTextField(value = email, onValueChange = { email = it }, label = "Email address",
                leadingIcon = { Icon(Icons.Default.Email, null, tint = VibeTextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next))
            Spacer(Modifier.height(10.dp))
            VibeTextField(value = password, onValueChange = { password = it }, label = "Password",
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = VibeTextSecondary) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = VibeTextSecondary)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next))
            Spacer(Modifier.height(10.dp))
            VibeTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Confirm password",
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = VibeTextSecondary) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done))
            Spacer(Modifier.height(6.dp))
            if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
                Text("Passwords do not match.", color = VibeError, fontSize = 12.sp)
            }
            Spacer(Modifier.height(16.dp))

            if (uiState is AuthUiState.Error) {
                Text((uiState as AuthUiState.Error).msg, color = VibeError, fontSize = 13.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = { if (password == confirmPassword) viewModel.register(name, email, password) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = uiState !is AuthUiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = VibeGreen),
                shape = CircleShape
            ) {
                if (uiState is AuthUiState.Loading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                else Text("Create Account", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(24.dp))
            Row {
                Text("Already have an account? ", color = VibeTextSecondary, fontSize = 14.sp)
                TextButton(onClick = onLogin, contentPadding = PaddingValues(0.dp)) {
                    Text("Log In", color = VibeGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
