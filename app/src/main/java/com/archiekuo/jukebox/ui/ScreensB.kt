package com.archiekuo.jukebox.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archiekuo.jukebox.R

@Composable
fun DetailScreen(
    title: String,
    cover: Int,
    onBack: () -> Unit,
    onPlayTrack: (Int) -> Unit,
) {
    val c = LocalJb.current
    var liked by remember { mutableStateOf(false) }
    var tab by remember { mutableIntStateOf(0) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Box(Modifier.fillMaxWidth().height(230.dp)) {
                Image(
                    painter = painterResource(cover),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x66000000), Color.Transparent, c.bg),
                            ),
                        ),
                )
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.statusBarsPadding().padding(start = 4.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, color = c.text, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton(onClick = { liked = !liked }) {
                        Icon(
                            if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "收藏",
                            tint = c.accent,
                        )
                    }
                }
                Text("精選獨立音樂 · 範例資料", color = c.subText, fontSize = 13.sp)
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(c.accent)
                            .clickable { onPlayTrack(0) }
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = c.onAccent)
                            Spacer(Modifier.width(6.dp))
                            Text("播放全部", color = c.onAccent, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("節目列表", "相關推薦").forEachIndexed { i, t -> Chip(t, tab == i) { tab = i } }
                }
                Spacer(Modifier.height(6.dp))
            }
        }
        items(Sample.tracks.size) { i ->
            val t = Sample.tracks[i]
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onPlayTrack(i) }
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Cover(t.cover, Modifier.size(54.dp), 10.dp)
                Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                    Text(t.title, color = c.text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text("${t.artist} · ${formatTime(t.seconds)}", color = c.subText, fontSize = 12.sp)
                }
                Icon(Icons.Filled.MoreVert, contentDescription = null, tint = c.subText)
            }
        }
    }
}

