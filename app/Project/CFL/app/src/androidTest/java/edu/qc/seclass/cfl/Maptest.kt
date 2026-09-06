package edu.qc.seclass.cfl

import android.content.Intent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.intent.Intents
import org.junit.Rule
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.Matchers.hasToString
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Maptest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivityHub>()

    @Before
    fun settingup(){
        Intents.init()
    }

    @After
    fun tearingdown(){
        Intents.release()
    }

    @Test
    fun testingwalkingdirectionsgrab(){
        val expectedIntent = allOf(
            hasAction(Intent.ACTION_VIEW),
            toPackage("com.google.android.apps.maps")
        )
        intending(expectedIntent).respondWith(android.app.Instrumentation.ActivityResult(0, null))
        //composeTestRule.onRoot().printToLog("TAG")
        composeTestRule.onNodeWithText("Student").performClick()
        composeTestRule.onNodeWithText("Student Union").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Get Walking Directions").performScrollTo().performClick()

        intended(allOf(
            hasAction(Intent.ACTION_VIEW),
            toPackage("com.google.android.apps.maps"),
            hasData(hasToString(startsWith("google.navigation")))

        ))
    }

}