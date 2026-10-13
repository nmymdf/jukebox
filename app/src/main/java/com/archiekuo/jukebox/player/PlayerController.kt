package com.archiekuo.jukebox.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.archiekuo.jukebox.data.Song

/** Talks to [PlaybackService] and exposes the playback state to Compose. */
class PlayerController(context: Context) {
    private var controller: MediaController? = null
    private var queue: List<Song> = emptyList()
    private var pending: (() -> Unit)? = null

    var playing by mutableStateOf(false)
        private set
    var song by mutableStateOf<Song?>(null)
        private set
    var positionMs by mutableLongStateOf(0L)
        private set
    var durationMs by mutableLongStateOf(0L)
        private set
    var buffering by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)

    /** Called when a new song starts (used for the history list). */
    var onSongChanged: ((Song) -> Unit)? = null

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            playing = isPlaying
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateSong(mediaItem)
            song?.let { s -> onSongChanged?.invoke(s) }
            error = null
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            buffering = playbackState == Player.STATE_BUFFERING
        }

        override fun onPlayerError(e: PlaybackException) {
            error = "播放失敗:" + e.errorCodeName
        }
    }

    init {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener(
            {
                try {
                    val c = future.get()
                    controller = c
                    c.addListener(listener)
                    playing = c.isPlaying
                    updateSong(c.currentMediaItem)
                    pending?.invoke()
                    pending = null
                } catch (e: Exception) {
                    error = "無法連接播放服務"
                }
            },
            ContextCompat.getMainExecutor(context),
        )
    }

    private fun updateSong(item: MediaItem?) {
        song = if (item == null) {
            null
        } else {
            queue.firstOrNull { it.id == item.mediaId } ?: Song(
                id = item.mediaId,
                title = item.mediaMetadata.title?.toString() ?: "",
                artist = item.mediaMetadata.artist?.toString() ?: "",
                album = item.mediaMetadata.albumTitle?.toString() ?: "",
                image = item.mediaMetadata.artworkUri?.toString() ?: "",
                audio = item.localConfiguration?.uri?.toString() ?: "",
                seconds = 0,
                license = "",
            )
        }
    }

    private fun Song.toMediaItem(): MediaItem {
        val meta = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
        if (image.isNotBlank()) meta.setArtworkUri(Uri.parse(image))
        return MediaItem.Builder()
            .setMediaId(id)
            .setUri(audio)
            .setMediaMetadata(meta.build())
            .build()
    }

    /** Replace the queue with [list] and start playing at [index]. */
    fun playQueue(list: List<Song>, index: Int) {
        if (list.isEmpty()) return
        val start = index.coerceIn(0, list.lastIndex)
        val action: () -> Unit = {
            val c = controller
            if (c != null) {
                queue = list
                error = null
                c.setMediaItems(list.map { it.toMediaItem() }, start, 0L)
                c.prepare()
                c.play()
            }
        }
        if (controller == null) pending = action else action()
    }

    fun toggle() {
        val c = controller ?: return
        if (c.isPlaying) {
            c.pause()
        } else {
            if (c.playbackState == Player.STATE_IDLE) c.prepare()
            c.play()
        }
    }

    fun pause() {
        controller?.pause()
    }

    fun next() {
        controller?.seekToNextMediaItem()
    }

    fun previous() {
        val c = controller ?: return
        if (c.currentPosition > 3000) c.seekTo(0) else c.seekToPreviousMediaItem()
    }

    fun seekTo(ms: Long) {
        controller?.seekTo(ms)
    }

    fun setVolume(v: Float) {
        controller?.volume = v
    }

    /** Call about twice a second while the player screen is visible. */
    fun refresh() {
        val c = controller ?: return
        positionMs = if (c.currentPosition < 0) 0L else c.currentPosition
        val d = c.duration
        durationMs = if (d > 0) d else (song?.seconds ?: 0) * 1000L
    }
}
