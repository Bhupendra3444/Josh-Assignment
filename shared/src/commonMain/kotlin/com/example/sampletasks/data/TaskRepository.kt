package com.example.sampletasks.data

import com.example.sampletasks.model.TaskDraft
import com.example.sampletasks.model.TaskRecord
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasks(): Flow<List<TaskRecord>>
    suspend fun addTask(draft: TaskDraft)
}
