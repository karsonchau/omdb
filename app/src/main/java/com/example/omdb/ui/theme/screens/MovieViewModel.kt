package com.example.omdb.ui.theme.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.omdb.MoviesApplication
import com.example.omdb.data.MoviesRepository
import com.example.omdb.model.ApiResult
import com.example.omdb.model.MovieSearchParams
import com.example.omdb.model.MovieSearchResult
import com.example.omdb.model.MovieType
import com.example.omdb.network.NetworkObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface MovieUiState {
    data class Success(val result: MovieSearchResult) : MovieUiState
    data class Error(val message: String) : MovieUiState
}

class MovieViewModel(private val moviesRepository: MoviesRepository, private val networkObserver: NetworkObserver): ViewModel() {
    private val _movieUiState = MutableStateFlow<MovieUiState>(MovieUiState.Success(MovieSearchResult(
        listOf(), 0, null
    )))
    val movieUiState: StateFlow<MovieUiState> = _movieUiState.asStateFlow()

    val isConnected = networkObserver
        .isConnected
        .stateIn(viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            false)

    private val take = 10
    private var isLoading = false

    fun searchMoviesByTitle(title: String, year: String? = null, movieType: MovieType?) {
        if (title.isBlank() || isLoading) return

        if (!isConnected.value) {
            _movieUiState.update {
                MovieUiState.Error("No network connection!")
            }
            return
        }

        val currentParams = (_movieUiState.value as? MovieUiState.Success)?.result?.searchParams

        if (currentParams != null && (currentParams.title != title ||
                    currentParams.year != year ||
                    currentParams.movieType != movieType)) {
            _movieUiState.update { MovieUiState.Success(
                MovieSearchResult(movies = emptyList(),
                    totalResults = 0,
                    searchParams = MovieSearchParams(
                        title = title,
                        year = year,
                        movieType = movieType)
                )) }
        }

        // Check if we already have all the results
        if (_movieUiState.value is MovieUiState.Success) {
            val currentResults = (_movieUiState.value as MovieUiState.Success).result
            if (currentResults.movies.size ==
                currentResults.totalResults && currentResults.totalResults != 0) {
                return
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            var page: Int? = null
            if (_movieUiState.value is MovieUiState.Success) {
                val result = _movieUiState.value as MovieUiState.Success
                page = getNextPage(currentItemCount = result.result.movies.size,
                    totalCount = result.result.totalResults)
            }
            val result = moviesRepository.getMovies(
                title = title,
                year = year,
                page = page,
                movieType = movieType?.value)
            if (result is ApiResult.Success) {
                _movieUiState.update { current ->
                    val oldMovies = if (current is MovieUiState.Success) current.result.movies else emptyList()
                    val newMovies = oldMovies.toMutableList().apply { addAll(result.data.movies) }
                    MovieUiState.Success(MovieSearchResult(
                        movies = newMovies,
                        totalResults = result.data.totalResults,
                        searchParams = MovieSearchParams(
                            title = title,
                            year = year,
                            movieType = movieType)))
                }
            } else if (result is ApiResult.Error) {
                _movieUiState.update {
                    MovieUiState.Error(result.message)
                }
            }
            isLoading = false
        }
    }

    private fun getNextPage(currentItemCount: Int, totalCount: Int): Int? {
        val currentPage = (currentItemCount / take) + 1
        return if (currentItemCount < totalCount) currentPage else null // Return null if no more pages
    }

    fun reset() {
        _movieUiState.update {
            MovieUiState.Success(MovieSearchResult(
                listOf(), 0
            ))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MoviesApplication)
                val moviesRepository = application.container.moviesRepository
                val networkObserver = application.container.networkObserver
                MovieViewModel(
                    moviesRepository = moviesRepository,
                    networkObserver = networkObserver)
            }
        }
    }
}