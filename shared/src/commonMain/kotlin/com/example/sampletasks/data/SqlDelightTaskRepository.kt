package com.example.sampletasks.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.sampletasks.db.TaskDatabase
import com.example.sampletasks.db.TaskEntity
import com.example.sampletasks.model.TaskDraft
import com.example.sampletasks.model.TaskRecord
import com.example.sampletasks.model.TaskType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

class SqlDelightTaskRepository(
    driverFactory: DatabaseDriverFactory,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : TaskRepository {

    private val database = TaskDatabase(driverFactory.createDriver())
    private val queries = database.taskEntityQueries

    override fun observeTasks(): Flow<List<TaskRecord>> =
        queries.selectAll().asFlow().mapToList(dispatcher).map { rows ->
            rows.map { it.toModel() }
        }

    override suspend fun addTask(draft: TaskDraft) = withContext(dispatcher) {
        queries.insertTask(
            task_type = draft.taskType.name,
            text = draft.text,
            image_url = draft.imageUrl,
            image_path = draft.imagePath,
            audio_path = draft.audioPath,
            duration_sec = draft.durationSec.toLong(),
            timestamp_iso = draft.timestamp.toString(),
            metadata = draft.metadata
        )
    }

    private fun TaskEntity.toModel(): TaskRecord = TaskRecord(
        id = id ?: 0L,
        taskType = TaskType.valueOf(task_type),
        text = text,
        imageUrl = image_url,
        imagePath = image_path,
        audioPath = audio_path,
        durationSec = duration_sec?.toInt() ?: 0,
        timestamp = Instant.parse(timestamp_iso),
        metadata = metadata
    )
}
