package com.example.omdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Movie(
    @SerialName("Title") val title: String,
    @SerialName("Year") val year: String,
    @SerialName("imdbID") val imdbID: String,
    @SerialName("Type") val type: MovieType,
    @SerialName("Poster") val posterUrl: String
)

@Serializable
enum class MovieType(val value: String) {
    @SerialName("movie")
    MOVIE("movie"),

    @SerialName("series")
    SERIES("series"),

    @SerialName("episode")
    EPISODE("episode"),

    @SerialName("game")
    GAME("game")
}

