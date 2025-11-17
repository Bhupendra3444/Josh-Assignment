package com.example.sampletasks

import com.example.sampletasks.audio.AudioPlayer
import com.example.sampletasks.audio.CameraController
import com.example.sampletasks.audio.DesktopAudioPlayer
import com.example.sampletasks.audio.DesktopCameraController
import com.example.sampletasks.audio.DesktopNoiseMeter
import com.example.sampletasks.audio.DesktopRecordingManager
import com.example.sampletasks.audio.NoiseMeter
import com.example.sampletasks.audio.RecordingManager
import com.example.sampletasks.data.DatabaseDriverFactory
import com.example.sampletasks.data.SqlDelightTaskRepository
import com.example.sampletasks.data.TaskRepository

class DesktopPlatformServices : PlatformServices {
    override val audioPlayer: AudioPlayer = DesktopAudioPlayer()
    override val recordingManager: RecordingManager = DesktopRecordingManager()
    override val noiseMeter: NoiseMeter = DesktopNoiseMeter()
    override val cameraController: CameraController = DesktopCameraController()
    override val taskRepository: TaskRepository = SqlDelightTaskRepository(DatabaseDriverFactory())
}
