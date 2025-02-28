package com.example.omdb.ui.theme.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omdb.data.MoviesRepository
import com.example.omdb.di.CoroutineContextProvider
import com.example.omdb.model.Result
import com.example.omdb.model.MovieSearchParams
import com.example.omdb.model.MovieSearchResult
import com.example.omdb.model.MovieType
import com.example.omdb.model.SearchUiState
import com.example.omdb.network.NetworkObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MovieUiState {
    data class Success(val result: MovieSearchResult) : MovieUiState
    data class Error(val message: String) : MovieUiState
}

@OptIn(FlowPreview::class)
@HiltViewModel
open class MovieSearchViewModel @Inject constructor(
    private val moviesRepository: MoviesRepository,
    private val networkObserver: NetworkObserver,
    private val dispatchers: CoroutineContextProvider
) : ViewModel() {

    private val _movieUiState = MutableStateFlow<MovieUiState>(
        MovieUiState.Success(
            MovieSearchResult(
                listOf(), 0, null
            )
        )
    )
    open val movieUiState: StateFlow<MovieUiState> = _movieUiState.asStateFlow()

    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    val isConnected = networkObserver
        .isConnected
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            false
        )


    private val take = 10
    private var getMoviesJob: Job? = null

    init {
        viewModelScope.launch(dispatchers.io) {
            searchState.debounce(300L).distinctUntilChanged().collect {
                if (getMoviesJob?.isActive == true) {
                    getMoviesJob?.cancel()
                }
                searchMovies(
                    title = it.title,
                    year = it.year,
                    movieType = it.movieType
                )
            }
        }
    }

    fun searchMovies(title: String, year: String? = null, movieType: MovieType? = null) {
        if (title.isBlank() || getMoviesJob?.isActive == true) return

        if (!networkObserver.hasConnection) {
            handleNoConnection()
            return
        }

        val currentParams = getCurrentSearchParams()

        if (paramsChanged(currentParams, title, year, movieType)) {
            resetSearchState(title, year, movieType)
        }

        if (shouldReturnIfAllResultsFetched()) return

        getMoviesJob = viewModelScope.launch(dispatchers.io) {
            val page = getNextPageForSearch()
            val result = fetchMovies(title, year, page, movieType)
            handleSearchResult(result, title, year, movieType)
        }
    }

    private fun handleNoConnection() {
        val currentState = _movieUiState.value
        if (currentState is MovieUiState.Success && currentState.result.movies.isEmpty()) {
            _movieUiState.update {
                MovieUiState.Error("No network connection!")
            }
        }
    }

    private fun getCurrentSearchParams(): MovieSearchParams? {
        return (_movieUiState.value as? MovieUiState.Success)?.result?.searchParams
    }

    private fun paramsChanged(
        currentParams: MovieSearchParams?,
        title: String,
        year: String?,
        movieType: MovieType?
    ): Boolean {
        return currentParams == null || currentParams.title != title || currentParams.year != year || currentParams.movieType != movieType
    }

    private fun resetSearchState(title: String, year: String?, movieType: MovieType?) {
        _movieUiState.update {
            MovieUiState.Success(
                MovieSearchResult(
                    movies = emptyList(),
                    totalResults = 0,
                    searchParams = MovieSearchParams(title, year, movieType)
                )
            )
        }
    }

    private fun shouldReturnIfAllResultsFetched(): Boolean {
        val currentResults = (_movieUiState.value as? MovieUiState.Success)?.result
        return currentResults?.movies?.size == currentResults?.totalResults && currentResults?.totalResults != 0
    }

    private fun getNextPageForSearch(): Int? {
        val currentResults = (_movieUiState.value as? MovieUiState.Success)?.result
        val currentItemCount = currentResults?.movies?.size ?: 0
        val totalResults = currentResults?.totalResults ?: 0
        return getNextPage(currentItemCount, totalResults)
    }

    private suspend fun fetchMovies(
        title: String,
        year: String?,
        page: Int?,
        movieType: MovieType?
    ): Result<MovieSearchResult> {
        return moviesRepository.getMovies(title, year, page, movieType?.value)
    }

    private fun handleSearchResult(
        result: Result<MovieSearchResult>,
        title: String,
        year: String?,
        movieType: MovieType?
    ) {
        when (result) {
            is Result.Success -> {
                _movieUiState.update { current ->
                    val oldMovies =
                        (current as? MovieUiState.Success)?.result?.movies ?: emptyList()
                    val newMovies = oldMovies.toMutableList().apply { addAll(result.data.movies) }
                    MovieUiState.Success(
                        MovieSearchResult(
                            movies = newMovies,
                            totalResults = result.data.totalResults,
                            searchParams = MovieSearchParams(title, year, movieType)
                        )
                    )
                }
            }

            is Result.Failure -> {
                _movieUiState.update { MovieUiState.Error(result.error) }
            }
        }
    }

    private fun getNextPage(currentItemCount: Int, totalCount: Int): Int? {
        val currentPage = (currentItemCount / take) + 1
        return if (currentItemCount < totalCount) currentPage else null // Return null if no more pages
    }

    fun onTitleChange(title: String) {
        _searchState.update {
            it.copy(title = title)
        }
    }

    fun onMovieType(movieType: MovieType?) {
        _searchState.update {
            it.copy(movieType = movieType)
        }
    }

    fun onYearChange(year: String) {
        _searchState.update {
            it.copy(year = year)
        }
    }

    fun reset() {
        _movieUiState.update {
            MovieUiState.Success(
                MovieSearchResult(
                    listOf(), 0
                )
            )
        }
        _searchState.update {
            SearchUiState()
        }
    }
}