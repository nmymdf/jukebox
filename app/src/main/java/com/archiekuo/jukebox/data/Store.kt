package com.archiekuo.jukebox.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

/** Everything the user saves on the phone: Client ID, favorites, history, own categories. */
class Store(context: Context) {
    private val sp = context.getSharedPreferences("jukebox", Context.MODE_PRIVATE)

    var clientId by mutableStateOf(sp.getString("client_id", "") ?: "")
        private set

    val favorites = mutableStateListOf<Song>()
    val history = mutableStateListOf<Song>()
    val recentSearches = mutableStateListOf<String>()
    val customCats = mutableStateListOf<Cat>()
    val assigned = mutableStateMapOf<String, List<Song>>()

    init {
        favorites.addAll(readSongs("favorites"))
        history.addAll(readSongs("history"))
        recentSearches.addAll(readStrings("searches"))
        customCats.addAll(readCats())
        readAssigned()
    }

    // ---------- Client ID ----------
    fun saveClientId(value: String) {
        clientId = value.trim()
        sp.edit().putString("client_id", clientId).apply()
    }

    // ---------- Favorites ----------
    fun isFav(song: Song): Boolean = favorites.any { it.id == song.id }

    fun toggleFav(song: Song) {
        val idx = favorites.indexOfFirst { it.id == song.id }
        if (idx >= 0) favorites.removeAt(idx) else favorites.add(0, song)
        writeSongs("favorites", favorites)
    }

    // ---------- History ----------
    fun addHistory(song: Song) {
        val idx = history.indexOfFirst { it.id == song.id }
        if (idx >= 0) history.removeAt(idx)
        history.add(0, song)
        while (history.size > 50) history.removeAt(history.lastIndex)
        writeSongs("history", history)
    }

    fun clearHistory() {
        history.clear()
        writeSongs("history", history)
    }

    // ---------- Searches ----------
    fun addSearch(q: String) {
        val t = q.trim()
        if (t.isEmpty()) return
        recentSearches.remove(t)
        recentSearches.add(0, t)
        while (recentSearches.size > 10) recentSearches.removeAt(recentSearches.lastIndex)
        writeStrings("searches", recentSearches)
    }

    fun removeSearch(q: String) {
        recentSearches.remove(q)
        writeStrings("searches", recentSearches)
    }

    fun clearSearches() {
        recentSearches.clear()
        writeStrings("searches", recentSearches)
    }

    // ---------- Categories ----------
    fun allCats(): List<Cat> = Categories.builtin + customCats

    fun findCat(id: String): Cat? = allCats().firstOrNull { it.id == id }

    fun addCustom(cat: Cat) {
        customCats.add(cat)
        writeCats()
    }

    fun removeCustom(id: String) {
        customCats.removeAll { it.id == id }
        assigned.remove(id)
        writeCats()
        writeAssigned()
    }

    fun songsIn(catId: String): List<Song> = assigned[catId] ?: emptyList()

    fun isAssigned(catId: String, song: Song): Boolean = songsIn(catId).any { it.id == song.id }

    fun toggleAssign(catId: String, song: Song) {
        val cur = songsIn(catId)
        assigned[catId] =
            if (cur.any { it.id == song.id }) cur.filter { it.id != song.id } else listOf(song) + cur
        writeAssigned()
    }

    // ---------- persistence ----------
    private fun readSongs(key: String): List<Song> {
        val s = sp.getString(key, null) ?: return emptyList()
        return try {
            val a = JSONArray(s)
            (0 until a.length()).map { Song.fromJson(a.getJSONObject(it)) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun writeSongs(key: String, list: List<Song>) {
        val a = JSONArray()
        list.forEach { a.put(it.toJson()) }
        sp.edit().putString(key, a.toString()).apply()
    }

    private fun readStrings(key: String): List<String> {
        val s = sp.getString(key, null) ?: return emptyList()
        return try {
            val a = JSONArray(s)
            (0 until a.length()).map { a.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun writeStrings(key: String, list: List<String>) {
        val a = JSONArray()
        list.forEach { a.put(it) }
        sp.edit().putString(key, a.toString()).apply()
    }

    private fun readCats(): List<Cat> {
        val s = sp.getString("custom_cats", null) ?: return emptyList()
        return try {
            val a = JSONArray(s)
            (0 until a.length()).map { Cat.fromJson(a.getJSONObject(it)) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun writeCats() {
        val a = JSONArray()
        customCats.forEach { a.put(it.toJson()) }
        sp.edit().putString("custom_cats", a.toString()).apply()
    }

    private fun readAssigned() {
        val s = sp.getString("assigned", null) ?: return
        try {
            val o = JSONObject(s)
            for (key in o.keys()) {
                val a = o.getJSONArray(key)
                assigned[key] = (0 until a.length()).map { Song.fromJson(a.getJSONObject(it)) }
            }
        } catch (e: Exception) {
            // ignore damaged data
        }
    }

    private fun writeAssigned() {
        val o = JSONObject()
        for ((k, list) in assigned) {
            val a = JSONArray()
            list.forEach { a.put(it.toJson()) }
            o.put(k, a)
        }
        sp.edit().putString("assigned", o.toString()).apply()
    }
}
