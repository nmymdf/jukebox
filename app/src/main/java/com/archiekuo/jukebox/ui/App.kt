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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

sealed interface Screen {
    data object Splash : Screen
    data object Home : Screen
    data object Categories : Screen
    data object Favorites : Screen
    data object Settings : Screen
    data object Search : Screen
    data object NowPlaying : Screen
    data object SleepTimer : Screen
    data class ChannelList(val title: String) : Screen
    data class Detail(val title: String, val cover: Int) : Screen
}

@Composable
fun JukeboxApp() {
    val c = LocalJb.current
    val player = remember { PlayerState() }
    val sleep = remember { SleepState() }
    val stack = remember { mutableStateListOf<Screen>(Screen.Splash) }
    val current = stack.last()

    fun push(s: Screen) { stack.add(s) }
    fun pop() { if (stack.size > 1) stack.removeAt(stack.lastIndex) }
    fun goTab(s: Screen) {
        stack.clear()
        stack.add(s)
    }

    BackHandler(enabled = stack.size > 1 && current != Screen.Splash) { pop() }

    // fake playback clock (real audio arrives with the Jamendo step)
    LaunchedEffect(player.playing) {
        while (player.playing) {
            delay(1000)
            player.tick()
        }
    }

    // sleep timer countdown
    LaunchedEffect(sleep.running) {
        while (sleep.running && sleep.remaining > 0) {
            delay(1000)
            sleep.remaining = sleep.remaining - 1
        }
        if (sleep.running && sleep.remaining <= 0) {
            player.playing = false
            sleep.cancel()
        }
    }

    if (current == Screen.Splash) {
        SplashScreen(onDone = { goTab(Screen.Home) })
        return
    }

    val showBar = when (current) {
        Screen.Home, Screen.Categories, Screen.Favorites, Screen.Settings,
        is Screen.ChannelList, is Screen.Detail -> true
        else -> false
    }
    val selected = when (current) {
        Screen.Home -> 0
        Screen.Categories, is Screen.ChannelList -> 1
        Screen.Favorites -> 3
        Screen.Settings -> 4
        else -> -1
    }

    Column(Modifier.fillMaxSize().background(c.bg)) {
        Box(Modifier.weight(1f)) {
            when (val s = current) {
                Screen.Splash -> {}
                Screen.Home -> HomeScreen(
                    onSearch = { push(Screen.Search) },
                    onSettings = { goTab(Screen.Settings) },
                    onCategories = { goTab(Screen.Categories) },
                    onDetail = { t, i -> push(Screen.Detail(t, i)) },
                    onPlay = {
                        player.playing = true
                        push(Screen.NowPlaying)
                    },
                )
                Screen.Categories -> CategoriesScreen(
                    onSearch = { push(Screen.Search) },
                    onOpen = { push(Screen.ChannelList(it.name)) },
                )
                is Screen.ChannelList -> ChannelListScreen(
                    title = s.title,
                    onBack = { pop() },
                    onSearch = { push(Screen.Search) },
                    onOpen = { push(Screen.Detail(it.title, it.image)) },
                )
                is Screen.Detail -> DetailScreen(
                    title = s.title,
                    cover = s.cover,
                    onBack = { pop() },
                    onPlayTrack = {
                        player.play(it)
                        push(Screen.NowPlaying)
                    },
                )
                Screen.NowPlaying -> NowPlayingScreen(
                    player = player,
                    onBack = { pop() },
                    onSleep = { push(Screen.SleepTimer) },
                )
                Screen.Search -> SearchScreen(
                    onBack = { pop() },
                    onPlayTrack = {
                        player.play(it)
                        push(Screen.NowPlaying)
                    },
                )
                Screen.Favorites -> FavoritesScreen(
                    onSearch = { push(Screen.Search) },
                    onOpen = { push(Screen.Detail(it.name, it.image)) },
                )
                Screen.SleepTimer -> SleepTimerScreen(sleep = sleep, onBack = { pop() })
                Screen.Settings -> SettingsScreen(
                    onBack = { goTab(Screen.Home) },
                    onSleep = { push(Screen.SleepTimer) },
                )
            }
        }
        if (showBar) {
            BottomBar(
                selected = selected,
                playing = player.playing,
                onHome = { goTab(Screen.Home) },
                onCategories = { goTab(Screen.Categories) },
                onPlay = { push(Screen.NowPlaying) },
                onFavorites = { goTab(Screen.Favorites) },
                onMore = { goTab(Screen.Settings) },
            )
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
    onFavorites: () -> Unit,
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
        BarItem(Icons.Filled.Person, "我的", selected == 3, onFavorites)
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