@Composable
fun NowPlayingScreen(
    player: PlayerState,
    onBack: () -> Unit,
    onSleep: () -> Unit,
) {
    val c = LocalJb.current
    var liked by remember { mutableStateOf(false) }
    var volume by remember { mutableStateOf(0.6f) }
    val t = player.track
    val sliderColors = SliderDefaults.colors(
        thumbColor = c.accent,
        activeTrackColor = c.accent,
        inactiveTrackColor = c.surfaceHigh,
    )
    Column(Modifier.fillMaxSize()) {
        TopBar("正在播放", onBack) {
            IconButton(onClick = { liked = !liked }) {
                Icon(
                    if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "收藏",
                    tint = c.accent,
                )
            }
        }
        Cover(
            t.cover,
            Modifier.padding(horizontal = 36.dp, vertical = 12.dp).fillMaxWidth().aspectRatio(1f),
            24.dp,
        )
        Column(Modifier.padding(horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("現在播放", color = c.subText, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            Text(t.title, color = c.text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(t.artist, color = c.subText, fontSize = 14.sp)
        }
        Column(Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
            Slider(
                value = player.elapsed.toFloat() / t.seconds,
                onValueChange = { player.elapsed = (it * t.seconds).toInt() },
                colors = sliderColors,
            )
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                Text(formatTime(player.elapsed), color = c.subText, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text("-" + formatTime(t.seconds - player.elapsed), color = c.subText, fontSize = 12.sp)
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { player.prev() }, modifier = Modifier.size(64.dp)) {
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
                Icon(
                    if (player.playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "播放或暫停",
                    tint = c.onAccent,
                    modifier = Modifier.size(44.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            IconButton(onClick = { player.next() }, modifier = Modifier.size(64.dp)) {
                Icon(Icons.Filled.SkipNext, contentDescription = "下一首", tint = c.text, modifier = Modifier.size(36.dp))
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(Icons.Filled.QueueMusic, contentDescription = "待播佇列", tint = c.subText)
            Icon(
                if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "收藏",
                tint = c.subText,
                modifier = Modifier.clickable { liked = !liked },
            )
            Icon(Icons.Filled.Timer, contentDescription = "定時關閉", tint = c.subText, modifier = Modifier.clickable(onClick = onSleep))
        }
        Slider(
            value = volume,
            onValueChange = { volume = it },
            colors = sliderColors,
            modifier = Modifier.padding(horizontal = 36.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(onBack: () -> Unit, onPlayTrack: (Int) -> Unit) {
    val c = LocalJb.current
    var q by remember { mutableStateOf("") }
    val recent = remember { mutableStateListOf("爵士", "咖啡廳", "女聲", "跑步") }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = c.text)
            }
            TextField(
                value = q,
                onValueChange = { q = it },
                singleLine = true,
                placeholder = { Text("搜尋頻道、歌手、節目...", color = c.subText) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = c.subText) },
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = c.surfaceHigh,
                    unfocusedContainerColor = c.surfaceHigh,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = c.text,
                    unfocusedTextColor = c.text,
                    cursorColor = c.accent,
                ),
                modifier = Modifier.weight(1f),
            )
        }
        if (q.isBlank()) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text("熱門搜尋", color = c.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Sample.hotSearches.forEach { s -> Chip(s, false) { q = s } }
                }
                Spacer(Modifier.height(22.dp))
                Text("最近搜尋", color = c.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                recent.toList().forEach { s ->
                    Row(
                        Modifier.fillMaxWidth().clickable { q = s }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.History, contentDescription = null, tint = c.subText)
                        Text(s, color = c.text, fontSize = 15.sp, modifier = Modifier.weight(1f).padding(start = 14.dp))
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "刪除",
                            tint = c.subText,
                            modifier = Modifier.clickable { recent.remove(s) },
                        )
                    }
                }
            }
        } else {
            val hits = Sample.tracks.withIndex().filter {
                it.value.title.contains(q, ignoreCase = true) || it.value.artist.contains(q, ignoreCase = true)
            }
            LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)) {
                if (hits.isEmpty()) {
                    item { Text("沒有找到相關結果(目前是範例資料)", color = c.subText, fontSize = 14.sp) }
                }
                items(hits) { h ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onPlayTrack(h.index) }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Cover(h.value.cover, Modifier.size(50.dp), 10.dp)
                        Column(Modifier.padding(start = 14.dp)) {
                            Text(h.value.title, color = c.text, fontSize = 15.sp)
                            Text(h.value.artist, color = c.subText, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(onSearch: () -> Unit, onOpen: (Playlist) -> Unit) {
    val c = LocalJb.current
    var tab by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        TopBar("我的收藏", null) {
            IconButton(onClick = onSearch) {
                Icon(Icons.Filled.Search, contentDescription = "搜尋", tint = c.text)
            }
        }
        Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("歌單", "收藏", "歷史").forEachIndexed { i, t -> Chip(t, tab == i) { tab = i } }
        }
        LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp)) {
            items(Sample.playlists) { p ->
                Row(
                    Modifier.fillMaxWidth().clickable { onOpen(p) }.padding(vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Cover(p.image, Modifier.size(60.dp), 12.dp)
                    Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                        Text(p.name, color = c.text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text(p.info, color = c.subText, fontSize = 12.sp)
                    }
                    Icon(Icons.Filled.MoreVert, contentDescription = null, tint = c.subText)
                }
            }
        }
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
                Text("分鐘後停止播放", color = c.subText, fontSize = 13.sp)
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
        Spacer(Modifier.height(24.dp))
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
        Spacer(Modifier.height(20.dp))
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
                Text("漸弱音量 · 溫柔入睡", color = c.subText, fontSize = 12.sp)
            }
            Switch(
                checked = sleep.gentle,
                onCheckedChange = { sleep.gentle = it },
                colors = SwitchDefaults.colors(checkedTrackColor = c.accent, checkedThumbColor = c.onAccent),
            )
        }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit, onSleep: () -> Unit) {
    val c = LocalJb.current
    data class Item(val icon: androidx.compose.ui.graphics.vector.ImageVector, val title: String, val sub: String, val click: (() -> Unit)?)
    val items = listOf(
        Item(Icons.Filled.Category, "分類管理", "新增、改名、刪除分類", null),
        Item(Icons.Filled.HighQuality, "音質設定", "標準 / 高音質 / 無損", null),
        Item(Icons.Filled.Timer, "定時關閉", "睡眠模式、倒數停止播放", onSleep),
        Item(Icons.Filled.Notifications, "通知設定", "新節目 / 活動通知", null),
        Item(Icons.Filled.Storage, "資料管理", "清除快取 / 匯出收藏", null),
        Item(Icons.Filled.Info, "關於我們", "版本 ${APP_VERSION.removePrefix("v")}", null),
    )
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp)) {
        item { TopBar("設定", onBack) }
        item {
            Column(
                Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(c.surface),
            ) {
                items.forEach { it0 ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable(enabled = it0.click != null) { it0.click?.invoke() }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier.size(38.dp).clip(CircleShape).background(c.surfaceHigh),
                            contentAlignment = Alignment.Center,
                        ) { Icon(it0.icon, contentDescription = null, tint = c.accent, modifier = Modifier.size(20.dp)) }
                        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                            Text(it0.title, color = c.text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text(it0.sub, color = c.subText, fontSize = 12.sp)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = c.subText)
                    }
                }
            }
        }
        item {
            Text(
                "外觀:跟隨系統的深色 / 淺色設定",
                color = c.subText,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }
        item {
            Box(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                Image(
                    painter = painterResource(R.drawable.settings_footer),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(225f / 150f)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        item {
            Column(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Credit(size = 14)
            }
        }
    }
}
