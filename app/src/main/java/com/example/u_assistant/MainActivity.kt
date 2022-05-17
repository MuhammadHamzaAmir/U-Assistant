package com.example.u_assistant

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.api.gax.core.CredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.speech.v1.*
import com.google.protobuf.ByteString
import java.io.File
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    lateinit var speechClient: SpeechClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputStream: InputStream = assets.open("credentials.json")
        val size: Int = inputStream.available()
        Toast.makeText(this,size.toString(),Toast.LENGTH_LONG).show()

        Log.d("before sp","LET see")
//       //val credentials:CredentialsProvider = CredentialsProvider().credentials
        val button:Button= findViewById(R.id.button)
        val txv:TextView= findViewById(R.id.textv)


//        button.setOnClickListener {
//            // Code here executes on main thread after user presses button
//            cl(inputStream)
//        }
        speechClient = com.google.cloud.speech.v1.SpeechClient.create(
            SpeechSettings.newBuilder().setCredentialsProvider(CredentialsProvider { GoogleCredentials.fromStream(inputStream) }).build()
        )

        Log.d("AFTER sp","LET see")

        //Toast.makeText(this,speechClient.isShutdown.toString(),Toast.LENGTH_LONG).show()

        // The path to the audio file to transcribe
        // The path to the audio file to transcribe




        val audioFile: InputStream = assets.open("out.wav")
        //val file:File = File(gcsUri)

        val byteString:ByteString = ByteString.copyFrom(audioFile.readBytes())

        // Builds the sync recognize request

        // Builds the sync recognize request
        val config: RecognitionConfig = RecognitionConfig.newBuilder()
            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
            .setSampleRateHertz(16000)
            .setLanguageCode("ur-PK")
            .build()
        val audio: RecognitionAudio = RecognitionAudio.newBuilder().setContent(byteString).build()

        // Performs speech recognition on the audio file

        // Performs speech recognition on the audio file
        val response: RecognizeResponse = speechClient.recognize(config, audio)
        val results: List<SpeechRecognitionResult> = response.resultsList

        for (result in results){
            val alternative = result.alternativesList[0]
            val text = alternative.transcript
            txv.text = text.toString()
        }
        Log.d("ok",results.toString())

    }
}

// fun cl(inputStream:InputStream){
//     com.google.cloud.speech.v1.SpeechClient.create(
//         SpeechSettings.newBuilder().setCredentialsProvider(CredentialsProvider { GoogleCredentials.fromStream(inputStream) }).build()
//     )
// }