package com.example.u_assistant.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RasaIntentRanking(
    @SerialName("confidence")
    val confidence: Double,
    @SerialName("name")
    val name: String
)