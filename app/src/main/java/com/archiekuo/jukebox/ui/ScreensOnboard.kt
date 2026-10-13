package com.archiekuo.jukebox.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archiekuo.jukebox.data.Jamendo
import kotlinx.coroutines.launch

/** First-run guide: walks the user through getting a free Jamendo Client ID. */
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val c = LocalJb.current
    val store = LocalStore.current
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text("歡迎使用點唱收音機", color = c.accent, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "音樂來自 Jamendo(免費、合法的獨立音樂)。第一次使用,需要一個免費的「音樂通行證」(Client ID),只要申請一次,大約 3 分鐘。",
            color = c.subText,
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(20.dp))

        StepRow(1, "按下面的按鈕,開啟 Jamendo 開發者網站。")
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(c.accent)
                .clickable { uriHandler.openUri("https://developer.jamendo.com") }
                .padding(horizontal = 22.dp, vertical = 11.dp),
        ) {
            Text("開啟 Jamendo 申請頁", color = c.onAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(Modifier.height(18.dp))
        StepRow(2, "註冊或登入 Jamendo 帳號(免費)。")
        Spacer(Modifier.height(12.dp))
        StepRow(3, "建立一個應用程式(Create an application)。名稱隨意,例如「點唱收音機」,用途填個人、非商業。")
        Spacer(Modifier.height(12.dp))
        StepRow(4, "建立後會看到一串英數字的 Client ID,長按把它複製起來。")
        Spacer(Modifier.height(12.dp))
        StepRow(5, "回到這裡,按「從剪貼簿貼上」,再按「測試並開始」。")
        Spacer(Modifier.height(6.dp))
        Text(
            "網站的畫面可能和說明略有不同,找到那串 Client ID 就對了。",
            color = c.subText,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 36.dp),
        )

        Spacer(Modifier.height(20.dp))
        TextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            placeholder = { Text("貼上 Client ID", color = c.subText) },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = c.surfaceHigh,
                unfocusedContainerColor = c.surfaceHigh,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = c.text,
                unfocusedTextColor = c.text,
                cursorColor = c.accent,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Chip("從剪貼簿貼上", false) {
                val clip = clipboard.getText()?.text ?: ""
                if (clip.isBlank()) status = "剪貼簿是空的,請先複製 Client ID" else {
                    text = clip.trim()
                    status = null
                }
            }
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(c.accent)
                    .clickable {
                        val id = text.trim()
                        if (id.isEmpty()) {
                            status = "請先貼上 Client ID"
                        } else if (!busy) {
                            busy = true
                            status = "測試中…"
                            store.saveClientId(id)
                            scope.launch {
                                try {
                                    val r = Jamendo.popular(id, 5)
                                    if (r.isEmpty()) {
                                        status = "連線成功,但沒有回傳歌曲,請確認 Client ID 是否正確"
                                    } else {
                                        status = "成功!開始聽歌吧"
                                        onDone()
                                    }
                                } catch (e: Exception) {
                                    status = "失敗:" + (e.message ?: "連線錯誤") + "。請確認 Client ID 是否正確。"
                                }
                                busy = false
                            }
                        }
                    }
                    .padding(horizontal = 22.dp, vertical = 10.dp),
            ) {
                Text(
                    if (busy) "測試中…" else "測試並開始",
                    color = c.onAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
        }
        val st = status
        if (st != null) {
            Spacer(Modifier.height(10.dp))
            Text(st, color = c.accent, fontSize = 13.sp)
        }
        Spacer(Modifier.height(22.dp))
        Text(
            "稍後再說",
            color = c.subText,
            fontSize = 13.sp,
            modifier = Modifier
                .clickable {
                    store.skipOnboarding()
                    onDone()
                }
                .padding(vertical = 8.dp),
        )
    }
}

@Composable
private fun StepRow(n: Int, text: String) {
    val c = LocalJb.current
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier.size(24.dp).clip(CircleShape).background(c.accent),
            contentAlignment = Alignment.Center,
        ) { Text("$n", color = c.onAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(12.dp))
        Text(text, color = c.text, fontSize = 14.sp, modifier = Modifier.weight(1f))
    }
}
