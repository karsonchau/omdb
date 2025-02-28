package com.example.omdb

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.omdb.di.AppModule
import com.example.omdb.di.CoroutineContextProvider
import com.example.omdb.mock.FakeMovieRepository
import com.example.omdb.mock.FakeNetworkObserver
import com.example.omdb.model.Movie
import com.example.omdb.model.MovieSearchResult
import com.example.omdb.model.Result
import com.example.omdb.model.MovieType
import com.example.omdb.ui.theme.screens.MovieSearchScreen
import com.example.omdb.ui.theme.screens.MovieSearchViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar

@HiltAndroidTest
@UninstallModules(AppModule::class)
class MovieSearchScreenTests {
    @get:Rule(order = 1)
    var hiltTestRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        hiltTestRule.inject()
    }

    private val successResult = Result.Success(
        data =
        MovieSearchResult(
            movies = listOf(
                Movie(
                    title = "The Batman",
                    year = "2022",
                    imdbID = "tt1877830",
                    posterUrl = "N/A",
                    type = MovieType.MOVIE
                )
            ),
            totalResults = 1
        )
    )

    @Test
    fun yearDropDown_selectCurrentYear_yearSelected() {
        val fakeMovieRepository = FakeMovieRepository(successResult)
        val fakeNetworkObserver = FakeNetworkObserver()
        fakeNetworkObserver.hasConnection = false
        val fakeViewModel = MovieSearchViewModel(
            fakeMovieRepository,
            fakeNetworkObserver,
            dispatchers = CoroutineContextProvider.Default()
        )
        val snackbarHostState = SnackbarHostState()
        composeTestRule.setContent {
            MovieSearchScreen(
                viewModel = fakeViewModel,
                snackbarHostState = snackbarHostState,
                coroutineScope = rememberCoroutineScope()
            )
        }

        composeTestRule.onNode(hasText("Any year")).performClick()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
        composeTestRule.onNode(hasText(currentYear)).performClick()
        composeTestRule.onNode(hasText(currentYear)).assertExists()
    }

    @Test
    fun typeDropDown_openTypesDropDown_allTypesDisplayed() {
        val fakeMovieRepository = FakeMovieRepository(successResult)
        val fakeNetworkObserver = FakeNetworkObserver()
        fakeNetworkObserver.hasConnection = false
        val fakeViewModel = MovieSearchViewModel(
            fakeMovieRepository,
            fakeNetworkObserver,
            dispatchers = CoroutineContextProvider.Default()
        )
        val snackbarHostState = SnackbarHostState()
        composeTestRule.setContent {
            MovieSearchScreen(
                viewModel = fakeViewModel,
                snackbarHostState = snackbarHostState,
                coroutineScope = rememberCoroutineScope()
            )
        }

        composeTestRule.onNode(hasText("All")).performClick()
        composeTestRule.onNode(hasText("Movie")).assertExists()
        composeTestRule.onNode(hasText("Episode")).assertExists()
        composeTestRule.onNode(hasText("Game")).assertExists()
        composeTestRule.onNode(hasText("Series")).assertExists()
    }

    @Test
    fun typeDropDown_selectMovie_movieSelected() {
        val fakeMovieRepository = FakeMovieRepository(successResult)
        val fakeNetworkObserver = FakeNetworkObserver()
        fakeNetworkObserver.hasConnection = false
        val fakeViewModel = MovieSearchViewModel(
            fakeMovieRepository,
            fakeNetworkObserver,
            dispatchers = CoroutineContextProvider.Default()
        )
        val snackbarHostState = SnackbarHostState()
        composeTestRule.setContent {
            MovieSearchScreen(
                viewModel = fakeViewModel,
                snackbarHostState = snackbarHostState,
                coroutineScope = rememberCoroutineScope()
            )
        }

        composeTestRule.onNode(hasText("All")).performClick()
        composeTestRule.onNode(hasText("Movie")).performClick()
        composeTestRule.onNode(hasText("Movie")).assertExists()
    }

    @Test
    fun enterMovieTitle_noNetwork_noNetworkMessage() {
        val fakeMovieRepository = FakeMovieRepository(successResult)
        val fakeNetworkObserver = FakeNetworkObserver()
        fakeNetworkObserver.hasConnection = false
        val fakeViewModel = MovieSearchViewModel(
            fakeMovieRepository,
            fakeNetworkObserver,
            dispatchers = CoroutineContextProvider.Default()
        )
        val snackbarHostState = SnackbarHostState()
        composeTestRule.setContent {
            MovieSearchScreen(
                viewModel = fakeViewModel,
                snackbarHostState = snackbarHostState,
                coroutineScope = rememberCoroutineScope()
            )
        }
        composeTestRule.onNode(hasText("Title")).performTextInput("B")
        composeTestRule.waitUntil(timeoutMillis = 400) {
            snackbarHostState.currentSnackbarData?.visuals?.message?.isNotEmpty() == true
        }
        Assert.assertEquals(
            "No network connection!",
            snackbarHostState.currentSnackbarData?.visuals?.message
        )
    }

    @Test
    fun enterMovieTitle_failure_errorMessage() {
        val fakeMovieRepository = FakeMovieRepository(Result.Failure("Error"))
        val fakeNetworkObserver = FakeNetworkObserver()
        fakeNetworkObserver.hasConnection = true
        val fakeViewModel = MovieSearchViewModel(
            fakeMovieRepository,
            fakeNetworkObserver,
            dispatchers = CoroutineContextProvider.Default()
        )
        val snackbarHostState = SnackbarHostState()
        composeTestRule.setContent {
            MovieSearchScreen(
                viewModel = fakeViewModel,
                snackbarHostState = snackbarHostState,
                coroutineScope = rememberCoroutineScope()
            )
        }
        composeTestRule.onNode(hasText("Title")).performTextInput("B")
        composeTestRule.waitUntil(timeoutMillis = 400) {
            snackbarHostState.currentSnackbarData?.visuals?.message?.isNotEmpty() == true
        }
        Assert.assertEquals(
            "Error",
            snackbarHostState.currentSnackbarData?.visuals?.message
        )
    }

    @Test
    fun enterMovieTitle_success_movieCard() {
        val fakeMovieRepository = FakeMovieRepository(successResult)
        val fakeNetworkObserver = FakeNetworkObserver()
        fakeNetworkObserver.hasConnection = true
        val fakeViewModel = MovieSearchViewModel(
            fakeMovieRepository,
            fakeNetworkObserver,
            dispatchers = CoroutineContextProvider.Default()
        )
        val snackbarHostState = SnackbarHostState()
        composeTestRule.setContent {
            MovieSearchScreen(
                viewModel = fakeViewModel,
                snackbarHostState = snackbarHostState,
                coroutineScope = rememberCoroutineScope()
            )
        }
        composeTestRule.onNode(hasText("Title")).performTextInput("Batman")
        composeTestRule.waitUntil(timeoutMillis = 500) {
            composeTestRule.onAllNodes(hasText("The Batman")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNode(hasText("Batman")).assertExists()
        composeTestRule.onNode(hasText("The Batman")).assertExists()
        composeTestRule.onNode(hasText("2022 | Movie")).assertExists()
        composeTestRule.onNode(hasContentDescription("The Batman poster")).assertExists()
    }
}