package com.example.sampletasks.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.sampletasks.DesktopPlatformServices
import com.example.sampletasks.ui.SampleTasksApp

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Sample Tasks") {
        SampleTasksApp(platformServices = DesktopPlatformServices())
    }
}
