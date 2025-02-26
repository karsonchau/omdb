package com.example.omdb.network

import com.example.omdb.model.MovieSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MoviesApiService {
    @GET("/")
    suspend fun getMovies(@Query("s") title: String,
                          @Query("y") year: String?,
                          @Query("page") page: Int?,
                          @Query("type") type: String?): MovieSearchResponse
}