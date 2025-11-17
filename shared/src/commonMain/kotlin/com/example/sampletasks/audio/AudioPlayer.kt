package com.example.sampletasks.audio

import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {
    val state: StateFlow<PlaybackState>
    fun play(filePath: String)
    fun pause()
    fun stop()
    fun release()
}

sealed interface PlaybackState {
    object Idle : PlaybackState
    data class Playing(val source: String, val progress: Float) : PlaybackState
    data class Paused(val source: String) : PlaybackState
    data class Error(val source: String?, val message: String) : PlaybackState
}
