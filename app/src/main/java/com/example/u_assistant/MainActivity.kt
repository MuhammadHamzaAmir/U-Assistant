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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.u_assistant.models.Resource
import com.example.u_assistant.models.getOrThrow
import com.example.u_assistant.models.handle
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

    private lateinit var recorder: MediaRecorder

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
        recorder.apply {
            stop()
            release()
        }

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

@Composable
private fun MainScreen(
    onStartRecord: suspend () -> Unit,
    onStopRecord: suspend () -> String
) {
    MaterialTheme() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val api = remember { Api() }
        var currentText by remember { mutableStateOf("") }
        var isRecording by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                text = currentText,
                color = Color.White,
                style = MaterialTheme.typography.h6.copy(textDirection = TextDirection.Rtl)
            )
            Image(
                modifier = Modifier
                    .size(150.dp)
                    .clickable {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                if (isRecording) {
                                    currentText = onStopRecord()
                                    val model = api.getModel(currentText)
                                    model.intent.handle()(context as Activity, model.entities)
                                } else {
                                    onStartRecord()
                                }
                                isRecording = !isRecording
                            }
                        }
                    }
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(40.dp),
                painter = painterResource(id = R.drawable.ic_mic),
                contentDescription = "Mic",
                colorFilter = ColorFilter.tint(Color.Black)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "بٹن دبانے کے بعد بولین...",
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}