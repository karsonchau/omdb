package com.example.omdb

import app.cash.turbine.test
import com.example.omdb.data.MoviesRepository
import com.example.omdb.di.TestCoroutineContextProvider
import com.example.omdb.model.Movie
import com.example.omdb.model.Result
import com.example.omdb.model.MovieSearchResult
import com.example.omdb.model.MovieType
import com.example.omdb.network.NetworkObserver
import com.example.omdb.ui.theme.screens.MovieUiState
import com.example.omdb.ui.theme.screens.MovieSearchViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.Before
import org.mockito.Mockito

class MovieSearchViewModelTest {
    private lateinit var movieViewModel: MovieSearchViewModel
    private val fakeMovieRepository = Mockito.mock(MoviesRepository::class.java)
    private val fakeNetworkObserver = FakeNetworkObserver()
    @Before
    fun setUp() {
        // Instantiate the ViewModel with the mock
        movieViewModel = MovieSearchViewModel(
            moviesRepository = fakeMovieRepository,
            networkObserver = fakeNetworkObserver,
            dispatchers = TestCoroutineContextProvider()
        )
    }

    @Test
    fun `searchMovieByTitle - no network - error`() = runBlocking {
        // Act: Trigger the movie search
        fakeNetworkObserver.hasConnection = false
        movieViewModel.searchMovies("Batman")
        movieViewModel.movieUiState.test {
            val uiState = awaitItem()
            assert(uiState is MovieUiState.Error)
            val state = uiState as MovieUiState.Error
            Assert.assertEquals("No network connection!", state.message)
        }
    }

    @Test
    fun `searchMovieByTitle - network available - success`() = runBlocking {
        // Set up a mock response for the repository
        val title = "Batman"
        val mockMovies = MovieSearchResult(movies = listOf(Movie(
            title,
            "2022",
            "",
            MovieType.MOVIE,
            "")), totalResults = 1)
        Mockito.`when`(fakeMovieRepository.getMovies(title, null, null, null))
            .thenReturn(Result.Success(mockMovies))

        // Act: Trigger the movie search
        movieViewModel.searchMovies(title)

        // Assert: Verify that the UI state is successful and contains the movie
        movieViewModel.movieUiState.test {
            val uiState = awaitItem()
            assert(uiState is MovieUiState.Success)
            val successState = uiState as MovieUiState.Success
            assert(successState.result.movies.isNotEmpty())
            Assert.assertEquals(title, successState.result.movies.first().title)
        }
    }

    @Test
    fun `searchMovieByTitle - not found - error`() = runBlocking {
        // Set up a mock response for the repository
        val title = "asdfasdf"
        Mockito.`when`(fakeMovieRepository.getMovies(title, null, null, null))
            .thenReturn(Result.Failure("Movie not found!"))

        // Act: Trigger the movie search
        movieViewModel.searchMovies(title)

        // Assert: Verify that the UI state is successful and contains the movie
        movieViewModel.movieUiState.test {
            val uiState = awaitItem()
            assert(uiState is MovieUiState.Error)
            val state = uiState as MovieUiState.Error
            Assert.assertEquals("Movie not found!", state.message)
        }
    }
}

class FakeNetworkObserver: NetworkObserver {
    override var hasConnection: Boolean = true
    private val flow = MutableStateFlow(false)

    override val isConnected: Flow<Boolean>
        get() = flow
}