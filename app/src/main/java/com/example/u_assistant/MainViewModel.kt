package com.example.u_assistant

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.u_assistant.models.Resource
import com.example.u_assistant.models.toSuccess
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.speech.v1.SpeechClient
import com.google.cloud.speech.v1.SpeechSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {

    private val _speechClient = MutableStateFlow<Resource<SpeechClient>>(Resource.Loading)
    val speechClient = _speechClient.asStateFlow()

    suspend fun init(context: Context) = withContext(Dispatchers.Default) {
        _speechClient.value = context.assets.open("credentials.json").use {
            SpeechClient.create(
                SpeechSettings.newBuilder().setCredentialsProvider {
                    GoogleCredentials.fromStream(it)
                }.build()
            )
        }.toSuccess()
    }

}