package com.example.sampletasks.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.SystemClock
import com.example.sampletasks.model.TaskType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class AndroidRecordingManager(private val context: Context) : RecordingManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow<RecordingState>(RecordingState.Idle)
    override val state: StateFlow<RecordingState> = _state
    private var startTime: Long? = null
    private var tickerJob: Job? = null
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null

    override suspend fun start(taskType: TaskType) {
        cleanupRecorder(forceStop = true, deleteFile = true)
        val file = File(context.cacheDir, "recording-${System.currentTimeMillis()}.m4a")
        outputFile = file
        val recorder = MediaRecorder()
        try {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setAudioEncodingBitRate(128_000)
            recorder.setAudioSamplingRate(44_100)
            recorder.setOutputFile(file.absolutePath)
            recorder.prepare()
            recorder.start()
            mediaRecorder = recorder
            startTime = SystemClock.elapsedRealtime()
            startTicker()
            _state.value = RecordingState.Recording(0)
        } catch (t: Throwable) {
            recorder.release()
            mediaRecorder = null
            outputFile = null
            _state.value = RecordingState.Error(t.message ?: "Unable to start recording")
            throw t
        }
    }

    override suspend fun stop(): RecordingResult {
        val recorder = mediaRecorder ?: throw IllegalStateException("No active recording")
        val duration = ((SystemClock.elapsedRealtime() - (startTime ?: SystemClock.elapsedRealtime())) / 1000)
            .coerceAtLeast(1)
            .toInt()
        return try {
            recorder.stop()
            val file = outputFile ?: throw IllegalStateException("Missing audio file")
            val result = RecordingResult(file.absolutePath, duration)
            _state.value = RecordingState.Ready(result.filePath, result.durationSec)
            result
        } catch (t: Throwable) {
            outputFile?.delete()
            _state.value = RecordingState.Error(t.message ?: "Unable to stop recording")
            throw t
        } finally {
            cleanupRecorder()
        }
    }

    override suspend fun cancel() {
        cleanupRecorder(forceStop = true, deleteFile = true)
        _state.value = RecordingState.Idle
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (true) {
                delay(1_000)
                val start = startTime ?: return@launch
                val elapsed = ((SystemClock.elapsedRealtime() - start) / 1000).toInt()
                _state.value = RecordingState.Recording(elapsed)
            }
        }
    }

    private fun cleanupRecorder(forceStop: Boolean = false, deleteFile: Boolean = false) {
        tickerJob?.cancel()
        tickerJob = null
        startTime = null
        mediaRecorder?.let { recorder ->
            if (forceStop) {
                runCatching { recorder.stop() }
            }
            runCatching { recorder.reset() }
            recorder.release()
        }
        mediaRecorder = null
        if (deleteFile) {
            outputFile?.delete()
        }
        outputFile = null
    }
}
