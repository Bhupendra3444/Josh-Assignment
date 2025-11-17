package com.example.sampletasks.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import io.kamel.image.lazyPainterResource
import com.example.sampletasks.PlatformServices
import com.example.sampletasks.audio.AudioPlayer
import com.example.sampletasks.audio.CameraController
import com.example.sampletasks.audio.RecordingManager
import com.example.sampletasks.audio.RecordingResult
import com.example.sampletasks.audio.RecordingState
import com.example.sampletasks.audio.PlaybackState
import com.example.sampletasks.model.NoiseTestResult
import com.example.sampletasks.model.ProductSnippet
import com.example.sampletasks.model.RecordingValidator
import com.example.sampletasks.model.TaskDraft
import com.example.sampletasks.model.TaskRecord
import com.example.sampletasks.model.TaskType
import com.example.sampletasks.network.DummyProductsApi
import com.example.sampletasks.network.platformHttpClientEngine
import kotlinx.coroutines.launch

@Composable
fun SampleTasksApp(platformServices: PlatformServices) {
    val repository = platformServices.taskRepository
    val tasks by repository.observeTasks().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val api = remember { DummyProductsApi(platformHttpClientEngine()) }
    var snippets by remember { mutableStateOf<List<ProductSnippet>>(emptyList()) }
    var isNoiseRunning by remember { mutableStateOf(false) }
    val audioPlayer = platformServices.audioPlayer
    DisposableEffect(audioPlayer) {
        onDispose {
            audioPlayer.stop()
            audioPlayer.release()
        }
    }
    LaunchedEffect(Unit) {
        runCatching { api.fetchSnippets() }
            .onSuccess { snippets = it }
    }

    var screen: Screen by remember { mutableStateOf(Screen.Start) }
    var lastNoiseResult by remember { mutableStateOf<NoiseTestResult?>(null) }

    val navigateToHistory = { screen = Screen.History }
    val backStackEnabled = screen !is Screen.Start

    Scaffold(
        topBar = {
            SampleTopBar(
                showBack = backStackEnabled && screen !is Screen.History,
                onNavigateBack = {
                    screen = when (screen) {
                        is Screen.NoiseTest -> Screen.Start
                        is Screen.TaskSelection -> Screen.NoiseTest
                        is Screen.TextReading -> Screen.TaskSelection
                        is Screen.ImageDescription -> Screen.TaskSelection
                        is Screen.PhotoCapture -> Screen.TaskSelection
                        is Screen.History -> Screen.TaskSelection
                        else -> Screen.Start
                    }
                },
                onHistoryClick = navigateToHistory
            )
        }
    ) { padding ->
        Surface(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val current = screen) {
                Screen.Start -> StartScreen(onStart = { screen = Screen.NoiseTest })
                Screen.NoiseTest -> NoiseTestScreen(
                    result = lastNoiseResult,
                    isRunning = isNoiseRunning,
                    onRunTest = {
                        scope.launch {
                            isNoiseRunning = true
                            try {
                                val result = platformServices.noiseMeter.runNoiseTest()
                                lastNoiseResult = result
                                if (result.isPass) {
                                    screen = Screen.TaskSelection
                                }
                            } finally {
                                isNoiseRunning = false
                            }
                        }
                    }
                )
                Screen.TaskSelection -> TaskSelectionScreen(
                    onTextReading = {
                        val snippet = snippets.randomOrNull()
                        screen = Screen.TextReading(snippet)
                    },
                    onImageDescription = {
                        val snippet = snippets.randomOrNull()
                        screen = Screen.ImageDescription(snippet)
                    },
                    onPhotoCapture = {
                        screen = Screen.PhotoCapture
                    }
                )
                is Screen.TextReading -> TextReadingScreen(
                    snippet = current.snippet,
                    recordingManager = platformServices.recordingManager,
                    audioPlayer = audioPlayer,
                    onSubmit = { draft ->
                        scope.launch {
                            repository.addTask(draft)
                            screen = Screen.TaskSelection
                        }
                    }
                )
                is Screen.ImageDescription -> ImageDescriptionScreen(
                    snippet = current.snippet,
                    recordingManager = platformServices.recordingManager,
                    audioPlayer = audioPlayer,
                    onSubmit = { draft ->
                        scope.launch {
                            repository.addTask(draft)
                            screen = Screen.TaskSelection
                        }
                    }
                )
                Screen.PhotoCapture -> PhotoCaptureScreen(
                    cameraController = platformServices.cameraController,
                    recordingManager = platformServices.recordingManager,
                    audioPlayer = audioPlayer,
                    onSubmit = { draft ->
                        scope.launch {
                            repository.addTask(draft)
                            screen = Screen.TaskSelection
                        }
                    }
                )
                Screen.History -> TaskHistoryScreen(tasks = tasks, audioPlayer = audioPlayer)
            }
        }
    }
}

