package com.example.sampletasks.audio

import com.example.sampletasks.model.NoiseTestResult
import kotlinx.coroutines.delay
import kotlin.random.Random

class AndroidNoiseMeter : NoiseMeter {
    override suspend fun runNoiseTest(): NoiseTestResult {
        delay(1_500)
        val value = Random.nextInt(30, 61)
        val pass = value < 40
        val advice = if (pass) "Good to proceed" else "Please move to a quieter place"
        return NoiseTestResult(value, pass, advice)
    }
}
