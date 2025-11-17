package com.example.sampletasks.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.sampletasks.db.TaskDatabase

actual class DatabaseDriverFactory actual constructor(
    private val context: Any?
) {
    actual fun createDriver(): SqlDriver {
        val appContext = requireNotNull(context as? Context) { "Android context required for SQLDelight driver" }
        return AndroidSqliteDriver(TaskDatabase.Schema, appContext, "tasks.db")
    }
}