private sealed interface Screen {
    object Start : Screen
    object NoiseTest : Screen
    object TaskSelection : Screen
    object PhotoCapture : Screen
    object History : Screen
    data class TextReading(val snippet: ProductSnippet?) : Screen
    data class ImageDescription(val snippet: ProductSnippet?) : Screen
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SampleTopBar(showBack: Boolean, onNavigateBack: () -> Unit, onHistoryClick: () -> Unit) {
    TopAppBar(
        title = { Text("Sample Tasks") },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            IconButton(onClick = onHistoryClick) {
                Icon(Icons.Default.List, contentDescription = "Task history")
            }
        }
    )
}

@Composable
private fun StartScreen(onStart: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(24.dp)
    ) {
        Text(
            text = "Let's start with a Sample Task for practice.",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text("Pehele hum ek sample task karte hain.")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onStart) {
            Text("Start Sample Task")
        }
    }
}

@Composable
private fun NoiseTestScreen(
    result: NoiseTestResult?,
    isRunning: Boolean,
    onRunTest: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Noise Test", style = MaterialTheme.typography.headlineSmall)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Decibel Meter")
                Text(text = result?.averageDb?.let { "$it dB" } ?: "--")
                LinearProgressIndicator(
                    progress = { ((result?.averageDb ?: 0).coerceIn(0, 60)) / 60f },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(result?.advice ?: "Tap Start to measure the room noise.")
            }
        }
        Button(onClick = onRunTest, enabled = !isRunning) {
            Text("Start Test")
        }
    }
}

@Composable
private fun TaskSelectionScreen(
    onTextReading: () -> Unit,
    onImageDescription: () -> Unit,
    onPhotoCapture: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Pick a task", style = MaterialTheme.typography.headlineSmall)
        TaskCard("Text Reading Task", onTextReading)
        TaskCard("Image Description Task", onImageDescription)
        TaskCard("Photo Capture Task", onPhotoCapture)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskCard(label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(label, modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun TextReadingScreen(
    snippet: ProductSnippet?,
    recordingManager: RecordingManager,
    audioPlayer: AudioPlayer,
    onSubmit: (TaskDraft) -> Unit
) {
    var checkbox1 by remember { mutableStateOf(false) }
    var checkbox2 by remember { mutableStateOf(false) }
    var checkbox3 by remember { mutableStateOf(false) }
    var recordingResult by remember { mutableStateOf<RecordingResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose { audioPlayer.stop() }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Text Reading Task", style = MaterialTheme.typography.titleLarge)
        Text(snippet?.description ?: "Mega long lasting fragrance...")
        Text("Read the passage aloud in your native language.")

        RecordingSection(
            taskType = TaskType.TEXT_READING,
            recordingManager = recordingManager,
            onRecordingCaptured = {
                recordingResult = it
                error = null
            },
            onValidationError = { error = it }
        )

        recordingResult?.let {
            PlaybackCard(it, audioPlayer)
            Text("Captured ${it.durationSec}s")
            CheckboxRow("No background noise", checkbox1) { checkbox1 = it }
            CheckboxRow("No mistakes while reading", checkbox2) { checkbox2 = it }
            CheckboxRow("Beech me koi galti nahi hai", checkbox3) { checkbox3 = it }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = {
                    recordingResult = null
                    checkbox1 = false
                    checkbox2 = false
                    checkbox3 = false
                }) {
                    Text("Record again")
                }
                Button(
                    onClick = {
                        recordingResult?.let { record ->
                            onSubmit(
                                TaskDraft(
                                    taskType = TaskType.TEXT_READING,
                                    text = snippet?.description,
                                    audioPath = record.filePath,
                                    durationSec = record.durationSec
                                )
                            )
                        }
                    },
                    enabled = checkbox1 && checkbox2 && checkbox3
                ) {
                    Text("Submit")
                }
            }
        }

        error?.let {
            Text(it, color = Color.Red)
        }
    }
}

@Composable
private fun ImageDescriptionScreen(
    snippet: ProductSnippet?,
    recordingManager: RecordingManager,
    audioPlayer: AudioPlayer,
    onSubmit: (TaskDraft) -> Unit
) {
    var recordingResult by remember { mutableStateOf<RecordingResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose { audioPlayer.stop() }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Image Description Task", style = MaterialTheme.typography.titleLarge)
        snippet?.let {
            Text(it.title, fontWeight = FontWeight.Bold)
            Text(it.description, maxLines = 4, overflow = TextOverflow.Ellipsis)
            ImagePlaceholder(it.thumbnail)
        }
        Text("Describe what you see in your native language.")

        RecordingSection(
            taskType = TaskType.IMAGE_DESCRIPTION,
            recordingManager = recordingManager,
            onRecordingCaptured = {
                recordingResult = it
                error = null
            },
            onValidationError = { error = it }
        )

        recordingResult?.let { PlaybackCard(it, audioPlayer) }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = { recordingResult = null }) { Text("Record again") }
            Button(
                onClick = {
                    recordingResult?.let { record ->
                        onSubmit(
                            TaskDraft(
                                taskType = TaskType.IMAGE_DESCRIPTION,
                                imageUrl = snippet?.thumbnail,
                                audioPath = record.filePath,
                                durationSec = record.durationSec
                            )
                        )
                    }
                },
                enabled = recordingResult != null
            ) {
                Text("Submit")
            }
        }

        error?.let { Text(it, color = Color.Red) }
    }
}

