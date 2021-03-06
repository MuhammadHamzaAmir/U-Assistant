package com.example.u_assistant

import android.app.Activity
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ExperimentalGraphicsApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.u_assistant.models.Resource
import com.example.u_assistant.models.getOrThrow
import com.example.u_assistant.models.handle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.cloud.speech.v1.RecognitionAudio
import com.google.cloud.speech.v1.RecognitionConfig
import com.google.protobuf.ByteString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

private const val SPEECH_REQUEST_CODE = 101
private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private var recorder: MediaRecorder? = null

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val client = viewModel.speechClient.collectAsState()

            if (client.value is Resource.Loading) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                MainScreen(
                    onStartRecord = this::recordAudio,
                    onStopRecord = {
                        stopAudio().use {
                            ByteString.copyFrom(it.readBytes())
                        }.let { convertToText(it) }
                    }
                )
            }
        }

        lifecycleScope.launch { viewModel.init(this@MainActivity) }
    }


    private fun recordAudio() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AMR_WB)
            setAudioSamplingRate(16000)
            setAudioChannels(1)
            setOutputFile("${cacheDir.absolutePath}/audio.amr")
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)
            prepare()
            start()
        }
    }

    private fun stopAudio(): InputStream {
        recorder?.apply {
            stop()
            release()
        }

        recorder = null
        return File("${cacheDir.absolutePath}/audio.amr").inputStream()
    }

    private fun playRecording() {
        val uri = "${cacheDir.absolutePath}/audio.amr".toUri()
        val mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(applicationContext, uri)
            prepare()
            start()
            Log.d(TAG, "playRecording: $uri started")
        }

        mediaPlayer.setOnCompletionListener {
            Log.d(TAG, "playRecording: completed")
            mediaPlayer.release()
        }
    }


    private fun convertToText(byteString: ByteString): String {
        Log.d(TAG, "convertToText: Sending Request")
        val config = RecognitionConfig.newBuilder()
            .setEncoding(RecognitionConfig.AudioEncoding.AMR_WB)
            .setSampleRateHertz(16000)
            .setLanguageCode("ur-PK")
            .build()

        val audio = RecognitionAudio.newBuilder().setContent(byteString).build()

        // Performs speech recognition on the audio file
        val response = viewModel.speechClient.value.getOrThrow().recognize(config, audio)
        val results = response.resultsList

        Log.d(TAG, "convertToText: $results")
        return results.joinToString("\n\n") { it.alternativesList.first().transcript }
    }

}

@Preview(showBackground = true, heightDp = 640, widthDp = 360)
@Composable
private fun MainScreenPreview() {
    MainScreen(onStartRecord = {}, onStopRecord = { "" })
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalGraphicsApi::class)
@Composable
private fun MainScreen(
    onStartRecord: suspend () -> Unit,
    onStopRecord: suspend () -> String
) {
    MaterialTheme() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val api = remember { Api() }
        val microphonePermission = rememberMultiplePermissionsState(
            permissions = listOf(android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.WRITE_CONTACTS)
        )

        var currentText by remember { mutableStateOf("?????? ???? ???? ???????? ?????? ???? ???????? ????????") }
        var isRecording by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }

        val microphoneBgColor by animateColorAsState(targetValue = if (isRecording) Color.Red else Color.White)
        val microphoneColor by animateColorAsState(targetValue = if (isRecording) Color.White else Color.hsl(250F,0.5F,0.4F))
        val screenBgColor by animateColorAsState(targetValue = if (isRecording) Color.White else  Color.hsl(250F,0.5F,0.4F))




        if (microphonePermission.allPermissionsGranted) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = screenBgColor)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {
                    Spacer(modifier = Modifier.padding(top=140.dp))
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        text = currentText,
                        color = if (isRecording) Color.Black else Color.White,
                        style = MaterialTheme.typography.h5.copy(textDirection = TextDirection.Rtl),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.padding(top=30.dp))
                    Image(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .clickable {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        isRecording = !isRecording
                                        if (!isRecording) {
                                            isLoading = true
                                            currentText = onStopRecord()
                                            try {
                                                val model = api.getModel(currentText)
                                                model.intent.handle()(
                                                    context as Activity,
                                                    model.entities
                                                )
                                            } catch (e: Exception) {
                                                Log.e(TAG, "MainScreen: ", e)
                                            }
                                            isLoading = false
                                        } else {
                                            onStartRecord()
                                        }
                                    }
                                }
                            }
                            .background(color = microphoneBgColor)
                            .padding(40.dp),
                        painter = painterResource(id = R.drawable.ic_mic),
                        contentDescription = "Mic",
                        colorFilter = ColorFilter.tint(microphoneColor)
                    )
                    Spacer(modifier = Modifier.height(25.dp))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "?????? ?????????? ???? ?????? ??????????",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.h6.copy(textDirection = TextDirection.Rtl)
                    )
                    Spacer(modifier = Modifier.height(95.dp))
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

        } else {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black)
                .padding(16.dp)) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (microphonePermission.shouldShowRationale) {
                        // If the user has denied the permission but the rationale can be shown,
                        // then gently explain why the app requires this permission
                        PermissionText(text = "The microphone is important for this app. Please grant the permission.")
                    } else {
                        // If it's the first time the user lands on this feature, or the user
                        // doesn't want to be asked again for this permission, explain that the
                        // permission is required
                        PermissionText(text = "Microphone permission required for this feature to be available.")
                        Spacer(modifier = Modifier.height(8.dp))
                        PermissionText(
                            text = "Please grant the permission",
                            style = MaterialTheme.typography.subtitle1
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { microphonePermission.launchMultiplePermissionRequest() }) {
                        Text("Request permission")
                    }
                }
            }
        }

    }
}

@Composable
private fun PermissionText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = MaterialTheme.typography.h6
) {
    Text(
        modifier = modifier.fillMaxWidth(),
        text = text,
        style = style,
        color = Color.White,
        textAlign = TextAlign.Center
    )
}
