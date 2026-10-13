package com.archiekuo.jukebox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archiekuo.jukebox.ui.JukeboxApp
import com.archiekuo.jukebox.ui.JukeboxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JukeboxTheme {
                JukeboxApp()
            }
        }
    }
}
