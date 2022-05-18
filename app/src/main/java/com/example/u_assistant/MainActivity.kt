package com.example.u_assistant

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.api.gax.core.CredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.speech.v1.*
import java.io.File
import java.io.InputStream
import java.util.*


class MainActivity : AppCompatActivity() {
    private val SPEECH_REQUEST_CODE = 0
    lateinit var speechClient: SpeechClient
    private var recorder: MediaRecorder? = null

    //@RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputStream: InputStream = assets.open("credentials.json")
        val size: Int = inputStream.available()
        Toast.makeText(this, size.toString(), Toast.LENGTH_LONG).show()

        Log.d("before sp", "LET see")
//       //val credentials:CredentialsProvider = CredentialsProvider().credentials
        val button: Button = findViewById(R.id.button)
        val txv: TextView = findViewById(R.id.textv)


//        button.setOnClickListener {
//            // Code here executes on main thread after user presses button
//            cl(inputStream)
//        }
        speechClient = com.google.cloud.speech.v1.SpeechClient.create(
            SpeechSettings.newBuilder().setCredentialsProvider(CredentialsProvider {
                GoogleCredentials.fromStream(inputStream)
            }).build()
        )

        //Log.d("SPR",SpeechRecognizer.isOnDeviceRecognitionAvailable(this).toString())
//        Log.d("SPR2",SpeechRecognizer.isRecognitionAvailable(this).toString())

        val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
//        button.setOnClickListener {
//                recognizeSpeech(txv,speechRecognizer,speechRecognizerIntent)
//        }


        Log.d("RI3IS", SpeechRecognizer.isRecognitionAvailable(this).toString())
        button.setOnTouchListener { v, event ->
            if (event != null) {
                when (event.getAction()) {
                    MotionEvent.ACTION_UP -> {

                        if (ActivityCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.RECORD_AUDIO
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {

                            ActivityCompat.requestPermissions(
                                this@MainActivity,
                                arrayOf(Manifest.permission.RECORD_AUDIO),
                                101
                            )

                        } else {


                            recorder = MediaRecorder().apply {
                                setAudioSource(MediaRecorder.AudioSource.MIC)
                                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                                setAudioChannels(1)
                                setOutputFile("${cacheDir.absolutePath}/audio.3gp")
                                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                                prepare()
                                start()
                            }
                        }
                    }
                    MotionEvent.ACTION_DOWN -> {
                        recorder?.apply {
                            stop()
                            release()
                        }
                        recorder = null
                        //                            val inputStream: File = File("${cacheDir.absolutePath}/audio.3gp")
                        //                            val size= inputStream.length()/1024
                        //                            Log.d("SIZE",size.toString())
                    }
                }
            }

            false
        }


//        Log.d("AFTER sp","LET see")
//
//        //Toast.makeText(this,speechClient.isShutdown.toString(),Toast.LENGTH_LONG).show()
//
//        // The path to the audio file to transcribe
//        // The path to the audio file to transcribe
//
//
//
//
//        val audioFile: InputStream = assets.open("out.wav")
//        //val file:File = File(gcsUri)
//
//        val byteString:ByteString = ByteString.copyFrom(audioFile.readBytes())
//
//        // Builds the sync recognize request
//
//        // Builds the sync recognize request
//        val config: RecognitionConfig = RecognitionConfig.newBuilder()
//            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
//            .setSampleRateHertz(16000)
//            .setLanguageCode("ur-PK")
//            .build()
//        val audio: RecognitionAudio = RecognitionAudio.newBuilder().setContent(byteString).build()
//
//        // Performs speech recognition on the audio file
//
//        // Performs speech recognition on the audio file
//        val response: RecognizeResponse = speechClient.recognize(config, audio)
//        val results: List<SpeechRecognitionResult> = response.resultsList
//
//        for (result in results){
//            val alternative = result.alternativesList[0]
//            val text = alternative.transcript
//            txv.text = text.toString()
//        }
//        Log.d("ok",results.toString())

    }


// fun cl(inputStream:InputStream){
//     com.google.cloud.speech.v1.SpeechClient.create(
//         SpeechSettings.newBuilder().setCredentialsProvider(CredentialsProvider { GoogleCredentials.fromStream(inputStream) }).build()
//     )
// }


    private fun recognizeSpeech(
        textView: TextView,
        mSpeechRecognizer: SpeechRecognizer,
        mSpeechRecognizerIntent: Intent
    ) {

//    val mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        //val mSpeechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        mSpeechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        mSpeechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "Speak nah"
        )


        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ur-PK")
        mSpeechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(bytes: ByteArray) {}
            override fun onEndOfSpeech() {}
            override fun onError(i: Int) {}
            override fun onResults(bundle: Bundle) {
                //getting all the matches
                val matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                //displaying the first match
                if (matches != null) {
                    textView.text = matches[0]
                } else {
                    textView.text = "no work"
                }
            }

            override fun onPartialResults(bundle: Bundle) {}
            override fun onEvent(i: Int, bundle: Bundle) {}
        })


//    Log.d("RI1",RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE)
//    Log.d("RI",RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS)


    }
}