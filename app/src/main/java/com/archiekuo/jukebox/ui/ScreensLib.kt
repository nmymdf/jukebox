package com.archiekuo.jukebox.ui

import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archiekuo.jukebox.R
import com.archiekuo.jukebox.data.Cat
import com.archiekuo.jukebox.data.Jamendo
import com.archiekuo.jukebox.data.Song
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(onOpenCat: (Cat) -> Unit, onOpenPlayer: () -> Unit, onSearch: () -> Unit) {
    val c = LocalJb.current
    val store = LocalStore.current
    val player = LocalPlayer.current
    var tab by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        TopBar("我的收藏", null) {
            IconButton(onClick = onSearch) {
                Icon(Icons.Filled.Search, contentDescription = "搜尋", tint = c.text)
            }
        }
        Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("收藏", "分類", "歷史").forEachIndexed { i, t -> Chip(t, tab == i) { tab = i } }
        }
        val list: List<Song> = if (tab == 0) store.favorites else store.history
        if (tab == 1) {
            val used = store.allCats().filter { store.songsIn(it.id).isNotEmpty() }
            if (used.isEmpty()) {
                EmptyHint("還沒有收進分類的歌。在任何歌曲右邊按 ⋮ → 加入分類。")
            } else {
                LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp)) {
                    items(used, key = { it.id }) { cat ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onOpenCat(cat) }
                                .padding(vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CatArt(cat, Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)))
                            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                                Text(cat.name, color = c.text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Text("${store.songsIn(cat.id).size} 首", color = c.subText, fontSize = 12.sp)
                            }
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = c.subText)
                        }
                    }
                }
            }
        } else if (list.isEmpty()) {
            EmptyHint(if (tab == 0) "還沒有收藏的歌。在歌曲右邊按 ⋮ → 加入收藏。" else "還沒有播放紀錄。")
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                itemsIndexed(list, key = { _, s -> s.id }) { i, s ->
                    SongRow(s) {
                        player.playQueue(list, i)
                        onOpenPlayer()
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    val c = LocalJb.current
    Text(text, color = c.subText, fontSize = 13.sp, modifier = Modifier.padding(24.dp))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(onBack: () -> Unit, onSettings: () -> Unit, onOpenPlayer: () -> Unit) {
    val c = LocalJb.current
    val store = LocalStore.current
    val player = LocalPlayer.current
    val scope = rememberCoroutineScope()
    var q by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Song>?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun run(text: String) {
        val t = text.trim()
        if (t.isEmpty()) return
        q = t
        store.addSearch(t)
        loading = true
        error = null
        scope.launch {
            try {
                results = Jamendo.search(store.clientId, t).distinctBy { it.id }
            } catch (e: Exception) {
                error = e.message ?: "連線失敗"
                results = null
            }
            loading = false
        }
    }

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
                onValueChange = {
                    q = it
                    if (it.isBlank()) {
                        results = null
                        error = null
                    }
                },
                singleLine = true,
                placeholder = { Text("搜尋歌名、歌手(英文效果較好)", color = c.subText) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = c.subText) },
                trailingIcon = {
                    if (q.isNotEmpty()) {
                        IconButton(onClick = {
                            q = ""
                            results = null
                            error = null
                        }) { Icon(Icons.Filled.Close, contentDescription = "清除", tint = c.subText) }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { run(q) }),
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = c.surfaceHigh,
                    unfocusedContainerColor = c.surfaceHigh,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedTextColor = c.text,
                    unfocusedTextColor = c.text,
                    cursorColor = c.accent,
                ),
                modifier = Modifier.weight(1f),
            )
        }

        if (store.clientId.isBlank()) {
            NoKeyCard(onSettings)
        } else if (loading) {
            LoadingRow()
        } else if (error != null) {
            ErrorText(error ?: "", { run(q) })
        } else if (results != null) {
            val list = results ?: emptyList()
            if (list.isEmpty()) {
                EmptyHint("找不到「$q」的結果,換個關鍵字試試(英文歌名、歌手通常比較準)。")
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                    itemsIndexed(list, key = { _, s -> s.id }) { i, s ->
                        SongRow(s) {
                            player.playQueue(list, i)
                            onOpenPlayer()
                        }
                    }
                }
            }
        } else {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text("試試這些", color = c.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    listOf("jazz", "piano", "chill", "acoustic", "rock", "lofi", "guitar", "ambient")
                        .forEach { s -> Chip(s, false) { run(s) } }
                }
                if (store.recentSearches.isNotEmpty()) {
                    Spacer(Modifier.height(22.dp))
                    Text("最近搜尋", color = c.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    store.recentSearches.toList().forEach { s ->
                        Row(
                            Modifier.fillMaxWidth().clickable { run(s) }.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.History, contentDescription = null, tint = c.subText)
                            Text(s, color = c.text, fontSize = 15.sp, modifier = Modifier.weight(1f).padding(start = 14.dp))
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "刪除",
                                tint = c.subText,
                                modifier = Modifier.clickable { store.removeSearch(s) },
                            )
                        }
                    }
                }
            }
        }
    }
}

