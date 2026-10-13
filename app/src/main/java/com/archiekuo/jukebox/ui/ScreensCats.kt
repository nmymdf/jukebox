package com.archiekuo.jukebox.ui

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archiekuo.jukebox.data.GROUPS
import com.archiekuo.jukebox.data.Cat
import com.archiekuo.jukebox.data.Jamendo
import com.archiekuo.jukebox.data.Song
import kotlinx.coroutines.launch

@Composable
fun CategoriesScreen(
    onSearch: () -> Unit,
    onOpen: (Cat) -> Unit,
    onAdd: () -> Unit,
) {
    val c = LocalJb.current
    val store = LocalStore.current
    var group by remember { mutableStateOf("全部") }
    val cats = store.allCats()
    Column(Modifier.fillMaxSize()) {
        TopBar("分類", null) {
            IconButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = "新增分類", tint = c.accent)
            }
            IconButton(onClick = onSearch) {
                Icon(Icons.Filled.Search, contentDescription = "搜尋", tint = c.text)
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(listOf("全部") + GROUPS) { g ->
                Chip(g, group == g) { group = g }
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GROUPS.forEach { g ->
                if (group == "全部" || group == g) {
                    val list = cats.filter { it.group == g }
                    if (list.isNotEmpty()) {
                        item(key = "header_$g") {
                            Text(
                                g,
                                color = c.subText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 10.dp),
                            )
                        }
                        items(list, key = { it.id }) { cat -> CatCard(cat) { onOpen(cat) } }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatCard(cat: Cat, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        CatArt(cat, Modifier.fillMaxSize())
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xE60B1226), Color(0x990B1226), Color.Transparent),
                    ),
                ),
        )
        Column(Modifier.align(Alignment.CenterStart).padding(start = 18.dp, end = 60.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(cat.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                if (cat.custom) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Gold)
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                    ) { Text("自訂", color = Color(0xFF2A1B00), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                }
            }
            Text(cat.sub, color = Color(0xCCFFFFFF), fontSize = 12.sp, maxLines = 1)
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryDetailScreen(
    cat: Cat,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onOpenPlayer: () -> Unit,
    onDeleted: () -> Unit,
) {
    val c = LocalJb.current
    val store = LocalStore.current
    val player = LocalPlayer.current
    val scope = rememberCoroutineScope()

    var tab by remember(cat.id) { mutableIntStateOf(0) }
    var songs by remember(cat.id) { mutableStateOf<List<Song>>(emptyList()) }
    var loading by remember(cat.id) { mutableStateOf(false) }
    var error by remember(cat.id) { mutableStateOf<String?>(null) }
    var offset by remember(cat.id) { mutableIntStateOf(0) }
    var end by remember(cat.id) { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }

    fun load(more: Boolean) {
        if (loading) return
        loading = true
        error = null
        val start = if (more) offset else 0
        scope.launch {
            try {
                val r = Jamendo.forCategory(store.clientId, cat, 30, start).distinctBy { it.id }
                songs = if (more) songs + r.filter { n -> songs.none { it.id == n.id } } else r
                offset = start + 30
                if (r.size < 10) end = true
            } catch (e: Exception) {
                error = e.message ?: "連線失敗"
            }
            loading = false
        }
    }

    LaunchedEffect(cat.id, store.clientId) {
        songs = emptyList()
        offset = 0
        end = false
        if (store.clientId.isNotBlank()) load(false)
    }

    val mine = store.songsIn(cat.id)
    val shown = if (tab == 0) songs else mine

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Box(Modifier.fillMaxWidth().height(190.dp)) {
                CatArt(cat, Modifier.fillMaxSize())
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color(0x66000000), Color.Transparent, c.bg))),
                )
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp)) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                    }
                    Spacer(Modifier.weight(1f))
                    if (cat.custom) {
                        Box {
                            IconButton(onClick = { menu = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "更多", tint = Color.White)
                            }
                            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                DropdownMenuItem(
                                    text = { Text("刪除這個分類") },
                                    onClick = {
                                        menu = false
                                        store.removeCustom(cat.id)
                                        onDeleted()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text(cat.name, color = c.text, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(cat.sub, color = c.subText, fontSize = 13.sp)
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(c.accent)
                            .clickable {
                                if (shown.isNotEmpty()) {
                                    player.playQueue(shown, 0)
                                    onOpenPlayer()
                                }
                            }
                            .padding(horizontal = 22.dp, vertical = 11.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = c.onAccent)
                            Spacer(Modifier.width(6.dp))
                            Text("播放全部", color = c.onAccent, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Chip("探索", tab == 0) { tab = 0 }
                    Chip("我的歌 (${mine.size})", tab == 1) { tab = 1 }
                }
                Spacer(Modifier.height(6.dp))
            }
        }

        if (tab == 0 && store.clientId.isBlank()) {
            item { NoKeyCard(onSettings) }
        } else if (tab == 1 && mine.isEmpty()) {
            item {
                Text(
                    "這裡還沒有歌。到「探索」找喜歡的歌,按右邊的 ⋮ → 加入分類,就會收進這個分類。",
                    color = c.subText,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(20.dp),
                )
            }
        } else {
            itemsIndexed(shown, key = { _, s -> s.id }) { i, s ->
                SongRow(s) {
                    player.playQueue(shown, i)
                    onOpenPlayer()
                }
            }
            if (tab == 0) {
                if (loading) item { LoadingRow() }
                val err = error
                if (err != null) item { ErrorText(err) { load(songs.isNotEmpty()) } }
                if (!loading && err == null && songs.isEmpty()) {
                    item { ErrorText("這個分類目前找不到歌曲,可以試試別的分類或到搜尋輸入關鍵字。") }
                }
                if (!loading && !end && songs.isNotEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            Chip("載入更多", false) { load(true) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCategoryDialog(onDismiss: () -> Unit, onSave: (Cat) -> Unit) {
    val c = LocalJb.current
    var name by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("情境") }
    var tags by remember { mutableStateOf("") }
    var vocal by remember { mutableStateOf("any") }
    val vocalOptions = listOf("any" to "不限", "vocal" to "有人聲", "instrumental" to "純音樂", "male" to "男聲", "female" to "女聲")
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        title = { Text("新增分類", color = c.text) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名稱,例如:雨夜") },
                    singleLine = true,
                )
                Spacer(Modifier.height(14.dp))
                Text("分組", color = c.subText, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GROUPS.forEach { g -> Chip(g, group == g) { group = g } }
                }
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("標籤(英文,逗號分隔)") },
                    singleLine = true,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "例如:jazz, piano, rain。用 Jamendo 的英文標籤找歌;不填也可以,之後自己把歌加進來。",
                    color = c.subText,
                    fontSize = 11.sp,
                )
                Spacer(Modifier.height(14.dp))
                Text("人聲", color = c.subText, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    vocalOptions.forEach { (key, label) -> Chip(label, vocal == key) { vocal = key } }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val n = name.trim()
                    if (n.isNotEmpty()) {
                        val list = tags.split(",", "，").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
                        onSave(
                            Cat(
                                id = "custom_" + System.currentTimeMillis(),
                                group = group,
                                name = n,
                                tags = list,
                                vocal = vocal,
                                custom = true,
                            ),
                        )
                    }
                },
            ) { Text("新增", color = c.accent) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消", color = c.subText) } },
    )
}

/** Tick the categories a song should belong to. */
@Composable
fun CategoryPickerDialog(song: Song, onDismiss: () -> Unit) {
    val c = LocalJb.current
    val store = LocalStore.current
    val cats = store.allCats()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        title = { Text("加入分類", color = c.text) },
        text = {
            Column {
                Text(song.title, color = c.subText, fontSize = 13.sp, maxLines = 1)
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.heightIn(max = 380.dp)) {
                    GROUPS.forEach { g ->
                        val list = cats.filter { it.group == g }
                        if (list.isNotEmpty()) {
                            item(key = "pick_header_$g") {
                                Text(
                                    g,
                                    color = c.accent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
                                )
                            }
                            items(list, key = { "pick_" + it.id }) { cat ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable { store.toggleAssign(cat.id, song) }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        checked = store.isAssigned(cat.id, song),
                                        onCheckedChange = null,
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = c.accent,
                                            checkmarkColor = c.onAccent,
                                            uncheckedColor = c.subText,
                                        ),
                                        modifier = Modifier.padding(end = 8.dp),
                                    )
                                    Text(cat.name, color = c.text, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("完成", color = c.accent) } },
    )
}
