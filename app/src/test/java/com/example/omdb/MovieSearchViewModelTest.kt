package com.example.omdb

import com.example.omdb.data.MoviesRepository
import com.example.omdb.model.Result
import com.example.omdb.model.MovieSearchResult
import com.example.omdb.network.NetworkObserver
import com.example.omdb.ui.theme.screens.MovieUiState
import com.example.omdb.ui.theme.screens.MovieSearchViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.Before
import org.mockito.Mockito
import org.mockito.kotlin.whenever

class MovieSearchViewModelTest {

    private lateinit var movieViewModel: MovieSearchViewModel

    private val fakeMovieRepository = Mockito.mock(MoviesRepository::class.java)

    private val fakeNetworkObserver = Mockito.mock(NetworkObserver::class.java)

    @Before
    fun injectHiltRule() {
        movieViewModel = MovieSearchViewModel(moviesRepository = fakeMovieRepository, networkObserver = fakeNetworkObserver)
    }

    @Test
    fun searchMovieByTitle_noNetwork_error() {
        // Arrange: Setup mock repository behavior
        runBlocking {
            whenever(fakeMovieRepository.getMovies("Batman", null, null, null))
                .thenReturn(Result.Success(MovieSearchResult(emptyList(), 0, null)))
        }

        // Act
        movieViewModel.searchMovies("Batman")

        // Assert
        assert(movieViewModel.movieUiState.value is MovieUiState.Error)
        Assert.assertEquals("No network connection!", (movieViewModel.movieUiState.value as MovieUiState.Error).message)
    }
}