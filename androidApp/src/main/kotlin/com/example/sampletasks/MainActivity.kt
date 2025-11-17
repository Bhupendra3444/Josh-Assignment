package com.example.sampletasks

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.sampletasks.ui.SampleTasksApp

class MainActivity : ComponentActivity() {
    private val requiredPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    PermissionGate(
                        permissions = requiredPermissions,
                        onAllPermissionsGranted = {
                            val services = remember { AndroidPlatformServices(applicationContext) }
                            SampleTasksApp(platformServices = services)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionGate(
    permissions: Array<String>,
    onAllPermissionsGranted: @Composable () -> Unit
) {
    val context = LocalContext.current
    var hasPermissions by remember(context) {
        mutableStateOf(permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        })
    }
    var shouldShowRationale by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.all { it }
        hasPermissions = granted
        if (!granted) {
            shouldShowRationale = true
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            launcher.launch(permissions)
        }
    }

    if (hasPermissions) {
        onAllPermissionsGranted()
    } else {
        PermissionRequestScreen(
            shouldShowRationale = shouldShowRationale,
            onRequest = { launcher.launch(permissions) }
        )
    }
}

@Composable
private fun PermissionRequestScreen(
    shouldShowRationale: Boolean,
    onRequest: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Microphone and camera permissions are required to record and capture tasks.",
            style = MaterialTheme.typography.bodyLarge
        )
        if (shouldShowRationale) {
            Text(
                text = "Please grant permissions in the next prompt.",
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        Button(onClick = onRequest, modifier = Modifier.padding(top = 24.dp)) {
            Text("Grant Permissions")
        }
    }
}
