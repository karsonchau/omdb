package com.example.omdb.ui.theme.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.omdb.R
import com.example.omdb.model.Movie
import com.example.omdb.model.MovieSearchResult
import com.example.omdb.model.MovieType
import com.example.omdb.ui.theme.OMDbTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@Composable
fun MovieSearchScreen(viewModel: MovieSearchViewModel = hiltViewModel(),
                      snackbarHostState: SnackbarHostState,
                      coroutineScope: CoroutineScope,
                      contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val uiState by viewModel.movieUiState.collectAsStateWithLifecycle()
    val hasNetwork by viewModel.isConnected.collectAsStateWithLifecycle()
    val searchUiState by viewModel.searchState.collectAsStateWithLifecycle()
    var initialCheck by remember { mutableStateOf(true) }

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
            value = searchUiState.title,
            onValueChange = {
                viewModel.onTitleChange(title = it)
            },
            placeholder = {
                Text(text = stringResource(id = R.string.title))
            },
            modifier = Modifier
                .fillMaxWidth(),
            keyboardActions = KeyboardActions.Default,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.Center) {
            YearDropdown(selectedYear = searchUiState.year, onYearSelected = { updatedYear ->
                viewModel.onYearChange(year = updatedYear)
            })
            Spacer(modifier = Modifier.width(10.dp))
            MovieTypes(movieType = searchUiState.movieType,
                onMovieTypeChange = { updatedMovieType ->
                    viewModel.onMovieType(movieType = updatedMovieType)
            })
        }
        Button(onClick = {
            viewModel.reset()
        }) {
            Text(text = "Reset")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (uiState is MovieUiState.Success) {
            MovieList((uiState as MovieUiState.Success).result,
                onLoadMore = {
                    viewModel.searchMovies(
                        title = searchUiState.title,
                        year = searchUiState.year,
                        movieType = searchUiState.movieType)
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
    years.add(0, LocalContext.current.getString(R.string.any_year))

    val dropdownWidth = 150.dp

    Box {
        OutlinedButton(onClick = { expanded = true },
            modifier = Modifier.width(dropdownWidth)) {
            Text(text = (if (selectedYear.isNullOrEmpty()) stringResource(id = R.string.any_year) else selectedYear)) // Default to empty
            Icon(Icons.Default.ArrowDropDown, contentDescription = stringResource(id = R.string.dropdown_arrow))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(dropdownWidth)
                .heightIn(max = 300.dp)
        ) {
            years.forEachIndexed { index, year ->
                DropdownMenuItem(
                    text = { Text(year) },
                    trailingIcon = {
                        if (year == selectedYear || (index == 0 && selectedYear == null)) {
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.semantics {
            collectionInfo = CollectionInfo(rowCount = movies.size, columnCount =  1)
        }) {
        items(movies.size, key = { index -> movies[index].imdbID }) { index ->
            val movie = movies[index]
            MovieItem(movie = movie, index = index)
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
fun MovieItem(movie: Movie, index: Int) {
    val context = LocalContext.current
    val type = movie.type.value.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()}
    val yearContentDesc = context.getString(R.string.year_type_description, movie.year, type)

    Card(elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)) {
        Row(
            modifier = Modifier
                .semantics(mergeDescendants = true) {
                    collectionItemInfo = CollectionItemInfo(
                        rowIndex = index,
                        rowSpan = 1,
                        columnIndex = 0,
                        columnSpan = 1
                    )
                }
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .padding(16.dp)
        ) {
            val imagePainter: Painter =
                if (movie.posterUrl.isEmpty() || movie.posterUrl == "N/A")
                    painterResource(id = R.drawable.na_image) else
                    rememberAsyncImagePainter(model = movie.posterUrl)
            Image(
                painter = imagePainter,
                contentDescription = stringResource(id = R.string.movie_poster, movie.title),
                modifier = Modifier.size(height = 150.dp, width = 100.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(text = movie.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(id = R.string.year_type_format,
                    movie.year,
                    type),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.semantics {
                        contentDescription = yearContentDesc
                    })
                Spacer(modifier = Modifier.weight(1f))
                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {},
                        modifier = Modifier.clearAndSetSemantics { }
                    ) {
                        Text(stringResource(id = R.string.click))
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun MovieItemPreview() {
    val movie = Movie(
        title = "Avengers: Endgame",
        year = "2019",
        imdbID = "tt4154796",
        type = MovieType.MOVIE,
        posterUrl = "https://m.media-amazon.com/images/M/MV5BMTc5MDE2ODcwNV5BMl5BanBnXkFtZTgwMzI2NzQ2NzM@._V1_SX300.jpg"
    )
    OMDbTheme {
        MovieItem(movie = movie, index = 0)
    }
}