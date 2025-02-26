package com.example.omdb


import com.example.omdb.data.MoviesRepository
import com.example.omdb.model.ApiResult
import com.example.omdb.model.MovieSearchResult
import com.example.omdb.network.NetworkObserver
import com.example.omdb.ui.theme.screens.MovieUiState
import com.example.omdb.ui.theme.screens.MovieViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.Before
import org.mockito.Mockito
import org.mockito.kotlin.whenever


class MovieViewModelTest {

    private lateinit var movieViewModel: MovieViewModel

    private val fakeMovieRepository = Mockito.mock(MoviesRepository::class.java)

    private val fakeNetworkObserver = Mockito.mock(NetworkObserver::class.java)

    @Before
    fun injectHiltRule() {
        movieViewModel = MovieViewModel(moviesRepository = fakeMovieRepository, networkObserver = fakeNetworkObserver)
    }

    @Test
    fun searchMovieByTitle_noNetwork_error() {
        // Arrange: Setup mock repository behavior
        runBlocking {
            whenever(fakeMovieRepository.getMovies("Batman", null, null, null))
                .thenReturn(ApiResult.Success(MovieSearchResult(emptyList(), 0, null)))
        }

        // Act
        movieViewModel.searchMoviesByTitle("Batman")

        // Assert
        assert(movieViewModel.movieUiState.value is MovieUiState.Error)
        Assert.assertEquals("No network connection!", (movieViewModel.movieUiState.value as MovieUiState.Error).message)
    }
}