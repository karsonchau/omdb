package com.example.omdb.ui.theme.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.omdb.R
import com.example.omdb.model.Movie
import com.example.omdb.model.MovieSearchResult
import com.example.omdb.model.MovieType
import com.example.omdb.ui.theme.DarkEpisodePurple
import com.example.omdb.ui.theme.DarkGameOrange
import com.example.omdb.ui.theme.DarkMovieBlue
import com.example.omdb.ui.theme.DarkSeriesGreen
import com.example.omdb.ui.theme.LightEpisodePurple
import com.example.omdb.ui.theme.LightGameOrange
import com.example.omdb.ui.theme.LightMovieBlue
import com.example.omdb.ui.theme.LightSeriesGreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun MovieSearchScreen(viewModel: MovieSearchViewModel = hiltViewModel(),
                      snackbarHostState: SnackbarHostState,
                      coroutineScope: CoroutineScope,
                      contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val uiState by viewModel.movieUiState.collectAsStateWithLifecycle()
    val hasNetwork by viewModel.isConnected.collectAsStateWithLifecycle()

    var searchTerm by remember { mutableStateOf(TextFieldValue()) }
    var year by remember {
        mutableStateOf<String?>(null)
    }
    var movieType by remember {
        mutableStateOf<MovieType?>(null)
    }
    var initialCheck by remember { mutableStateOf(true) }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(uiState) {
        if (uiState is MovieUiState.Error) {
            val errorMessage = (uiState as MovieUiState.Error).message
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }
    val context = LocalContext.current
    LaunchedEffect(hasNetwork) {
        if (initialCheck) {
            initialCheck = false
            return@LaunchedEffect
        }
        if (!hasNetwork) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.no_network_connection),
                    duration = SnackbarDuration.Short
                )
            }
        }
    }
    
    Column(modifier = Modifier
        .padding(contentPadding)
        .fillMaxSize()
        .padding(16.dp)) {
        OutlinedTextField(
            value = searchTerm,
            onValueChange = {
                searchTerm = it
                if (it.text.isEmpty()) {
                    viewModel.reset()
                }
            },
            placeholder = {
                Text(text = stringResource(id = R.string.title))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            keyboardActions = KeyboardActions.Default
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            YearDropdown(selectedYear = year, onYearSelected = { updatedYear ->
                year = updatedYear
            })
            MovieTypes(movieType = movieType,
                onMovieTypeChange = { updatedMovieType ->
                    movieType = updatedMovieType
            })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = {
                keyboardController?.hide()
                viewModel.searchMovies(
                    title = searchTerm.text,
                year = year,
                movieType = movieType) }) {
                Text(text = "Search")
            }
            Button(onClick = {
                searchTerm = TextFieldValue()
                year = null
                viewModel.reset()
            }) {
                Text(text = "Reset")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (uiState is MovieUiState.Success) {
            MovieList((uiState as MovieUiState.Success).result,
                onLoadMore = {
                    viewModel.searchMovies(
                        title = searchTerm.text,
                        year = year,
                        movieType = movieType)
            })
        }
    }
}

@Composable
fun MovieTypes(movieType: MovieType?, onMovieTypeChange: (MovieType?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = mapOf(
        "" to stringResource(id = R.string.all),
        MovieType.MOVIE.value to stringResource(id = R.string.movie),
        MovieType.EPISODE.value to stringResource(id = R.string.episode),
        MovieType.SERIES.value to stringResource(id = R.string.series),
        MovieType.GAME.value to stringResource(id = R.string.game)
    )
    val dropdownWidth = 150.dp

    val selectedOption = if (movieType == null) options[""] else options[movieType.value]

    Box {
        OutlinedButton(onClick = { expanded = true },
            modifier = Modifier.width(dropdownWidth)) {
            Text(text = selectedOption ?: "")
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = stringResource(id = R.string.dropdown_arrow)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(dropdownWidth)
        ) {
            options.forEach { (key, value) ->
                DropdownMenuItem(
                    text = { Text(value) },
                    trailingIcon = {
                        if (value == selectedOption) {
                            Icon(Icons.Default.Check,
                                contentDescription = stringResource(id = R.string.selected))
                        }
                    },
                    onClick = {
                        onMovieTypeChange(MovieType.entries.find { it.value.equals(key, ignoreCase = true) })
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun YearDropdown(
    selectedYear: String?,
    onYearSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (1950..currentYear).map { it.toString() }.reversed().toMutableList()
    years.add(0, "")

    val dropdownWidth = 150.dp

    Box {
        OutlinedButton(onClick = { expanded = true },
            modifier = Modifier.width(dropdownWidth)) {
            Text(text = (if (selectedYear.isNullOrEmpty()) stringResource(id = R.string.select_year) else selectedYear)) // Default to empty
            Icon(Icons.Default.ArrowDropDown, contentDescription = stringResource(id = R.string.dropdown_arrow))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(dropdownWidth)
        ) {
            years.forEach { year ->
                DropdownMenuItem(
                    text = { Text(year) },
                    trailingIcon = {
                        if (year == selectedYear) {
                            Icon(Icons.Default.Check,
                                contentDescription = stringResource(id = R.string.selected))
                        }
                    },
                    onClick = {
                        onYearSelected(year)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MovieList(result: MovieSearchResult, onLoadMore: () -> Unit) {
    val listState = rememberLazyListState()
    val movies = result.movies
    val totalCount = result.totalResults
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(movies, key = { movie -> movie.imdbID }) { movie ->
            MovieItem(movie = movie)
        }
        if (movies.size != totalCount && totalCount != 0) {
            item {
                CircularProgressIndicator(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .wrapContentWidth(Alignment.CenterHorizontally))
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= result.movies.size - 5 ) {
                    onLoadMore()
                }
            }
    }
}

@Composable
fun MovieItem(movie: Movie) {
    val backgroundColor = when (movie.type) {
        MovieType.MOVIE -> if (isSystemInDarkTheme()) DarkMovieBlue else LightMovieBlue
        MovieType.SERIES -> if (isSystemInDarkTheme()) DarkSeriesGreen else LightSeriesGreen
        MovieType.EPISODE -> if (isSystemInDarkTheme()) DarkEpisodePurple else LightEpisodePurple
        MovieType.GAME -> if (isSystemInDarkTheme()) DarkGameOrange else LightGameOrange
    }
    Card(colors = CardDefaults.cardColors(containerColor = backgroundColor)) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)) {
            val imagePainter: Painter = if (movie.posterUrl.isEmpty() || movie.posterUrl == "N/A") painterResource(id = R.drawable.na_image) else
                rememberAsyncImagePainter(model = movie.posterUrl)
            Image(
                painter = imagePainter,
                contentDescription = stringResource(id = R.string.movie_poster),
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.FillHeight
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = movie.title, style = MaterialTheme.typography.titleMedium)
                Text(text = stringResource(id = R.string.year, movie.year), 
                    style = MaterialTheme.typography.bodyMedium)
            }
            Button(onClick = {}) {
                Text(stringResource(id = R.string.click))
            }
        }
    }

}