@Composable
private fun PhotoCaptureScreen(
    cameraController: CameraController,
    recordingManager: RecordingManager,
    audioPlayer: AudioPlayer,
    onSubmit: (TaskDraft) -> Unit
) {
    val scope = rememberCoroutineScope()
    var photoPath by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    var recordingResult by remember { mutableStateOf<RecordingResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose { audioPlayer.stop() }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Photo Capture Task", style = MaterialTheme.typography.titleLarge)
        if (photoPath == null) {
            Button(onClick = {
                scope.launch {
                    photoPath = cameraController.capturePhoto()
                }
            }) {
                Text("Capture Image")
            }
        } else {
            Text("Photo saved at: $photoPath", style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = {
                scope.launch {
                    photoPath = cameraController.capturePhoto()
                }
            }) {
                Text("Retake Photo")
            }
        }
        Text("Describe the photo in your language.")
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Your description") },
            minLines = 2
        )

        RecordingSection(
            taskType = TaskType.PHOTO_CAPTURE,
            recordingManager = recordingManager,
            onRecordingCaptured = {
                recordingResult = it
                error = null
            },
            onValidationError = { error = it }
        )

        recordingResult?.let { PlaybackCard(it, audioPlayer) }

        Button(
            onClick = {
                if (photoPath != null && recordingResult != null) {
                    onSubmit(
                        TaskDraft(
                            taskType = TaskType.PHOTO_CAPTURE,
                            imagePath = photoPath,
                            audioPath = recordingResult!!.filePath,
                            durationSec = recordingResult!!.durationSec,
                            metadata = description
                        )
                    )
                } else {
                    error = "Capture photo and audio first"
                }
            },
            enabled = photoPath != null && recordingResult != null
        ) {
            Text("Submit")
        }
        error?.let { Text(it, color = Color.Red) }
    }
}

