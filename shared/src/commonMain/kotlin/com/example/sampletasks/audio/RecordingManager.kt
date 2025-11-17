package com.example.sampletasks.audio

import com.example.sampletasks.model.TaskType
import kotlinx.coroutines.flow.StateFlow

sealed interface RecordingState {
    object Idle : RecordingState
    data class Recording(val elapsedSec: Int) : RecordingState
    data class Ready(val filePath: String, val durationSec: Int) : RecordingState
    data class Error(val message: String) : RecordingState
}

data class RecordingResult(
    val filePath: String,
    val durationSec: Int
)

interface RecordingManager {
    val state: StateFlow<RecordingState>
    suspend fun start(taskType: TaskType)
    suspend fun stop(): RecordingResult
    suspend fun cancel()
}
