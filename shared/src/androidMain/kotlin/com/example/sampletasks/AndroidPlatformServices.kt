package com.example.sampletasks

import android.content.Context
import com.example.sampletasks.audio.AndroidAudioPlayer
import com.example.sampletasks.audio.AndroidCameraController
import com.example.sampletasks.audio.AndroidNoiseMeter
import com.example.sampletasks.audio.AndroidRecordingManager
import com.example.sampletasks.audio.AudioPlayer
import com.example.sampletasks.audio.CameraController
import com.example.sampletasks.audio.NoiseMeter
import com.example.sampletasks.audio.RecordingManager
import com.example.sampletasks.data.DatabaseDriverFactory
import com.example.sampletasks.data.SqlDelightTaskRepository
import com.example.sampletasks.data.TaskRepository

class AndroidPlatformServices(context: Context) : PlatformServices {
    override val audioPlayer: AudioPlayer = AndroidAudioPlayer(context)
    override val recordingManager: RecordingManager = AndroidRecordingManager(context)
    override val noiseMeter: NoiseMeter = AndroidNoiseMeter()
    override val cameraController: CameraController = AndroidCameraController(context)
    override val taskRepository: TaskRepository = SqlDelightTaskRepository(DatabaseDriverFactory(context))
}
