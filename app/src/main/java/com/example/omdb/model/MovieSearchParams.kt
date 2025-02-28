package com.example.omdb.model

import kotlinx.serialization.Serializable

@Serializable
data class MovieSearchParams(
    val title: String,
    val year: String? = null,
    val movieType: MovieType? = null
)
