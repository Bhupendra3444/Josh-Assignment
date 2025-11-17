package com.example.sampletasks.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

enum class TaskType {
    TEXT_READING,
    IMAGE_DESCRIPTION,
    PHOTO_CAPTURE
}

@Serializable
data class TaskRecord(
    val id: Long,
    val taskType: TaskType,
    val text: String?,
    val imageUrl: String?,
    val imagePath: String?,
    val audioPath: String?,
    val durationSec: Int,
    val timestamp: Instant = Clock.System.now(),
    val metadata: String? = null
)

data class TaskDraft(
    val taskType: TaskType,
    val text: String? = null,
    val imageUrl: String? = null,
    val imagePath: String? = null,
    val audioPath: String? = null,
    val durationSec: Int,
    val metadata: String? = null,
    val timestamp: Instant = Clock.System.now()
)

data class TaskHistoryStats(
    val totalTasks: Int,
    val totalDurationSec: Int
)

data class NoiseTestResult(
    val averageDb: Int,
    val isPass: Boolean,
    val advice: String
)

data class RecordingValidationResult(
    val isValid: Boolean,
    val message: String? = null
)

object RecordingValidator {
    private const val MIN_SECONDS = 10
    private const val MAX_SECONDS = 20

    fun validate(durationSec: Int): RecordingValidationResult {
        if (durationSec < MIN_SECONDS) {
            return RecordingValidationResult(false, "Recording too short (min 10 s).")
        }
        if (durationSec > MAX_SECONDS) {
            return RecordingValidationResult(false, "Recording too long (max 20 s).")
        }
        return RecordingValidationResult(true)
    }
}

@Serializable
data class ProductSnippet(
    val id: Long,
    val title: String,
    val description: String,
    val thumbnail: String
)
