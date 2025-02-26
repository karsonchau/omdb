package com.example.omdb.model

import kotlinx.serialization.Serializable

@Serializable
data class MovieSearchResult(
    val movies: List<Movie>,
    val totalResults: Int,
    val searchParams: MovieSearchParams? = null
)