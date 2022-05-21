package com.example.u_assistant.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RasaEntity(
    @SerialName("confidence_entity")
    val confidenceEntity: Double,
    @SerialName("end")
    val end: Int,
    @SerialName("entity")
    val entity: String,
    @SerialName("extractor")
    val extractor: String,
    @SerialName("start")
    val start: Int,
    @SerialName("value")
    val value: String
)