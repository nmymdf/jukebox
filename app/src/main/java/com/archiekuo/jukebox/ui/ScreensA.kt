package com.archiekuo.jukebox.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archiekuo.jukebox.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2200)
        onDone()
    }
    Box(Modifier.fillMaxSize().background(Color(0xFF0B1226))) {
        Image(
            painter = painterResource(R.drawable.splash_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(110.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC0B1226)))),
        )
        Credit(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 18.dp),
            size = 14,
        )
    }
}

@Composable
fun HomeScreen(
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onCategories: () -> Unit,
    onDetail: (String, Int) -> Unit,
    onPlay: () -> Unit,
) {
    val c = LocalJb.current
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 8.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("點唱收音機", color = c.accent, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text("隨時隨地 · 聽見美好", color = c.subText, fontSize = 13.sp)
                }
                IconButton(onClick = onSearch) {
                    Icon(Icons.Filled.Search, contentDescription = "搜尋", tint = c.text)
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Filled.Menu, contentDescription = "設定", tint = c.text)
                }
            }
        }
        item {
            Box(
                Modifier
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .fillMaxWidth()
                    .aspectRatio(197f / 110f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onPlay),
            ) {
                Image(
                    painter = painterResource(R.drawable.home_hero),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        item { SectionTitle("精選推薦", "更多", onCategories) }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(Sample.featured) { p ->
                    Box(
                        Modifier
                            .width(116.dp)
                            .height(156.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onDetail(p.name, p.image) },
                    ) {
                        Image(
                            painter = painterResource(p.image),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        Box(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(70.dp)
                                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xDD0B1226)))),
                        )
                        Column(Modifier.align(Alignment.BottomStart).padding(10.dp)) {
                            Text(p.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(p.info, color = Color(0xCCFFFFFF), fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        item { SectionTitle("熱門頻道", "更多", onCategories) }
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Sample.hotChannels.forEach { (name, img) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onDetail(name, img) },
                    ) {
                        Image(
                            painter = painterResource(img),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(name, color = c.text, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoriesScreen(onSearch: () -> Unit, onOpen: (Category) -> Unit) {
    val c = LocalJb.current
    var sel by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        TopBar("分類", null) {
            IconButton(onClick = onSearch) {
                Icon(Icons.Filled.Search, contentDescription = "搜尋", tint = c.text)
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(Sample.chips.size) { i ->
                Chip(Sample.chips[i], sel == i) { sel = i }
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(Sample.categories) { cat ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(76.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onOpen(cat) },
                ) {
                    Image(
                        painter = painterResource(cat.image),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xE60B1226), Color(0x990B1226), Color.Transparent),
                                ),
                            ),
                    )
                    Column(
                        Modifier.align(Alignment.CenterStart).padding(start = 18.dp),
                    ) {
                        Text(cat.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(cat.sub, color = Color(0xCCFFFFFF), fontSize = 12.sp)
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun ChannelListScreen(title: String, onBack: () -> Unit, onSearch: () -> Unit, onOpen: (Channel) -> Unit) {
    val c = LocalJb.current
    var tab by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        TopBar(title, onBack) {
            IconButton(onClick = onSearch) {
                Icon(Icons.Filled.Search, contentDescription = "搜尋", tint = c.text)
            }
        }
        Row(
            Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            listOf("最新", "最熱門", "收藏").forEachIndexed { i, t -> Chip(t, tab == i) { tab = i } }
        }
        LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp)) {
            items(Sample.channels) { ch ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onOpen(ch) }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Cover(ch.image, Modifier.size(58.dp), 12.dp)
                    Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                        Text(ch.title, color = c.text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text(ch.desc, color = c.subText, fontSize = 12.sp)
                        if (ch.live) {
                            Box(
                                Modifier
                                    .padding(top = 4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFE5484D))
                                    .padding(horizontal = 6.dp, vertical = 1.dp),
                            ) { Text("LIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                        } else {
                            Text(ch.plays, color = c.subText, fontSize = 11.sp)
                        }
                    }
                    Icon(Icons.Filled.MoreVert, contentDescription = null, tint = c.subText)
                }
            }
        }
    }
}

@Composable
fun PlayCircleButton(size: Int, onClick: () -> Unit) {
    val c = LocalJb.current
    Box(
        Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(c.accent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = "播放", tint = c.onAccent, modifier = Modifier.size((size * 0.6f).dp))
    }
}
