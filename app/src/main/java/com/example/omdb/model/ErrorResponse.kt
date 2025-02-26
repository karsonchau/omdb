package com.example.omdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    @SerialName("Response") val response: String,
    @SerialName("Error") val error: String
)