@Composable
private fun ImagePlaceholder(url: String) {
    KamelImage(
        resource = lazyPainterResource(data = url),
        contentDescription = "Task reference image",
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentScale = ContentScale.Crop,
        onLoading = {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading preview...", style = MaterialTheme.typography.bodySmall)
            }
        },
        onFailure = { _ ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Preview unavailable\n$url",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    )
}

@Composable
private fun RecordingSection(
    taskType: TaskType,
    recordingManager: RecordingManager,
    onRecordingCaptured: (RecordingResult) -> Unit,
    onValidationError: (String) -> Unit
) {
    val state by recordingManager.state.collectAsState()

    val modifier = Modifier
        .fillMaxWidth()
        .height(120.dp)
        .background(MaterialTheme.colorScheme.primaryContainer)
        .pointerInput(taskType) {
            detectTapGestures(
                onPress = {
                    try {
                        recordingManager.start(taskType)
                    } catch (t: Throwable) {
                        onValidationError(t.message ?: "Unable to start recording")
                        return@detectTapGestures
                    }
                    val released = tryAwaitRelease()
                    if (released) {
                        val result = try {
                            recordingManager.stop()
                        } catch (t: Throwable) {
                            onValidationError(t.message ?: "Unable to stop recording")
                            return@detectTapGestures
                        }
                        val validation = RecordingValidator.validate(result.durationSec)
                        if (validation.isValid) {
                            onRecordingCaptured(result)
                        } else {
                            onValidationError(validation.message ?: "Invalid recording")
                        }
                    } else {
                        try {
                            recordingManager.cancel()
                        } catch (_: Throwable) {
                        }
                    }
                }
            )
        }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when (val s = state) {
            RecordingState.Idle -> Text("Press & hold to record")
            is RecordingState.Recording -> Text("Recording... ${s.elapsedSec}s")
            is RecordingState.Ready -> Text("Captured ${s.durationSec}s")
            is RecordingState.Error -> Text(s.message, color = Color.Red)
        }
    }
}

@Composable
private fun CheckboxRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onChecked)
        Text(label)
    }
}

@Composable
private fun PlaybackCard(result: RecordingResult, audioPlayer: AudioPlayer) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Playback", fontWeight = FontWeight.Bold)
            Text("Path: ${result.filePath}", style = MaterialTheme.typography.bodySmall)
            Text("Duration: ${result.durationSec}s")
            PlaybackControls(
                audioPlayer = audioPlayer,
                filePath = result.filePath,
                durationSec = result.durationSec
            )
        }
    }
}

@Composable
private fun PlaybackControls(
    audioPlayer: AudioPlayer,
    filePath: String,
    durationSec: Int
) {
    val playbackState by audioPlayer.state.collectAsState()
    val state = playbackState
    val isCurrent = when (state) {
        is PlaybackState.Playing -> state.source == filePath
        is PlaybackState.Paused -> state.source == filePath
        is PlaybackState.Error -> state.source == filePath
        else -> false
    }
    val progress = (state as? PlaybackState.Playing)?.takeIf { it.source == filePath }?.progress ?: 0f
    val isPlaying = (state as? PlaybackState.Playing)?.source == filePath
    val isPaused = (state as? PlaybackState.Paused)?.source == filePath
    val errorMessage = (state as? PlaybackState.Error)?.takeIf { it.source == filePath }?.message

    LinearProgressIndicator(
        progress = { if (isCurrent) progress else 0f },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = { audioPlayer.play(filePath) }) {
            Text(if (isPaused) "Resume" else "Play")
        }
        TextButton(onClick = { audioPlayer.pause() }, enabled = isPlaying) {
            Text("Pause")
        }
        TextButton(onClick = { audioPlayer.stop() }, enabled = isPlaying || isPaused) {
            Text("Stop")
        }
    }
    Text(
        text = if (isCurrent) "Status: ${if (isPlaying) "Playing" else if (isPaused) "Paused" else "Idle"}" else "Tap play to preview",
        style = MaterialTheme.typography.bodySmall
    )
    errorMessage?.let {
        Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun TaskHistoryScreen(tasks: List<TaskRecord>, audioPlayer: AudioPlayer) {
    val totalDuration = tasks.sumOf { it.durationSec }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Tasks: ${tasks.size}")
            Text("Duration: ${totalDuration}s")
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tasks) { task ->
                Card {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Task #${task.id} • ${task.taskType}", fontWeight = FontWeight.Bold)
                        task.text?.let { Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        task.imageUrl?.let { Text("Image URL: $it", style = MaterialTheme.typography.bodySmall) }
                        task.imagePath?.let { Text("Image Path: $it", style = MaterialTheme.typography.bodySmall) }
                        task.metadata?.let { Text("Notes: $it", style = MaterialTheme.typography.bodySmall) }
                        Text("Duration: ${task.durationSec}s • ${task.timestamp}")
                        task.audioPath?.let {
                            Spacer(Modifier.height(8.dp))
                            PlaybackControls(
                                audioPlayer = audioPlayer,
                                filePath = it,
                                durationSec = task.durationSec
                            )
                        }
                    }
                }
            }
        }
    }
}
