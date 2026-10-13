package com.archiekuo.jukebox.data

import org.json.JSONArray
import org.json.JSONObject

const val APP_VERSION = "v1.3.2"
const val CREDIT = "作者:ArchieKUO ‧ $APP_VERSION"

val GROUPS = listOf("人聲", "心情", "曲風", "情境")

/** One playable song from Jamendo. */
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val image: String,
    val audio: String,
    val seconds: Int,
    val license: String,
) {
    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("title", title)
        .put("artist", artist)
        .put("album", album)
        .put("image", image)
        .put("audio", audio)
        .put("seconds", seconds)
        .put("license", license)

    companion object {
        fun fromJson(o: JSONObject): Song = Song(
            id = o.optString("id"),
            title = o.optString("title"),
            artist = o.optString("artist"),
            album = o.optString("album"),
            image = o.optString("image"),
            audio = o.optString("audio"),
            seconds = o.optInt("seconds"),
            license = o.optString("license"),
        )
    }
}

/**
 * A category. [tags] are Jamendo tags (English). [vocal] is one of
 * any / vocal / instrumental / male / female.
 */
data class Cat(
    val id: String,
    val group: String,
    val name: String,
    val tags: List<String>,
    val vocal: String = "any",
    val speed: String = "",
    val image: Int = 0,
    val custom: Boolean = false,
) {
    val sub: String
        get() {
            val parts = ArrayList<String>()
            when (vocal) {
                "male" -> parts.add("男聲")
                "female" -> parts.add("女聲")
                "vocal" -> parts.add("有人聲")
                "instrumental" -> parts.add("純音樂")
            }
            parts.addAll(tags.take(3))
            return if (parts.isEmpty()) "熱門" else parts.joinToString(" · ")
        }

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("group", group)
        .put("name", name)
        .put("tags", JSONArray(tags))
        .put("vocal", vocal)
        .put("speed", speed)

    companion object {
        fun fromJson(o: JSONObject): Cat {
            val arr = o.optJSONArray("tags") ?: JSONArray()
            val tags = (0 until arr.length()).map { arr.getString(it) }
            return Cat(
                id = o.optString("id"),
                group = o.optString("group"),
                name = o.optString("name"),
                tags = tags,
                vocal = o.optString("vocal", "any"),
                speed = o.optString("speed", ""),
                custom = true,
            )
        }
    }
}

fun licenseLabel(url: String): String {
    val m = Regex("licenses/([^/]+)/([^/]+)").find(url) ?: return ""
    return "CC " + m.groupValues[1].uppercase() + " " + m.groupValues[2]
}
