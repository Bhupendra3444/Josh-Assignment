package com.example.sampletasks.audio

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AndroidCameraController(private val context: Context) : CameraController {
    override suspend fun capturePhoto(): String = withContext(Dispatchers.IO) {
        // TODO hook into actual CameraX pipeline and persist captured bitmap
        val file = File(context.cacheDir, "photo-${System.currentTimeMillis()}.jpg")
        file.writeBytes(byteArrayOf())
        file.absolutePath
    }
}
