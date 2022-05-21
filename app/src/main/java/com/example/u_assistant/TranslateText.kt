package com.example.u_assistant

import android.util.Log
import com.google.cloud.translate.v3.LocationName
import com.google.cloud.translate.v3.TranslateTextRequest
import com.google.cloud.translate.v3.TranslationServiceClient
import java.io.IOException
private const val TAG = "TranslateText"


class TranslateText {
    // Set and pass variables to overloaded translateText() method for translation.

    // Translate text to target language.
    @Throws(IOException::class)
    fun translateText(text: String?) {
        val projectId = "u-assistant-344318"
        val targetLanguage = "en"


    }
}