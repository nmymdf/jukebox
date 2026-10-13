package com.archiekuo.jukebox.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Mic
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.archiekuo.jukebox.data.Cat
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
        Column(
            Modifier.align(Alignment.TopCenter).fillMaxWidth().statusBarsPadding().padding(top = 110.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "點唱收音機", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(Color(0x99000000), androidx.compose.ui.geometry.Offset(0f, 3f), 8f)),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "隨時隨地 · 聽見美好", color = Color.White.copy(alpha = 0.92f), fontSize = 17.sp,
                style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(Color(0x99000000), androidx.compose.ui.geometry.Offset(0f, 2f), 6f)),
            )
        }
        Text(
            "Music Brings Us Closer", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 46.dp),
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

/** Home fits on one screen: the picture grows or shrinks to the space that is left. */
@Composable
fun HomeScreen(
    message: String?,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onAllCats: () -> Unit,
    onOpenCat: (String) -> Unit,
    onPlayRandom: () -> Unit,
) {
    val c = LocalJb.current
    val store = LocalStore.current
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val heroH = (maxHeight * 0.26f).coerceIn(110.dp, 180.dp)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 8.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Mic, contentDescription = null, tint = c.accent, modifier = Modifier.size(26.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("點唱收音機", color = c.accent, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("隨時隨地 · 聽見美好", color = c.subText, fontSize = 12.sp)
                }
                IconButton(onClick = onSearch) {
                    Icon(Icons.Filled.Search, contentDescription = "搜尋", tint = c.text)
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Filled.Menu, contentDescription = "設定", tint = c.text)
                }
            }
            Box(
                Modifier
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .fillMaxWidth()
                    .aspectRatio(197f / 110f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onPlayRandom),
            ) {
                Image(
                    painter = painterResource(R.drawable.home_hero),
                    contentDescription = "隨機播放熱門歌曲",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Column(Modifier.align(Alignment.CenterStart).padding(start = 18.dp)) {
                    val sh = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(Color(0xAA000000), androidx.compose.ui.geometry.Offset(0f, 3f), 8f))
                    Text("A Good Song,", color = Color.White, fontSize = 30.sp, fontFamily = ScriptFont, fontWeight = FontWeight.Bold, style = sh)
                    Text("A Place to Rest.", color = Gold, fontSize = 30.sp, fontFamily = ScriptFont, fontWeight = FontWeight.Bold, style = sh)
                }
            }
            if (message != null) {
                Text(
                    message,
                    color = c.accent,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            SectionTitle("精選分類", "全部", onAllCats)
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                listOf("mood_calm", "genre_jazz", "ctx_travel").forEach { id ->
                    val cat = store.findCat(id)
                    if (cat != null) FeaturedCard(cat, Modifier.weight(1f)) { onOpenCat(cat.id) }
                }
            }
            SectionTitle("快速選擇", null)
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                listOf("vocal_female", "vocal_male", "instrumental", "mood_happy").forEach { id ->
                    val cat = store.findCat(id)
                    if (cat != null) QuickCircle(cat) { onOpenCat(cat.id) }
                }
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun FeaturedCard(cat: Cat, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(112.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        CatArt(cat, Modifier.fillMaxSize())
        GradientScrim(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(60.dp))
        Text(
            cat.name,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomStart).padding(10.dp),
        )
    }
}

@Composable
private fun QuickCircle(cat: Cat, onClick: () -> Unit) {
    val c = LocalJb.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        CatArt(cat, Modifier.size(62.dp).clip(CircleShape))
        Spacer(Modifier.height(6.dp))
        Text(cat.name, color = c.text, fontSize = 13.sp)
    }
}
