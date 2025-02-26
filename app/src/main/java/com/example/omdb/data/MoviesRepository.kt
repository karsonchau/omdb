package com.example.omdb.data

import com.example.omdb.model.ApiResult
import com.example.omdb.model.MovieSearchResult
import com.example.omdb.network.MoviesApiService

interface MoviesRepository {
    suspend fun getMovies(title: String, year: String? = null, page: Int? = null, movieType: String? = null): ApiResult
}

class NetworkMoviesRepository(private val service: MoviesApiService): MoviesRepository {
    override suspend fun getMovies(title: String, year: String?, page: Int?, movieType: String?): ApiResult {
        return try {
            val response = service.getMovies(title, year, page, movieType)

            if (response.response == "True") {
                ApiResult.Success(MovieSearchResult(movies = response.movies ?: listOf(),
                    totalResults = response.totalResults?.toIntOrNull() ?: 0
                    ))
            } else {
                ApiResult.Error(response.error ?: "Unknown error")
            }
        } catch (e: Exception) {
            // log error
            print(e.message)
            ApiResult.Error("Network error")
        }
    }
}