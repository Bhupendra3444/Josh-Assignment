package com.example.sampletasks.audio

import com.example.sampletasks.model.NoiseTestResult

interface NoiseMeter {
    suspend fun runNoiseTest(): NoiseTestResult
}
