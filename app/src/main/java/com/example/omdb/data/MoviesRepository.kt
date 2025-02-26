package com.example.omdb.data

import com.example.omdb.model.Result
import com.example.omdb.model.MovieSearchResult
import com.example.omdb.network.MoviesApiService

interface MoviesRepository {
    suspend fun getMovies(title: String, year: String? = null, page: Int? = null, movieType: String? = null): Result<MovieSearchResult>
}

class NetworkMoviesRepository(private val service: MoviesApiService): MoviesRepository {
    override suspend fun getMovies(title: String, year: String?, page: Int?, movieType: String?): Result<MovieSearchResult> {
        return try {
            val response = service.getMovies(title, year, page, movieType)

            if (response.response == "True") {
                Result.Success(MovieSearchResult(movies = response.movies ?: listOf(),
                    totalResults = response.totalResults?.toIntOrNull() ?: 0
                    ))
            } else {
                Result.Failure(response.error ?: "Unknown error")
            }
        } catch (e: Exception) {
            // log error
            print(e.message)
            Result.Failure("Network error")
        }
    }
}