private class SettingItem(
    val icon: ImageVector,
    val title: String,
    val sub: String,
    val click: (() -> Unit)?,
)

@Composable
fun SettingsScreen(onBack: () -> Unit, onSleep: () -> Unit, onAllCats: () -> Unit, onGuide: () -> Unit) {
    val c = LocalJb.current
    val store = LocalStore.current
    val scope = rememberCoroutineScope()
    var text by remember(store.clientId) { mutableStateOf(if (store.usingBuiltIn) "" else store.clientId) }
    var status by remember { mutableStateOf<String?>(null) }
    var testing by remember { mutableStateOf(false) }

    val items = listOf(
        SettingItem(Icons.Filled.Key, "取得 Client ID 教學", "一步一步帶你申請 Jamendo 的免費通行證", onGuide),
        SettingItem(Icons.Filled.Timer, "定時關閉", "睡眠模式、倒數停止播放", onSleep),
        SettingItem(Icons.Filled.Category, "分類管理", "新增、刪除自己的分類", onAllCats),
        SettingItem(Icons.Filled.Delete, "清除播放歷史", "目前 ${store.history.size} 首", { store.clearHistory() }),
        SettingItem(Icons.Filled.History, "清除搜尋記錄", "目前 ${store.recentSearches.size} 筆", { store.clearSearches() }),
        SettingItem(Icons.Filled.Info, "關於", "音樂來自 Jamendo,授權依各曲 Creative Commons,僅供個人非商業使用", null),
    )

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp)) {
        item { TopBar("設定", onBack) }
        item {
            Column(
                Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(c.surface)
                    .padding(16.dp),
            ) {
                Text("音樂來源:Jamendo", color = c.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    if (store.usingBuiltIn) {
                        "目前使用 App 內建的音樂來源,不用另外申請。也可以填入自己的 Client ID 來取代。"
                    } else {
                        "把申請到的 Client ID 貼在這裡。還沒有的話,用下面的「取得 Client ID 教學」。"
                    },
                    color = c.subText,
                    fontSize = 12.sp,
                )
                Spacer(Modifier.height(10.dp))
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    placeholder = { Text("Client ID", color = c.subText) },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = c.surfaceHigh,
                        unfocusedContainerColor = c.surfaceHigh,
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedTextColor = c.text,
                        unfocusedTextColor = c.text,
                        cursorColor = c.accent,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(c.accent)
                            .clickable {
                                store.saveClientId(text)
                                status = if (text.isBlank()) "已清除" else "已儲存"
                            }
                            .padding(horizontal = 20.dp, vertical = 9.dp),
                    ) { Text("儲存", color = c.onAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                    Chip(if (testing) "測試中…" else "測試連線", false) {
                        if (!testing) {
                            store.saveClientId(text)
                            testing = true
                            status = null
                            scope.launch {
                                status = try {
                                    val r = Jamendo.popular(store.clientId, 5)
                                    if (r.isEmpty()) "連線成功,但沒有回傳歌曲" else "連線成功,可以聽音樂了"
                                } catch (e: Exception) {
                                    "失敗:" + (e.message ?: "連線錯誤")
                                }
                                testing = false
                            }
                        }
                    }
                }
                val st = status
                if (st != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(st, color = c.accent, fontSize = 12.sp)
                }
            }
        }
        item { Spacer(Modifier.height(14.dp)) }
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
                        if (it0.click != null) {
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = c.subText)
                        }
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
            Box(
                Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF3B2A6B), Color(0xFF123F5A)))),
                contentAlignment = Alignment.Center,
            ) {
                Text("A good song, a place to rest", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        item {
            Column(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Credit(size = 14)
            }
        }
    }
}
