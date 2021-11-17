package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest: AutoCloseKoinTest() {

    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var application: Application
    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(application, fakeDataSource)
    }

    @Test
    fun saveEmptyReminder_displayTitleError() {
        val reminder = ReminderDataItem(null, null, null, null, null, "1")
        saveReminderViewModel.validateAndSaveReminder(reminder)
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }


    @Test
    fun saveReminderWithTitleAndNoLocation_displayLocationError() {
        val reminder = ReminderDataItem("Only Title", null, null, null, null, "1")
        saveReminderViewModel.validateAndSaveReminder(reminder)
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }

    @Test
    fun saveReminderWithTitleAndLocation_displaySuccess() {
        val title = "Feed ducks"
        val location = "Kurpark Weiher"
        val latitude = 50.08449855863385
        val longitude = 8.249763215269093
        val reminder = ReminderDataItem(title, null, location, latitude, longitude, "1")
        saveReminderViewModel.validateAndSaveReminder(reminder)
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(),
            `is`(application.resources.getString(R.string.reminder_saved)))
        assertThat(saveReminderViewModel.addGeofenceEvent.getOrAwaitValue(),
            `is`(SaveReminderViewModel.Location(title, latitude, longitude)))
    }

    @Test
    fun selectPointOfInterest_setSelectedLocation() {
        val location = "Bootsverleih am Kursee"
        val latitude = 50.08495200109355
        val longitude = 8.248832360418813
        val poi = PointOfInterest(LatLng(latitude, longitude), "123", location)
        saveReminderViewModel.selectedPOI.value = poi
        assertThat(saveReminderViewModel.selectedLocation.getOrAwaitValue(),
            `is`(SaveReminderViewModel.Location(location, latitude, longitude)))
    }
}