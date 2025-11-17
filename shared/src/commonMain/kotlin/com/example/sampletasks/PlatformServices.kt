package com.example.sampletasks

import com.example.sampletasks.audio.CameraController
import com.example.sampletasks.audio.NoiseMeter
import com.example.sampletasks.audio.RecordingManager
import com.example.sampletasks.data.TaskRepository
import com.example.sampletasks.audio.AudioPlayer

interface PlatformServices {
    val audioPlayer: AudioPlayer
    val recordingManager: RecordingManager
    val noiseMeter: NoiseMeter
    val cameraController: CameraController
    val taskRepository: TaskRepository
}
