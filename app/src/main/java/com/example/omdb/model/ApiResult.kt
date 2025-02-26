package com.example.omdb.model

sealed class ApiResult {
    data class Success(val data: MovieSearchResult) : ApiResult()
    data class Error(val message: String) : ApiResult()
}