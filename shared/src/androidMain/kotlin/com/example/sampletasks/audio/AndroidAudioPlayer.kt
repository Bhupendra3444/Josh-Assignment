package com.example.sampletasks.audio

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AndroidAudioPlayer(private val context: Context) : AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private val _state = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null
    private var currentSource: String? = null

    override fun play(filePath: String) {
        currentSource = filePath
        release()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    _state.value = PlaybackState.Playing(filePath, 0f)
                    startProgressUpdates()
                }
                setOnCompletionListener {
                    stop()
                }
                setOnErrorListener { _, _, _ ->
                    _state.value = PlaybackState.Error(currentSource, "Playback failed")
                    release()
                    true
                }
            }
        } catch (e: Exception) {
            _state.value = PlaybackState.Error(currentSource, "Failed to load audio: ${e.message}")
        }
    }

    override fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            currentSource?.let { _state.value = PlaybackState.Paused(it) }
        }
        progressJob?.cancel()
    }

    override fun stop() {
        progressJob?.cancel()
        release()
        _state.value = PlaybackState.Idle
    }

    override fun release() {
        progressJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        currentSource = null
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && mediaPlayer?.isPlaying == true) {
                val progress = mediaPlayer?.let { it.currentPosition.toFloat() / it.duration.toFloat() } ?: 0f
                currentSource?.let {
                    _state.value = PlaybackState.Playing(it, progress.coerceIn(0f, 1f))
                }
                delay(100)
            }
        }
    }
}
