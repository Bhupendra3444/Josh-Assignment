package com.example.sampletasks.audio

import com.example.sampletasks.model.TaskType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min
import kotlin.random.Random

class DesktopRecordingManager : RecordingManager {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val _state = MutableStateFlow<RecordingState>(RecordingState.Idle)
    override val state: StateFlow<RecordingState> = _state
    private var tickerJob: Job? = null
    private var elapsed = 0

    override suspend fun start(taskType: TaskType) {
        elapsed = 0
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (true) {
                delay(1_000)
                elapsed += 1
                _state.value = RecordingState.Recording(elapsed)
            }
        }
        _state.value = RecordingState.Recording(0)
    }

    override suspend fun stop(): RecordingResult {
        tickerJob?.cancel()
        val duration = elapsed.coerceAtLeast(1)
        val file = File.createTempFile("desktop-recording-${Random.nextInt()}", ".wav")
        writeSilentWav(file, duration)
        val result = RecordingResult(file.absolutePath, duration)
        _state.value = RecordingState.Ready(result.filePath, duration)
        return result
    }

    override suspend fun cancel() {
        tickerJob?.cancel()
        _state.value = RecordingState.Idle
    }
}

private fun writeSilentWav(file: File, durationSec: Int) {
    val duration = durationSec.coerceAtLeast(1)
    val sampleRate = 16_000
    val channels = 1
    val bitsPerSample = 16
    val byteRate = sampleRate * channels * bitsPerSample / 8
    val dataSize = duration * byteRate
    val totalDataLen = dataSize + 36
    val header = ByteArray(44)

    fun writeInt(value: Int, offset: Int) {
        header[offset] = (value and 0xff).toByte()
        header[offset + 1] = (value shr 8 and 0xff).toByte()
        header[offset + 2] = (value shr 16 and 0xff).toByte()
        header[offset + 3] = (value shr 24 and 0xff).toByte()
    }

    fun writeShort(value: Int, offset: Int) {
        header[offset] = (value and 0xff).toByte()
        header[offset + 1] = (value shr 8 and 0xff).toByte()
    }

    header[0] = 'R'.code.toByte()
    header[1] = 'I'.code.toByte()
    header[2] = 'F'.code.toByte()
    header[3] = 'F'.code.toByte()
    writeInt(totalDataLen, 4)
    header[8] = 'W'.code.toByte()
    header[9] = 'A'.code.toByte()
    header[10] = 'V'.code.toByte()
    header[11] = 'E'.code.toByte()
    header[12] = 'f'.code.toByte()
    header[13] = 'm'.code.toByte()
    header[14] = 't'.code.toByte()
    header[15] = ' '.code.toByte()
    writeInt(16, 16)
    writeShort(1, 20)
    writeShort(channels, 22)
    writeInt(sampleRate, 24)
    writeInt(byteRate, 28)
    writeShort(channels * bitsPerSample / 8, 32)
    writeShort(bitsPerSample, 34)
    header[36] = 'd'.code.toByte()
    header[37] = 'a'.code.toByte()
    header[38] = 't'.code.toByte()
    header[39] = 'a'.code.toByte()
    writeInt(dataSize, 40)

    FileOutputStream(file).use { out ->
        out.write(header)
        val buffer = ByteArray(1024)
        var remaining = dataSize
        while (remaining > 0) {
            val chunk = min(remaining, buffer.size)
            out.write(buffer, 0, chunk)
            remaining -= chunk
        }
    }
}
