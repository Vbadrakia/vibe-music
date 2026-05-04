package com.vibe.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) onLoginSuccess()
    }

    Box(Modifier.fillMaxSize().background(VibeBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            // Logo
            Box(
                Modifier.size(72.dp).background(VibeGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("▶", fontSize = 30.sp, color = Color.Black) }
            Spacer(Modifier.height(12.dp))
            Text("vibe", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Text("Log in to continue.", color = VibeTextSecondary, fontSize = 14.sp)
            Spacer(Modifier.height(36.dp))

            // Email field
            VibeTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email address",
                leadingIcon = { Icon(Icons.Default.Email, null, tint = VibeTextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
            Spacer(Modifier.height(12.dp))

            // Password field
            VibeTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = VibeTextSecondary) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null, tint = VibeTextSecondary
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onForgotPassword, modifier = Modifier.align(Alignment.End)) {
                Text("Forgot password?", color = VibeGreen, fontSize = 13.sp)
            }
            Spacer(Modifier.height(20.dp))

            // Error
            if (uiState is AuthUiState.Error) {
                Text((uiState as AuthUiState.Error).msg, color = VibeError, fontSize = 13.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
            }

            // Login button
            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = uiState !is AuthUiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = VibeGreen),
                shape = CircleShape
            ) {
                if (uiState is AuthUiState.Loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                else Text("Log In", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(24.dp))

            // Divider
            DividerRow("Or continue with")
            Spacer(Modifier.height(16.dp))

            // SSO Buttons
            SocialButton("Continue with Google", "G", GoogleBlue, onClick = { viewModel.loginWithGoogle() })
            Spacer(Modifier.height(10.dp))
            SocialButton("Continue with Apple", "⌘", Color.White, textColor = Color.Black, onClick = { viewModel.loginWithApple() })

            Spacer(Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.Center) {
                Text("Don't have an account? ", color = VibeTextSecondary, fontSize = 14.sp)
                TextButton(onClick = onRegister, contentPadding = PaddingValues(0.dp)) {
                    Text("Sign Up", color = VibeGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun VibeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = VibeTextSecondary, fontSize = 14.sp) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = VibeGreen,
            unfocusedBorderColor = VibeBorder,
            cursorColor = VibeGreen,
            focusedContainerColor = VibeSurface,
            unfocusedContainerColor = VibeSurface
        ),
        singleLine = true
    )
}

@Composable
private fun DividerRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Divider(Modifier.weight(1f), color = VibeDivider)
        Text("  $text  ", color = VibeTextSecondary, fontSize = 12.sp)
        Divider(Modifier.weight(1f), color = VibeDivider)
    }
}

@Composable
fun SocialButton(label: String, iconText: String, iconBg: Color, textColor: Color = Color.White, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = CircleShape,
        border = ButtonDefaults.outlinedButtonBorder,
        colors = ButtonDefaults.outlinedButtonColors(containerColor = VibeSurface)
    ) {
        Box(Modifier.size(24.dp).background(iconBg, CircleShape), contentAlignment = Alignment.Center) {
            Text(iconText, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
