package com.example.omdb

import com.example.omdb.data.MoviesRepository
import com.example.omdb.di.AppModule
import com.example.omdb.model.ApiResult
import com.example.omdb.model.MovieSearchResult
import com.example.omdb.network.NetworkObserver
import com.example.omdb.ui.theme.screens.MovieUiState
import com.example.omdb.ui.theme.screens.MovieViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Before
import org.junit.Rule
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(AppModule::class)
class MovieViewModelTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var movieViewModel: MovieViewModel

    @Mock
    lateinit var moviesRepository: MoviesRepository

    @Mock
    lateinit var networkObserver: NetworkObserver


    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        hiltRule.inject()
        movieViewModel = MovieViewModel(moviesRepository, networkObserver)
    }

    @Test
    fun testMoviesSearch() {
        // Arrange: Setup mock repository behavior
        runBlocking {
            whenever(moviesRepository.getMovies(any(), any(), any(), any()))
                .thenReturn(ApiResult.Success(MovieSearchResult(emptyList(), 0, null)))
        }

        // Act
        movieViewModel.searchMoviesByTitle("Batman")

        // Assert
        assert(movieViewModel.movieUiState.value is MovieUiState.Success)
    }
}