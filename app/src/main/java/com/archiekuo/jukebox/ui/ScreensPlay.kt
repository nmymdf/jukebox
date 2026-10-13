package com.archiekuo.jukebox.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archiekuo.jukebox.data.licenseLabel

@Composable
fun NowPlayingScreen(onBack: () -> Unit, onSleep: () -> Unit) {
    val c = LocalJb.current
    val store = LocalStore.current
    val player = LocalPlayer.current
    val pick = LocalPickCategory.current
    val song = player.song
    var volume by remember { mutableStateOf(0.8f) }
    var drag by remember { mutableStateOf<Float?>(null) }
    val sliderColors = SliderDefaults.colors(
        thumbColor = c.accent,
        activeTrackColor = c.accent,
        inactiveTrackColor = c.surfaceHigh,
    )

    Column(Modifier.fillMaxSize().navigationBarsPadding()) {
        TopBar("正在播放", onBack)
        if (song == null) {
            Column(
                Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("還沒有播放任何歌曲", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "到「分類」挑一個分類,或在首頁點大圖隨機播放。",
                    color = c.subText,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
            }
            return@Column
        }

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            SongArt(
                song.image,
                Modifier.padding(horizontal = 36.dp, vertical = 8.dp).aspectRatio(1f),
                24.dp,
            )
        }
        Column(Modifier.padding(horizontal = 28.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                song.title,
                color = c.text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(song.artist, color = c.subText, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val lic = licenseLabel(song.license)
            if (lic.isNotEmpty()) Text("授權:$lic", color = c.subText, fontSize = 11.sp)
        }

        val dur = if (player.durationMs > 0) player.durationMs else 1L
        Column(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
            Slider(
                value = drag ?: (player.positionMs.toFloat() / dur).coerceIn(0f, 1f),
                onValueChange = { drag = it },
                onValueChangeFinished = {
                    val d = drag
                    if (d != null) player.seekTo((d * dur).toLong())
                    drag = null
                },
                colors = sliderColors,
            )
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                Text(formatMs(player.positionMs), color = c.subText, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text(formatMs(player.durationMs), color = c.subText, fontSize = 12.sp)
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { player.previous() }, modifier = Modifier.size(64.dp)) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = "上一首", tint = c.text, modifier = Modifier.size(36.dp))
            }
            Spacer(Modifier.width(14.dp))
            Box(
                Modifier
                    .size(78.dp)
                    .clip(CircleShape)
                    .background(c.accent)
                    .clickable { player.toggle() },
                contentAlignment = Alignment.Center,
            ) {
                if (player.buffering) {
                    CircularProgressIndicator(color = c.onAccent, modifier = Modifier.size(34.dp), strokeWidth = 3.dp)
                } else {
                    Icon(
                        if (player.playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "播放或暫停",
                        tint = c.onAccent,
                        modifier = Modifier.size(44.dp),
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            IconButton(onClick = { player.next() }, modifier = Modifier.size(64.dp)) {
                Icon(Icons.Filled.SkipNext, contentDescription = "下一首", tint = c.text, modifier = Modifier.size(36.dp))
            }
        }

        val err = player.error
        if (err != null) {
            Text(
                err,
                color = c.accent,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            )
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { pick(song) }) {
                Icon(Icons.Filled.PlaylistAdd, contentDescription = "加入分類", tint = c.subText)
            }
            IconButton(onClick = { store.toggleFav(song) }) {
                Icon(
                    if (store.isFav(song)) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "收藏",
                    tint = if (store.isFav(song)) c.accent else c.subText,
                )
            }
            IconButton(onClick = onSleep) {
                Icon(Icons.Filled.Timer, contentDescription = "定時關閉", tint = c.subText)
            }
        }
        Slider(
            value = volume,
            onValueChange = {
                volume = it
                player.setVolume(it)
            },
            colors = sliderColors,
            modifier = Modifier.padding(horizontal = 36.dp),
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun SleepTimerScreen(sleep: SleepState, onBack: () -> Unit) {
    val c = LocalJb.current
    val total = sleep.minutes * 60
    val progress = if (sleep.running && total > 0) sleep.remaining.toFloat() / total else 1f
    val shown = if (sleep.running) sleep.remaining else total
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        TopBar("定時關閉", onBack)
        Spacer(Modifier.height(24.dp))
        Box(Modifier.size(250.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = 6.dp.toPx()
                val topLeft = Offset(stroke / 2, stroke / 2)
                val arcSize = Size(size.width - stroke, size.height - stroke)
                drawArc(
                    color = c.line,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke),
                )
                drawArc(
                    color = c.accent,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Bedtime, contentDescription = null, tint = c.accent)
                Text("定時關閉", color = c.subText, fontSize = 13.sp)
                Text(formatTime(shown), color = c.text, fontSize = 50.sp, fontWeight = FontWeight.Bold)
                Text("後停止播放", color = c.subText, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(15, 30, 60, 90).forEach { m ->
                val on = sleep.minutes == m
                Box(
                    Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(if (on) c.accent else c.surfaceHigh)
                        .clickable(enabled = !sleep.running) { sleep.minutes = m },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "$m",
                        color = if (on) c.onAccent else c.text,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Text("分鐘", color = c.subText, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
        Spacer(Modifier.height(20.dp))
        Box(
            Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(c.accent)
                .clickable { if (sleep.running) sleep.cancel() else sleep.start() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (sleep.running) "取消計時" else "開始計時",
                color = c.onAccent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(18.dp))
        Row(
            Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(c.surface)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Bedtime, contentDescription = null, tint = c.accent)
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text("睡眠模式", color = c.text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text("最後 20 秒音量慢慢變小", color = c.subText, fontSize = 12.sp)
            }
            Switch(
                checked = sleep.gentle,
                onCheckedChange = { sleep.gentle = it },
                colors = SwitchDefaults.colors(checkedTrackColor = c.accent, checkedThumbColor = c.onAccent),
            )
        }
    }
}
