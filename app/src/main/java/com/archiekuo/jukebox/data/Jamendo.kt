package com.archiekuo.jukebox.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** Minimal Jamendo API client (https://developer.jamendo.com). */
object Jamendo {
    private const val BASE = "https://api.jamendo.com/v3.0/tracks/"

    class JamendoException(message: String) : Exception(message)

    private class Raw(val song: Song, val vocalInstrumental: String, val gender: String)

    private fun enc(s: String): String = URLEncoder.encode(s, "UTF-8")

    /** [params] values must already be URL-encoded (a raw '+' separates several tags). */
    private fun buildUrl(clientId: String, params: List<Pair<String, String>>, limit: Int, offset: Int): String {
        val sb = StringBuilder(BASE)
        sb.append("?client_id=").append(enc(clientId))
        sb.append("&format=json&imagesize=300&limit=").append(limit)
        sb.append("&offset=").append(offset)
        for ((k, v) in params) sb.append('&').append(k).append('=').append(v)
        return sb.toString()
    }

    private fun httpGet(url: String): JSONObject {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 12000
        conn.readTimeout = 20000
        conn.setRequestProperty("User-Agent", "Jukebox/1.1")
        try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            if (stream == null) throw JamendoException("連線失敗(HTTP $code)")
            val text = stream.bufferedReader().use { it.readText() }
            val json = JSONObject(text)
            val h = json.optJSONObject("headers")
            if (h != null && h.optString("status") != "success") {
                val msg = h.optString("error_message")
                throw JamendoException(if (msg.isBlank()) "Jamendo 回傳錯誤(代碼 ${h.optInt("code")})" else msg)
            }
            return json
        } finally {
            conn.disconnect()
        }
    }

    private fun parse(json: JSONObject): List<Raw> {
        val arr = json.optJSONArray("results") ?: return emptyList()
        val out = ArrayList<Raw>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val audio = o.optString("audio")
            if (audio.isBlank()) continue
            val info = o.optJSONObject("musicinfo")
            out.add(
                Raw(
                    song = Song(
                        id = o.optString("id"),
                        title = o.optString("name"),
                        artist = o.optString("artist_name"),
                        album = o.optString("album_name"),
                        image = o.optString("image"),
                        audio = audio,
                        seconds = o.optInt("duration"),
                        license = o.optString("license_ccurl"),
                    ),
                    vocalInstrumental = info?.optString("vocalinstrumental") ?: "",
                    gender = info?.optString("gender") ?: "",
                ),
            )
        }
        return out
    }

    private fun fetch(clientId: String, params: List<Pair<String, String>>, limit: Int, offset: Int): List<Raw> =
        parse(httpGet(buildUrl(clientId, params, limit, offset)))

    /** Songs for a category. Tries strict filters first, then looser ones. */
    suspend fun forCategory(clientId: String, cat: Cat, limit: Int = 30, offset: Int = 0): List<Song> =
        withContext(Dispatchers.IO) {
            if (clientId.isBlank()) throw JamendoException("請先到「設定」貼上 Jamendo Client ID")

            val tagParam: List<Pair<String, String>> =
                if (cat.tags.isEmpty()) emptyList()
                else listOf("fuzzytags" to cat.tags.joinToString("+") { enc(it) })
            val rank: List<Pair<String, String>> =
                if (cat.tags.isEmpty()) listOf("order" to "popularity_month")
                else listOf("boost" to "popularity_month")

            val filters = ArrayList<Pair<String, String>>()
            when (cat.vocal) {
                "vocal" -> filters.add("vocalinstrumental" to "vocal")
                "instrumental" -> filters.add("vocalinstrumental" to "instrumental")
                "male" -> {
                    filters.add("vocalinstrumental" to "vocal")
                    filters.add("gender" to "male")
                }
                "female" -> {
                    filters.add("vocalinstrumental" to "vocal")
                    filters.add("gender" to "female")
                }
            }
            if (cat.speed.isNotBlank()) filters.add("speed" to cat.speed)

            var lastError: Exception? = null

            // 1) everything the server can filter
            try {
                val r = fetch(clientId, tagParam + filters + rank, limit, offset)
                if (r.isNotEmpty()) return@withContext r.map { it.song }
            } catch (e: Exception) {
                lastError = e
            }

            // 2) without server filters, filter by the song info on the phone
            try {
                val r = fetch(clientId, tagParam + rank + listOf("include" to "musicinfo"), limit * 3, offset)
                val kept = r.filter { raw ->
                    when (cat.vocal) {
                        "instrumental" -> raw.vocalInstrumental == "instrumental"
                        "vocal" -> raw.vocalInstrumental == "vocal"
                        "male" -> raw.gender == "male"
                        "female" -> raw.gender == "female"
                        else -> true
                    }
                }
                if (kept.isNotEmpty()) return@withContext kept.take(limit).map { it.song }
            } catch (e: Exception) {
                lastError = e
            }

            // 3) tags only
            if (cat.tags.isNotEmpty()) {
                try {
                    val r = fetch(clientId, tagParam, limit, offset)
                    if (r.isNotEmpty()) return@withContext r.map { it.song }
                } catch (e: Exception) {
                    lastError = e
                }

                // 4) plain text search on the first tag
                try {
                    val r = fetch(clientId, listOf("search" to enc(cat.tags.first())), limit, offset)
                    if (r.isNotEmpty()) return@withContext r.map { it.song }
                } catch (e: Exception) {
                    lastError = e
                }
            }

            if (lastError != null) throw lastError
            emptyList()
        }

    suspend fun search(clientId: String, query: String, limit: Int = 30, offset: Int = 0): List<Song> =
        withContext(Dispatchers.IO) {
            if (clientId.isBlank()) throw JamendoException("請先到「設定」貼上 Jamendo Client ID")
            try {
                fetch(clientId, listOf("search" to enc(query)), limit, offset).map { it.song }
            } catch (e: Exception) {
                fetch(clientId, listOf("namesearch" to enc(query)), limit, offset).map { it.song }
            }
        }

    suspend fun popular(clientId: String, limit: Int = 30): List<Song> =
        withContext(Dispatchers.IO) {
            if (clientId.isBlank()) throw JamendoException("請先到「設定」貼上 Jamendo Client ID")
            try {
                val r = fetch(clientId, listOf("order" to "popularity_month"), limit, 0)
                if (r.isNotEmpty()) return@withContext r.map { it.song }
            } catch (e: Exception) {
                // fall through to the plain request below
            }
            fetch(clientId, emptyList(), limit, 0).map { it.song }
        }
}
