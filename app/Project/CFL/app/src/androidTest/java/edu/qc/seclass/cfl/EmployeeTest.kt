package edu.qc.seclass.cfl

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmployeeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivityHub>()

    @Test
    fun testEmployeeLogin_correctpassword(){
        composeTestRule.onNodeWithText("Employee login").performClick()

        composeTestRule.onNodeWithText("Password").performTextInput("1111")

        Espresso.closeSoftKeyboard()

        composeTestRule.onNodeWithText("Login").performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Employee Portal").assertIsDisplayed()
    }

    @Test
    fun test_wrongpassword(){
        composeTestRule.onNodeWithText("Employee login").performClick()

        composeTestRule.onNodeWithText("Password").performTextInput("8319")

        Espresso.closeSoftKeyboard()

        composeTestRule.onNodeWithText("Login").performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Invalid password, please try again.").assertIsDisplayed()

    }

    @Test
    fun testrestaurantsappear(){
        composeTestRule.onNodeWithText("Employee login").performClick()

        composeTestRule.onNodeWithText("Password").performTextInput("1111")

        Espresso.closeSoftKeyboard()

        composeTestRule.onNodeWithText("Login").performClick()

        composeTestRule.onNodeWithText("Student Union").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Current Menu Items").assertIsDisplayed()
    }

    @Test
    fun addingmenuitems(){
        composeTestRule.onNodeWithText("Employee login").performClick()

        composeTestRule.onNodeWithText("Password").performTextInput("1111")

        Espresso.closeSoftKeyboard()

        composeTestRule.onNodeWithText("Login").performClick()

        composeTestRule.onNodeWithText("Student Union").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Menu item name").performScrollTo()
            .performTextInput("Cheese Pizza")

        // Dietary Tags
        composeTestRule.onNodeWithText("Dietary tags (comma separated)")
            .performScrollTo()
            .performTextInput("Vegetarian, Dairy")

        // Stock Quantity
        composeTestRule.onNodeWithText("Initial stock quantity")
            .performScrollTo()
            .performTextInput("50")

        // Description
        composeTestRule.onNodeWithText("Description")
            .performScrollTo()
            .performTextInput("Classic cheese pizza with tomato sauce.")

        // Price
        composeTestRule.onNodeWithText("Price (e.g. 4.99)")
            .performScrollTo()
            .performTextInput("4.99")

        // Category
        composeTestRule.onNodeWithText("Category (e.g. Drink, Snack)")
            .performScrollTo()
            .performTextInput("Dinner")

        // 5. Submit
        // Close keyboard one last time to ensure the floating button/bottom button is visible
        Espresso.closeSoftKeyboard()

        composeTestRule.onNodeWithText("Add Item")
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasText("Cheese Pizza"))

        composeTestRule.onNodeWithText("Cheese Pizza").assertExists()

    }

    @Test
    fun testingquantity_deleteitems(){
        composeTestRule.onNodeWithText("Employee login").performClick()

        composeTestRule.onNodeWithText("Password").performTextInput("1111")

        Espresso.closeSoftKeyboard()

        composeTestRule.onNodeWithText("Login").performClick()

        composeTestRule.onNodeWithText("Student Union").performClick()
        composeTestRule.onNodeWithText("Burger").performClick()
        composeTestRule.onNodeWithText("Delete Item").performClick()

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Burger").assertDoesNotExist()


    }


}