package com.example.omdb.mock

import com.example.omdb.data.MoviesRepository
import com.example.omdb.model.MovieSearchResult
import com.example.omdb.model.Result

class FakeMovieRepository(private val result: Result<MovieSearchResult>): MoviesRepository {
    override suspend fun getMovies(
        title: String,
        year: String?,
        page: Int?,
        movieType: String?
    ): Result<MovieSearchResult> {
        return result
    }
}