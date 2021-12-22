package com.udacity.project4

import android.app.Application
import android.os.Build
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed

import androidx.test.espresso.matcher.RootMatchers.withDecorView


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    KoinTest {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    @get:Rule
    var activityTestRule = ActivityTestRule(RemindersActivity::class.java)


    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            viewModel {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Test
    fun tryAddOneReminderWithoutTitle_displayError() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        // GIVEN: Empty reminder list
        onView(withId(R.id.noDataTextView)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        // WHEN: Create reminder with location, title and description but not saving it
        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(longClick())
        onView(withId(R.id.saveLocation)).perform(click())
        onView(withId(R.id.selectedLocation)).check(matches(CoreMatchers.not(withText(""))))

        onView(withId(R.id.reminderTitle)).perform(typeText("Title"))
        onView(withId(R.id.reminderDescription)).perform(typeText("Description"))
        Espresso.closeSoftKeyboard()

        Espresso.pressBack()

        // THEN: show empty list
        onView(withId(R.id.noDataTextView)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        activityScenario.close()
    }

    @Test
    fun addOneReminderWithoutSaving_showEmptyList() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        // GIVEN: Empty reminder list
        onView(withId(R.id.noDataTextView)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        // WHEN: Add reminder with location, title and description
        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(longClick())
        onView(withId(R.id.saveLocation)).perform(click())

        onView(withId(R.id.selectedLocation)).check(matches(CoreMatchers.not(withText(""))))

        onView(withId(R.id.saveReminder)).perform(click())

        // THEN: show error
        onView(withText(R.string.err_enter_title)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        activityScenario.close()
    }

    @Test
    fun addOneReminder_displayInUI() {
        val saveReminderViewModel: SaveReminderViewModel = get()

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        // GIVEN: Empty reminder list
        onView(withId(R.id.noDataTextView)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        // WHEN: Create reminder without title
        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(longClick())
        onView(withId(R.id.saveLocation)).perform(click())

        val selectedLocation = saveReminderViewModel.selectedLocation.value
        checkNotNull(selectedLocation)
        onView(withId(R.id.selectedLocation)).check(matches(withText(selectedLocation.name)))

        onView(withId(R.id.reminderTitle)).perform(typeText("Title"))
        onView(withId(R.id.reminderDescription)).perform(typeText("Description"))
        Espresso.closeSoftKeyboard()

        onView(withId(R.id.saveReminder)).perform(click())

        // Testing Toast failed with android 11 and higher -> https://github.com/android/android-test/issues/803
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            onView(withText(R.string.reminder_saved))
                .inRoot(withDecorView(not(activityTestRule.activity.window.decorView)))
                .check(matches(isDisplayed()))
        }

        // THEN: show reminder
        onView(withText("Title")).check(matches(isDisplayed()))
        onView(withText("Description")).check(matches(isDisplayed()))
        onView(withText(selectedLocation.name)).check(matches(isDisplayed()))

        activityScenario.close()
    }
}
