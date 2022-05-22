package com.example.u_assistant

import com.example.u_assistant.models.RasaModel
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val RASA_URL: String = "https://241a-111-68-97-201.ngrok.io/model/parse"

class Api {

    private val json = Json {
        isLenient = true
    }

    private val ktor = HttpClient() {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun getModel(text: String): RasaModel {
        return ktor.post(RASA_URL) { setBody(json.encodeToString(RasaRequest(text))) }.body()
    }

    @kotlinx.serialization.Serializable
    private class RasaRequest(@SerialName("text") val text: String)

}