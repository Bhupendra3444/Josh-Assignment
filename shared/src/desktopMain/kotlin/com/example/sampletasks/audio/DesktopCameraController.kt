package com.example.sampletasks.audio

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class DesktopCameraController : CameraController {
    override suspend fun capturePhoto(): String = withContext(Dispatchers.Default) {
        "desktop-photo-${Random.nextInt()}.jpg"
    }
}
