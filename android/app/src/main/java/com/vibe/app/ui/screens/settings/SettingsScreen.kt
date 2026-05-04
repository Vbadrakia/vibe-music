package com.vibe.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibe.app.ui.theme.*

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(VibeBg).verticalScroll(rememberScrollState())) {
        // Toolbar
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
            Text("Settings and Privacy", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        SettingsSection("Streaming Options") {
            SettingsItem(Icons.Default.Wifi, "Streaming quality", subtitle = "High quality")
            SettingsItem(Icons.Default.Download, "Download storage", subtitle = "Internal storage")
            SwitchSettingsItem(Icons.Default.GraphicEq, "Equalizer", subtitle = "Off") { }
        }
        SettingsSection("Privacy") {
            SwitchSettingsItem(Icons.Default.Visibility, "Listening activity", subtitle = "Friends can see what I play") { }
            SwitchSettingsItem(Icons.Default.People, "Private session", subtitle = "Start a private listening session") { }
        }
        SettingsSection("Notifications") {
            SwitchSettingsItem(Icons.Default.Notifications, "Push notifications") { }
            SwitchSettingsItem(Icons.Default.Email, "Email notifications") { }
        }
        SettingsSection("Account") {
            SettingsItem(Icons.Default.Person, "Profile", subtitle = "Edit your info")
            SettingsItem(Icons.Default.Security, "Change password")
            SettingsItem(Icons.Default.Language, "Language", subtitle = "English (India)")
        }
        SettingsSection("Support") {
            SettingsItem(Icons.Default.Help, "Help")
            SettingsItem(Icons.Default.Info, "About Vibe", subtitle = "Version 1.0.0")
        }

        Spacer(Modifier.height(12.dp))
        // Logout
        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = CircleShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VibeError),
                border = ButtonDefaults.outlinedButtonBorder
            ) { Text("Log Out", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VibeError) }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, color = VibeTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp))
    Column(content = content)
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 15.sp)
            subtitle?.let { Text(it, color = VibeTextSecondary, fontSize = 12.sp) }
        }
        Icon(Icons.Default.ChevronRight, null, tint = VibeTextSecondary)
    }
}

@Composable
private fun SwitchSettingsItem(icon: ImageVector, title: String, subtitle: String? = null, onToggle: (Boolean) -> Unit) {
    var checked by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 15.sp)
            subtitle?.let { Text(it, color = VibeTextSecondary, fontSize = 12.sp) }
        }
        Switch(checked = checked, onCheckedChange = { checked = it; onToggle(it) },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VibeGreen, uncheckedThumbColor = VibeTextSecondary, uncheckedTrackColor = VibeBorder))
    }
}
