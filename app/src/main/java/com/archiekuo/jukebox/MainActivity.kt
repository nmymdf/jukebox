package com.archiekuo.jukebox

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.archiekuo.jukebox.data.Store
import com.archiekuo.jukebox.player.PlayerController
import com.archiekuo.jukebox.ui.JukeboxApp
import com.archiekuo.jukebox.ui.JukeboxTheme

class MainActivity : ComponentActivity() {
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            JukeboxTheme {
                val store = remember { Store(applicationContext) }
                val player = remember { PlayerController(applicationContext) }
                JukeboxApp(store, player)
            }
        }
    }
}
