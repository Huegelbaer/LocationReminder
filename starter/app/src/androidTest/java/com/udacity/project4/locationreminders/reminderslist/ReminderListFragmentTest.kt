package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest: AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource

    @Before
    fun setup() {
        repository = FakeDataSource()
    }

    @Test
    fun clearData_displayNoData() = runBlockingTest {
        // GIVEN - empty reminders
        repository.deleteAllReminders()

        // WHEN - launch fragment
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // THEN - display no data
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun clickAdd_navigateToAddFragment() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB))
            .perform(click())

        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.
}