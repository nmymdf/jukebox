package com.archiekuo.jukebox.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.archiekuo.jukebox.data.Song
import com.archiekuo.jukebox.data.Store
import com.archiekuo.jukebox.player.PlayerController

val LocalStore = staticCompositionLocalOf<Store> { error("Store not provided") }
val LocalPlayer = staticCompositionLocalOf<PlayerController> { error("Player not provided") }

/** Opens the "add this song to categories" dialog. */
val LocalPickCategory = staticCompositionLocalOf<(Song) -> Unit> { {} }

class SleepState {
    var minutes by mutableIntStateOf(30)
    var remaining by mutableIntStateOf(0)
    var running by mutableStateOf(false)
    var gentle by mutableStateOf(true)

    fun start() {
        remaining = minutes * 60
        running = true
    }

    fun cancel() {
        running = false
        remaining = 0
    }
}
