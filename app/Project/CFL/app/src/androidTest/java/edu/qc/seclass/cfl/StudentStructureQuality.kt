package edu.qc.seclass.cfl

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.qc.seclass.cfl.data.DataManager
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.Test
import java.util.UUID

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class StudentStructureQuality {

    @get:Rule
    val composeTestRuling = createAndroidComposeRule<MainActivityHub>()

    @Test
    fun testStoresLoad() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val dataManager = DataManager(context)
        val allstores = dataManager.getStores()

        composeTestRuling.onNodeWithText("Student").performClick()

        for (store in allstores) {
            composeTestRuling.onNodeWithText(store.name)
                .performScrollTo()
                .assertIsDisplayed()
        }
    }

    @Test
    fun testcase_CategoryDietFilter_Viewing(){
        composeTestRuling.onNodeWithText("Student").performClick()

        composeTestRuling.onNodeWithText("Vegan").performClick()
        composeTestRuling.onNodeWithText("Search").performClick()

        composeTestRuling.onNodeWithText("Garden Salad").performClick().assertIsDisplayed()
        composeTestRuling.onNodeWithText("Iced Coffee").assertDoesNotExist()

    }

    @Test
    fun testcase_categoryfilter_view(){
        composeTestRuling.onNodeWithText("Student").performClick()
        composeTestRuling.onNodeWithText("Burgers").performClick()

        composeTestRuling.onNodeWithText("Search").performClick()
        composeTestRuling.onNodeWithText("Burger").performClick().assertIsDisplayed()

    }

    @Test
    fun testsearchfilter(){
        composeTestRuling.onNodeWithText("Student").performClick()
        composeTestRuling.onNodeWithText("Search food or stores...").performTextInput("Student Union")

        composeTestRuling.onNodeWithText("Student Union").assertIsDisplayed()

    }

    @Test
    fun testsearchingfoodfilter(){
        composeTestRuling.onNodeWithText("Student").performClick()
        composeTestRuling.onNodeWithText("Search food or stores...").performTextInput("Burger")

        composeTestRuling.onNodeWithText("Burger").assertIsDisplayed()
    }

    @Test
    fun testAddReview_showsInList() {

        val uniqueReviewText = "Test review content ${UUID.randomUUID().toString().take(5)}"


        composeTestRuling.onNodeWithText("Student")
            .performClick()

        composeTestRuling.onNodeWithText("Student Union")
            .performScrollTo()
            .performClick()

        composeTestRuling.onNodeWithText("Your name").performScrollTo().performTextInput("Jake")

        composeTestRuling.onNodeWithText("Comment")
            .performScrollTo()
            .performTextInput(uniqueReviewText)


        composeTestRuling.onNodeWithText("Rating (1-5)")
            .performTextInput("5")


        composeTestRuling.onNodeWithText("Submit Review")
            .performScrollTo()
            .performClick()


        composeTestRuling.waitForIdle()


        composeTestRuling.onNodeWithText(uniqueReviewText)
            .performScrollTo()
            .assertIsDisplayed()
    }

}