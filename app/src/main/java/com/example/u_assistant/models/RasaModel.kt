package com.example.u_assistant.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RasaModel(
    @SerialName("entities")
    val entities: List<RasaEntity>,
    @SerialName("intent")
    val intent: RasaIntent,
    @SerialName("intent_ranking")
    val intentRanking: List<RasaIntentRanking>,
    @SerialName("text")
    val text: String,
    @SerialName("text_tokens")
    val textTokens: List<List<Int>>
)