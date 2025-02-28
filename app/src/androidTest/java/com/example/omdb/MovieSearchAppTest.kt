package com.example.omdb

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule

@HiltAndroidTest
class MovieSearchAppTest {

    @get:Rule(order = 1)
    var hiltTestRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    var composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltTestRule.inject()

    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.omdb", appContext.packageName)
    }

    @Test
    fun hasTitle() {
        composeTestRule.onNodeWithText("OMDb").assertExists()
    }

    @Test
    fun enterMovieTitle_updatesSearchField() {
        composeTestRule.onNode(hasText("Title")).performTextInput("Batman")
        composeTestRule.onNode(hasText("Batman")).assertExists()
    }
}