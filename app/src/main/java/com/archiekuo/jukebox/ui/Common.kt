package com.archiekuo.jukebox.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.archiekuo.jukebox.data.CREDIT
import com.archiekuo.jukebox.data.Cat
import com.archiekuo.jukebox.data.Song

private val palettes: Map<String, List<Pair<Color, Color>>> = mapOf(
    "人聲" to listOf(
        Color(0xFF6A4C93) to Color(0xFF2B2150),
        Color(0xFF8E5572) to Color(0xFF3A2145),
        Color(0xFF5C6BC0) to Color(0xFF1F2A5C),
    ),
    "心情" to listOf(
        Color(0xFFE08E45) to Color(0xFF7A3B2E),
        Color(0xFF4A90A4) to Color(0xFF1B3B5A),
        Color(0xFFB5838D) to Color(0xFF5A3A4B),
    ),
    "曲風" to listOf(
        Color(0xFF3F8F7B) to Color(0xFF18413F),
        Color(0xFF7A9E4F) to Color(0xFF2E4A24),
        Color(0xFF4F7CAC) to Color(0xFF1B3556),
    ),
    "情境" to listOf(
        Color(0xFFD1654B) to Color(0xFF5E2A3A),
        Color(0xFFC9964B) to Color(0xFF5A3A1D),
        Color(0xFF8A6FB5) to Color(0xFF3A2A5E),
    ),
)

fun catBrush(cat: Cat): Brush {
    val list = palettes[cat.group] ?: palettes.getValue("情境")
    val p = list[Math.abs(cat.id.hashCode() % list.size)]
    return Brush.linearGradient(listOf(p.first, p.second))
}

private fun catIcon(cat: Cat): androidx.compose.ui.graphics.vector.ImageVector {
    val id = cat.id
    return when {
        id.contains("male") && !id.contains("female") -> Icons.Filled.Mic
        id.contains("female") -> Icons.Filled.Mic
        id.contains("duet") || id.contains("choir") -> Icons.Filled.Groups
        id.contains("band") || id.contains("rock") -> Icons.Filled.Album
        id.contains("instrumental") || id.contains("classical") || id.contains("jazz") -> Icons.Filled.Piano
        id.contains("calm") || id.contains("sleep") || id.contains("easy") -> Icons.Filled.Bedtime
        id.contains("happy") || id.contains("beach") || id.contains("sun") -> Icons.Filled.WbSunny
        id.contains("romantic") || id.contains("warm") -> Icons.Filled.Favorite
        id.contains("run") || id.contains("workout") || id.contains("gym") -> Icons.Filled.DirectionsRun
        id.contains("travel") -> Icons.Filled.Flight
        id.contains("electronic") -> Icons.Filled.GraphicEq
        else -> when (cat.group) {
            "人聲" -> Icons.Filled.Mic
            "心情" -> Icons.Filled.Favorite
            "曲風" -> Icons.Filled.Album
            else -> Icons.Filled.Headphones
        }
    }
}

/** Category picture: always sharp, drawn in code (gradient + soft circles + icon). */
@Composable
fun CatArt(cat: Cat, modifier: Modifier = Modifier) {
    Box(modifier.background(catBrush(cat)), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color.White.copy(alpha = 0.10f), radius = size.minDimension * 0.7f, center = Offset(size.width * 0.85f, size.height * 0.1f))
            drawCircle(Color.White.copy(alpha = 0.07f), radius = size.minDimension * 0.5f, center = Offset(size.width * 0.1f, size.height * 0.95f))
        }
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val side = if (maxWidth < maxHeight) maxWidth else maxHeight
            Icon(catIcon(cat), contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(side * 0.42f))
        }
    }
}

/** Album cover from the web, with a plain placeholder. */
@Composable
fun SongArt(url: String, modifier: Modifier = Modifier, radius: Dp = 10.dp) {
    val c = LocalJb.current
    if (url.isBlank()) {
        Box(
            modifier.clip(RoundedCornerShape(radius)).background(c.surfaceHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.MusicNote, contentDescription = null, tint = c.subText)
        }
    } else {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(RoundedCornerShape(radius)).background(c.surfaceHigh),
        )
    }
}

@Composable
fun TopBar(
    title: String,
    onBack: (() -> Unit)?,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val c = LocalJb.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = c.text)
            }
        } else {
            Spacer(Modifier.width(12.dp))
        }
        Text(
            text = title,
            color = c.text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        actions()
    }
}

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    val c = LocalJb.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) c.accent else c.surfaceHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (selected) c.onAccent else c.text,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

/** Author credit: gold, italic, with version. */
@Composable
fun Credit(modifier: Modifier = Modifier, size: Int = 12) {
    Text(
        text = CREDIT,
        color = Gold,
        fontStyle = FontStyle.Italic,
        fontSize = size.sp,
        modifier = modifier,
    )
}

@Composable
fun SectionTitle(title: String, more: String?, onMore: () -> Unit = {}) {
    val c = LocalJb.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = c.text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        if (more != null) {
            Text(
                text = "$more ›",
                color = c.subText,
                fontSize = 13.sp,
                modifier = Modifier.clickable(onClick = onMore),
            )
        }
    }
}

fun formatTime(seconds: Int): String {
    val s = if (seconds < 0) 0 else seconds
    return "%02d:%02d".format(s / 60, s % 60)
}

fun formatMs(ms: Long): String = formatTime((ms / 1000).toInt())

/** One song in a list, with a menu for favorite / add to category. */
@Composable
fun SongRow(song: Song, onClick: () -> Unit) {
    val c = LocalJb.current
    val store = LocalStore.current
    val player = LocalPlayer.current
    val pick = LocalPickCategory.current
    var menu by remember { mutableStateOf(false) }
    val current = player.song?.id == song.id
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 20.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SongArt(song.image, Modifier.size(54.dp), 10.dp)
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(
                song.title,
                color = if (current) c.accent else c.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                song.artist + (if (song.seconds > 0) " · " + formatTime(song.seconds) else ""),
                color = c.subText,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box {
            IconButton(onClick = { menu = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "更多", tint = c.subText)
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text(if (store.isFav(song)) "取消收藏" else "加入收藏") },
                    onClick = {
                        store.toggleFav(song)
                        menu = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("加入分類…") },
                    onClick = {
                        menu = false
                        pick(song)
                    },
                )
            }
        }
    }
}

@Composable
fun LoadingRow() {
    val c = LocalJb.current
    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = c.accent, modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
    }
}

/** Shown wherever music is needed but no Jamendo Client ID has been entered yet. */
@Composable
fun NoKeyCard(onSettings: () -> Unit) {
    val c = LocalJb.current
    Column(
        Modifier
            .padding(20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.surface)
            .padding(18.dp),
    ) {
        Text("還沒設定音樂來源", color = c.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "要聽音樂,需要先申請一個免費的 Jamendo Client ID,我們會一步一步帶你完成,大約 3 分鐘。",
            color = c.subText,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(c.accent)
                .clickable(onClick = onSettings)
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            Text("開始申請", color = c.onAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun ErrorText(message: String, onRetry: (() -> Unit)? = null) {
    val c = LocalJb.current
    Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message, color = c.subText, fontSize = 13.sp)
        if (onRetry != null) {
            Spacer(Modifier.height(10.dp))
            Chip("重試", false, onRetry)
        }
    }
}

@Composable
fun FillBox(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize()) { content() }
}

@Composable
fun GradientScrim(modifier: Modifier = Modifier, endColor: Color = Color(0xDD0B1226)) {
    Box(modifier.background(Brush.verticalGradient(listOf(Color.Transparent, endColor))))
}
