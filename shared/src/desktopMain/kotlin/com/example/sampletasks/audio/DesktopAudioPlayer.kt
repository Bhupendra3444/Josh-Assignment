package com.example.sampletasks.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.LineEvent

class DesktopAudioPlayer : AudioPlayer {
    private val _state = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private var clip: Clip? = null
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var progressJob: Job? = null
    private var currentSource: String? = null

    override fun play(filePath: String) {
        stop()
        currentSource = filePath
        runCatching {
            val audioFile = File(filePath)
            require(audioFile.exists()) { "Audio file missing" }
            val audioInput = AudioSystem.getAudioInputStream(audioFile)
            val clipInstance = AudioSystem.getClip()
            clipInstance.open(audioInput)
            audioInput.close()
            clipInstance.addLineListener { event ->
                if (event.type == LineEvent.Type.STOP) {
                    stop()
                }
            }
            clip = clipInstance
            clipInstance.start()
            _state.value = PlaybackState.Playing(filePath, 0f)
            startProgressUpdates()
        }.onFailure {
            _state.value = PlaybackState.Error(currentSource, it.message ?: "Audio playback failed")
            release()
        }
    }

    override fun pause() {
        if (clip?.isRunning == true) {
            clip?.stop()
            currentSource?.let { _state.value = PlaybackState.Paused(it) }
        }
        progressJob?.cancel()
    }

    override fun stop() {
        progressJob?.cancel()
        clip?.stop()
        clip?.close()
        clip = null
        currentSource = null
        _state.value = PlaybackState.Idle
    }

    override fun release() {
        stop()
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && clip != null) {
                val progress = clip?.let {
                    if (it.microsecondLength == 0L) 0f else it.microsecondPosition.toFloat() / it.microsecondLength.toFloat()
                } ?: 0f
                currentSource?.let { source ->
                    _state.value = PlaybackState.Playing(source, progress.coerceIn(0f, 1f))
                }
                delay(100)
            }
        }
    }
}
