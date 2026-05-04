package com.vibe.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.vibe.app.media.PlayerController
import com.vibe.app.ui.navigation.VibeNavGraph
import com.vibe.app.ui.theme.VibeBg
import com.vibe.app.ui.theme.VibeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var playerController: PlayerController

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        playerController.connectToService()

        setContent {
            VibeTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = VibeBg) {
                    val navController = rememberNavController()
                    VibeNavGraph(navController = navController)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerController.release()
    }
}
