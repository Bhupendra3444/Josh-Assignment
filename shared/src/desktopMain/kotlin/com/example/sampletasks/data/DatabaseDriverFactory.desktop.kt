package com.example.sampletasks.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.sampletasks.db.TaskDatabase

actual class DatabaseDriverFactory actual constructor(
    @Suppress("UNUSED_PARAMETER")
    private val context: Any?
) {
    actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        TaskDatabase.Schema.create(driver)
        return driver
    }
}
