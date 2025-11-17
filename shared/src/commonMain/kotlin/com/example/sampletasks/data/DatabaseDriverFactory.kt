package com.example.sampletasks.data

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory(context: Any? = null) {
    fun createDriver(): SqlDriver
}
