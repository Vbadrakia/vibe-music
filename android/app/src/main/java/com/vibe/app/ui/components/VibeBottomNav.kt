package com.vibe.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue
import com.vibe.app.ui.navigation.Screen
import com.vibe.app.ui.theme.VibeBg
import com.vibe.app.ui.theme.VibeGreen

@Composable
fun VibeBottomNav(navController: NavController, currentRoute: String) {
    val navBackStack by navController.currentBackStackEntryAsState()
    val current = navBackStack?.destination?.route ?: currentRoute

    NavigationBar(containerColor = VibeBg, tonalElevation = 0.dp) {
        listOf(
            Triple(Screen.Home.route,    "Home",        Pair(Icons.Filled.Home,   Icons.Outlined.Home)),
            Triple(Screen.Search.route,  "Search",      Pair(Icons.Filled.Search, Icons.Outlined.Search)),
            Triple(Screen.Library.route, "Your Library", Pair(Icons.Filled.Home,  Icons.Outlined.Home))
        ).forEach { (route, label, icons) ->
            val selected = current == route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) navController.navigate(route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(if (selected) icons.first else icons.second, label) },
                label = { Text(label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VibeGreen,
                    selectedTextColor = VibeGreen,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
