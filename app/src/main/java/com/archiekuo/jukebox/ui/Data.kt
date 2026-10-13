package com.archiekuo.jukebox.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archiekuo.jukebox.R

const val APP_VERSION = "v1.0.0"
const val CREDIT = "作者:ArchieKUO ‧ $APP_VERSION"

data class Track(val title: String, val artist: String, val cover: Int, val seconds: Int)
data class Category(val name: String, val sub: String, val image: Int)
data class Channel(
    val title: String,
    val desc: String,
    val plays: String,
    val image: Int,
    val live: Boolean = false,
)
data class Playlist(val name: String, val info: String, val image: Int)

/** Sample data for the interface stage. Real songs will come from Jamendo later. */
object Sample {
    val tracks = listOf(
        Track("Golden Hour Drive", "範例歌手 A", R.drawable.album_main, 214),
        Track("Midnight Neon", "範例歌手 B", R.drawable.ch_3, 198),
        Track("Soft Rain Café", "範例歌手 C", R.drawable.fav_1, 236),
        Track("Blue Note Evening", "範例歌手 D", R.drawable.fav_4, 251),
        Track("Open Road", "範例歌手 E", R.drawable.fav_5, 205),
        Track("Warm Vinyl", "範例歌手 F", R.drawable.cat_classic, 189),
    )

    val categories = listOf(
        Category("獨立流行", "Indie Pop", R.drawable.cat_pop),
        Category("懷舊經典", "Classic", R.drawable.cat_classic),
        Category("輕音樂", "Easy Listening", R.drawable.cat_easy),
        Category("爵士樂", "Jazz", R.drawable.cat_jazz),
        Category("心靈放鬆", "Chill", R.drawable.cat_healing),
    )

    val chips = listOf("全部", "人聲", "心情", "曲風", "情境")

    val channels = listOf(
        Channel("獨立音樂放送站", "24小時不間斷", "", R.drawable.ch_1, live = true),
        Channel("女聲精選", "溫柔嗓音 · 不間斷", "播放 12.6萬", R.drawable.ch_2),
        Channel("電子夜晚", "霓虹節拍 · 夜間必備", "播放 8.3萬", R.drawable.ch_3),
        Channel("男聲精選", "低沉磁性 · 暖心嗓音", "播放 6.7萬", R.drawable.ch_4),
        Channel("活力派對", "聚會與運動節奏", "播放 5.4萬", R.drawable.ch_5),
        Channel("復古卡帶", "懷舊旋律 · 老派浪漫", "播放 4.2萬", R.drawable.ch_6),
    )

    val featured = listOf(
        Playlist("療癒心靈", "輕柔放鬆", R.drawable.feat_1),
        Playlist("經典老歌", "懷舊時光", R.drawable.feat_2),
        Playlist("旅遊必聽", "出發的路上", R.drawable.fav_5),
    )

    val hotChannels = listOf(
        "女聲" to R.drawable.ch_2,
        "男聲" to R.drawable.ch_4,
        "電子" to R.drawable.ch_3,
        "懷舊" to R.drawable.ch_6,
    )

    val playlists = listOf(
        Playlist("放鬆心情", "32首 · 2小時18分", R.drawable.fav_1),
        Playlist("懷舊老歌", "45首 · 3小時12分", R.drawable.fav_2),
        Playlist("Jazz 音樂", "37首 · 2小時41分", R.drawable.fav_4),
        Playlist("旅遊必聽", "26首 · 1小時48分", R.drawable.fav_5),
        Playlist("浪漫約會", "30首 · 2小時05分", R.drawable.fav_6),
    )

    val hotSearches = listOf("女聲", "爵士", "跑步", "純音樂", "平靜", "咖啡廳", "電子", "藍調")
}

/** Fake player used while the interface is being built. */
class PlayerState {
    var index by mutableIntStateOf(0)
    var playing by mutableStateOf(false)
    var elapsed by mutableIntStateOf(0)

    val track: Track get() = Sample.tracks[index]

    fun toggle() { playing = !playing }

    fun next() {
        index = (index + 1) % Sample.tracks.size
        elapsed = 0
    }

    fun prev() {
        if (elapsed > 3) {
            elapsed = 0
        } else {
            index = (index - 1 + Sample.tracks.size) % Sample.tracks.size
            elapsed = 0
        }
    }

    fun tick() {
        elapsed++
        if (elapsed >= track.seconds) next()
    }

    fun play(i: Int) {
        index = i
        elapsed = 0
        playing = true
    }
}

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
