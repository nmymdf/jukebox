package com.archiekuo.jukebox.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archiekuo.jukebox.data.Jamendo
import com.archiekuo.jukebox.data.Song
import com.archiekuo.jukebox.data.Store
import com.archiekuo.jukebox.player.PlayerController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Splash : Screen
    data object Onboarding : Screen
    data object Home : Screen
    data object Categories : Screen
    data object Library : Screen
    data object Settings : Screen
    data object Search : Screen
    data object NowPlaying : Screen
    data object SleepTimer : Screen
    data class CategoryDetail(val catId: String) : Screen
}

@Composable
fun JukeboxApp(store: Store, player: PlayerController) {
    val c = LocalJb.current
    val scope = rememberCoroutineScope()
    val sleep = remember { SleepState() }
    val stack = remember { mutableStateListOf<Screen>(Screen.Splash) }
    val current = stack.last()
    var pickSong by remember { mutableStateOf<Song?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var homeMessage by remember { mutableStateOf<String?>(null) }

    fun push(s: Screen) {
        stack.add(s)
    }

    fun pop() {
        if (stack.size > 1) stack.removeAt(stack.lastIndex)
    }

    fun goTab(s: Screen) {
        stack.clear()
        stack.add(s)
    }

    fun playRandom() {
        if (store.clientId.isBlank()) {
            push(Screen.Onboarding)
            return
        }
        homeMessage = "正在找歌…"
        scope.launch {
            try {
                val list = Jamendo.popular(store.clientId, 30).distinctBy { it.id }.shuffled()
                if (list.isEmpty()) {
                    homeMessage = "暫時找不到歌曲"
                } else {
                    homeMessage = null
                    player.playQueue(list, 0)
                    push(Screen.NowPlaying)
                }
            } catch (e: Exception) {
                homeMessage = e.message ?: "連線失敗"
            }
        }
    }

    BackHandler(enabled = stack.size > 1 && current != Screen.Splash) { pop() }

    // history list
    LaunchedEffect(Unit) {
        player.onSongChanged = { s -> store.addHistory(s) }
    }

    // keep the progress bar moving
    LaunchedEffect(Unit) {
        while (true) {
            player.refresh()
            delay(500)
        }
    }

    // sleep timer countdown, with a gentle fade at the end
    LaunchedEffect(sleep.running) {
        while (sleep.running && sleep.remaining > 0) {
            delay(1000)
            sleep.remaining = sleep.remaining - 1
            if (sleep.gentle && sleep.remaining in 1..20) {
                player.setVolume(sleep.remaining / 20f)
            }
        }
        if (sleep.running && sleep.remaining <= 0) {
            player.pause()
            sleep.cancel()
        }
        player.setVolume(1f)
    }

    CompositionLocalProvider(
        LocalStore provides store,
        LocalPlayer provides player,
        LocalPickCategory provides { s: Song -> pickSong = s },
    ) {
        if (current == Screen.Splash) {
            SplashScreen(
                onDone = {
                    if (store.clientId.isBlank() && !store.onboardSkipped) {
                        goTab(Screen.Onboarding)
                    } else {
                        goTab(Screen.Home)
                    }
                },
            )
        } else {
            val showBar = when (current) {
                Screen.Home, Screen.Categories, Screen.Library, Screen.Settings,
                is Screen.CategoryDetail -> true
                else -> false
            }
            val selected = when (current) {
                Screen.Home -> 0
                Screen.Categories, is Screen.CategoryDetail -> 1
                Screen.Library -> 3
                Screen.Settings -> 4
                else -> -1
            }

            Column(Modifier.fillMaxSize().background(c.bg)) {
                Box(Modifier.weight(1f)) {
                    when (val s = current) {
                        Screen.Splash -> {}
                        Screen.Onboarding -> OnboardingScreen(onDone = { goTab(Screen.Home) })
                        Screen.Home -> HomeScreen(
                            message = homeMessage,
                            onSearch = { push(Screen.Search) },
                            onSettings = { goTab(Screen.Settings) },
                            onAllCats = { goTab(Screen.Categories) },
                            onOpenCat = { id -> push(Screen.CategoryDetail(id)) },
                            onPlayRandom = { playRandom() },
                        )
                        Screen.Categories -> CategoriesScreen(
                            onSearch = { push(Screen.Search) },
                            onOpen = { cat -> push(Screen.CategoryDetail(cat.id)) },
                            onAdd = { showAdd = true },
                        )
                        is Screen.CategoryDetail -> {
                            val cat = store.findCat(s.catId)
                            if (cat == null) {
                                LaunchedEffect(s.catId) { pop() }
                            } else {
                                CategoryDetailScreen(
                                    cat = cat,
                                    onBack = { pop() },
                                    onSettings = { push(Screen.Onboarding) },
                                    onOpenPlayer = { push(Screen.NowPlaying) },
                                    onDeleted = { pop() },
                                )
                            }
                        }
                        Screen.Library -> LibraryScreen(
                            onOpenCat = { cat -> push(Screen.CategoryDetail(cat.id)) },
                            onOpenPlayer = { push(Screen.NowPlaying) },
                            onSearch = { push(Screen.Search) },
                        )
                        Screen.NowPlaying -> NowPlayingScreen(
                            onBack = { pop() },
                            onSleep = { push(Screen.SleepTimer) },
                        )
                        Screen.Search -> SearchScreen(
                            onBack = { pop() },
                            onSettings = { push(Screen.Onboarding) },
                            onOpenPlayer = { push(Screen.NowPlaying) },
                        )
                        Screen.SleepTimer -> SleepTimerScreen(sleep = sleep, onBack = { pop() })
                        Screen.Settings -> SettingsScreen(
                            onBack = { goTab(Screen.Home) },
                            onSleep = { push(Screen.SleepTimer) },
                            onAllCats = { goTab(Screen.Categories) },
                            onGuide = { push(Screen.Onboarding) },
                        )
                    }
                }
                if (current != Screen.Onboarding) {
                    Box(Modifier.fillMaxWidth().padding(bottom = 2.dp), contentAlignment = Alignment.Center) {
                        Credit(size = 10)
                    }
                }
                if (showBar) {
                    BottomBar(
                        selected = selected,
                        playing = player.playing,
                        onHome = { goTab(Screen.Home) },
                        onCategories = { goTab(Screen.Categories) },
                        onPlay = { push(Screen.NowPlaying) },
                        onLibrary = { goTab(Screen.Library) },
                        onMore = { goTab(Screen.Settings) },
                    )
                }
            }

            val song = pickSong
            if (song != null) {
                CategoryPickerDialog(song = song, onDismiss = { pickSong = null })
            }
            if (showAdd) {
                AddCategoryDialog(
                    onDismiss = { showAdd = false },
                    onSave = { cat ->
                        store.addCustom(cat)
                        showAdd = false
                    },
                )
            }
        }
    }
}

@Composable
private fun BottomBar(
    selected: Int,
    playing: Boolean,
    onHome: () -> Unit,
    onCategories: () -> Unit,
    onPlay: () -> Unit,
    onLibrary: () -> Unit,
    onMore: () -> Unit,
) {
    val c = LocalJb.current
    Row(
        Modifier
            .fillMaxWidth()
            .background(c.surface)
            .navigationBarsPadding()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BarItem(Icons.Filled.Home, "首頁", selected == 0, onHome)
        BarItem(Icons.Filled.GridView, "分類", selected == 1, onCategories)
        Box(
            Modifier
                .size(52.dp)
                .clip(CircleShape)
                .border(2.dp, c.accent, CircleShape)
                .clickable(onClick = onPlay),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = "正在播放",
                tint = c.accent,
                modifier = Modifier.size(30.dp),
            )
        }
        BarItem(Icons.Filled.Person, "我的", selected == 3, onLibrary)
        BarItem(Icons.Filled.MoreHoriz, "更多", selected == 4, onMore)
    }
}

@Composable
private fun BarItem(icon: ImageVector, label: String, on: Boolean, onClick: () -> Unit) {
    val c = LocalJb.current
    val tint = if (on) c.accent else c.subText
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
        Text(label, color = tint, fontSize = 11.sp)
    }
}
