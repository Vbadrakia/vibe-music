package com.vibe.app.ui.navigation

sealed class Screen(val route: String) {
    // Auth
    object Splash          : Screen("splash")
    object Onboarding      : Screen("onboarding")
    object Login           : Screen("login")
    object Register        : Screen("register")
    object ForgotPassword  : Screen("forgot_password")

    // Main
    object Home            : Screen("home")
    object Search          : Screen("search")
    object SearchResults   : Screen("search_results/{query}") {
        fun createRoute(query: String) = "search_results/$query"
    }
    object Library         : Screen("library")

    // Player
    object NowPlaying      : Screen("now_playing")
    object Queue           : Screen("queue")
    object Lyrics          : Screen("lyrics")

    // Content
    object AlbumDetail     : Screen("album/{albumId}") {
        fun createRoute(id: String) = "album/$id"
    }
    object ArtistDetail    : Screen("artist/{artistId}") {
        fun createRoute(id: String) = "artist/$id"
    }
    object GenreFeed       : Screen("genre/{genreId}") {
        fun createRoute(id: String) = "genre/$id"
    }
    object LikedSongs      : Screen("liked_songs")
    object PlaylistDetail  : Screen("playlist/{playlistId}") {
        fun createRoute(id: String) = "playlist/$id"
    }
    object ShowDetail      : Screen("show/{showId}") {
        fun createRoute(id: String) = "show/$id"
    }
    object EditPlaylist    : Screen("playlist/{playlistId}/edit") {
        fun createRoute(id: String) = "playlist/$id/edit"
    }

    // User
    object Profile         : Screen("profile")
    object Settings        : Screen("settings")
    object History         : Screen("history")
    object Upload          : Screen("upload")
}
