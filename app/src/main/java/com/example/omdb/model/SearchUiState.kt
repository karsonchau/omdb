package com.example.omdb.model

data class SearchUiState(
    val title: String = "",
    val year: String? = null,
    val movieType: MovieType? = null)
