package com.vibe.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vibe.app.ui.screens.album.AlbumDetailScreen
import com.vibe.app.ui.screens.artist.ArtistDetailScreen
import com.vibe.app.ui.screens.auth.ForgotPasswordScreen
import com.vibe.app.ui.screens.auth.LoginScreen
import com.vibe.app.ui.screens.auth.OnboardingScreen
import com.vibe.app.ui.screens.auth.RegisterScreen
import com.vibe.app.ui.screens.genre.GenreFeedScreen
import com.vibe.app.ui.screens.history.HistoryScreen
import com.vibe.app.ui.screens.home.HomeScreen
import com.vibe.app.ui.screens.library.LibraryScreen
import com.vibe.app.ui.screens.liked.LikedSongsScreen
import com.vibe.app.ui.screens.player.LyricsScreen
import com.vibe.app.ui.screens.player.NowPlayingScreen
import com.vibe.app.ui.screens.player.QueueScreen
import com.vibe.app.ui.screens.playlist.EditPlaylistScreen
import com.vibe.app.ui.screens.playlist.PlaylistDetailScreen
import com.vibe.app.ui.screens.show.ShowDetailScreen
import com.vibe.app.ui.screens.profile.ProfileScreen
import com.vibe.app.ui.screens.search.SearchResultsScreen
import com.vibe.app.ui.screens.search.SearchScreen
import com.vibe.app.ui.screens.settings.SettingsScreen
import com.vibe.app.ui.screens.splash.SplashScreen

@Composable
fun VibeNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // ── Auth ────────────────────────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(
                onAuthComplete = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Splash.route) { inclusive = true } } },
                onNeedsAuth = { navController.navigate(Screen.Onboarding.route) { popUpTo(Screen.Splash.route) { inclusive = true } } }
            )
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onGetStarted = { navController.navigate(Screen.Register.route) },
                onLogin = { navController.navigate(Screen.Login.route) }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } } },
                onRegister = { navController.navigate(Screen.Register.route) },
                onForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onSuccess = { navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } } },
                onLogin = { navController.navigate(Screen.Login.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(onBack = { navController.popBackStack() })
        }

        // ── Main ────────────────────────────────────────────────────────────
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Search.route) {
            SearchScreen(
                onQuerySubmit = { q -> navController.navigate(Screen.SearchResults.createRoute(q)) },
                onGenreClick = { id -> navController.navigate(Screen.GenreFeed.createRoute(id)) },
                navController = navController
            )
        }
        composable(
            route = Screen.SearchResults.route,
            arguments = listOf(navArgument("query") { type = NavType.StringType })
        ) { backStack ->
            val query = backStack.arguments?.getString("query") ?: ""
            SearchResultsScreen(
                query = query,
                navController = navController
            )
        }
        composable(Screen.Library.route) {
            LibraryScreen(navController = navController)
        }

        // ── Player ──────────────────────────────────────────────────────────
        composable(Screen.NowPlaying.route) {
            NowPlayingScreen(
                onBack = { navController.popBackStack() },
                onQueue = { navController.navigate(Screen.Queue.route) },
                onLyrics = { navController.navigate(Screen.Lyrics.route) }
            )
        }
        composable(Screen.Queue.route) {
            QueueScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Lyrics.route) {
            LyricsScreen(onBack = { navController.popBackStack() })
        }

        // ── Content ─────────────────────────────────────────────────────────
        composable(
            route = Screen.AlbumDetail.route,
            arguments = listOf(navArgument("albumId") { type = NavType.StringType })
        ) {
            AlbumDetailScreen(navController = navController)
        }
        composable(
            route = Screen.ArtistDetail.route,
            arguments = listOf(navArgument("artistId") { type = NavType.StringType })
        ) {
            ArtistDetailScreen(navController = navController)
        }
        composable(
            route = Screen.GenreFeed.route,
            arguments = listOf(navArgument("genreId") { type = NavType.StringType })
        ) {
            GenreFeedScreen(navController = navController)
        }
        composable(Screen.LikedSongs.route) {
            LikedSongsScreen(navController = navController)
        }
        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
        ) {
            PlaylistDetailScreen(navController = navController)
        }
        composable(
            route = Screen.ShowDetail.route,
            arguments = listOf(navArgument("showId") { type = NavType.StringType })
        ) {
            ShowDetailScreen(navController = navController)
        }
        composable(
            route = Screen.EditPlaylist.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
        ) {
            EditPlaylistScreen(onBack = { navController.popBackStack() })
        }

        // ── User ────────────────────────────────────────────────────────────
        composable(Screen.Profile.route) {
            ProfileScreen(
                onSettings = { navController.navigate(Screen.Settings.route) },
                onHistory = { navController.navigate(Screen.History.route) }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = { navController.navigate(Screen.Onboarding.route) { popUpTo(0) { inclusive = true } } }
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(navController = navController)
        }
    }
}
