package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun saveReminderAndGetById() = runBlockingTest {
        val reminder = ReminderDTO("Title", null, "Location", 50.0, 8.0, "GetById")
        repository.saveReminder(reminder)

        val result = repository.getReminder("GetById")
        assertThat(result is Result.Success<ReminderDTO>, `is`(true))
        val loaded = (result as Result.Success<ReminderDTO>).data
        assertThat(loaded.title, `is`("Title"))
        assertThat(loaded.description, CoreMatchers.nullValue())
        assertThat(loaded.location, `is`("Location"))
        assertThat(loaded.latitude, `is`(50.0))
        assertThat(loaded.longitude, `is`(8.0))
    }

    @Test
    fun updateReminderAndGetById() = runBlockingTest {
        val reminder = ReminderDTO("Title", null, "Location", 50.0, 8.0, "Update")
        repository.saveReminder(reminder)

        val newReminder = ReminderDTO("Title", "Description", "Location", 50.0, 8.0, "Update")
        repository.saveReminder(newReminder)

        val result = repository.getReminder("Update")
        assertThat(result is Result.Success<ReminderDTO>, `is`(true))
        val loaded = (result as Result.Success<ReminderDTO>).data
        assertThat(loaded.title, `is`("Title"))
        assertThat(loaded.description, `is`("Description"))
        assertThat(loaded.location, `is`("Location"))
        assertThat(loaded.latitude, `is`(50.0))
        assertThat(loaded.longitude, `is`(8.0))
    }

    @Test
    fun saveRemindersAndClearDatabase() = runBlockingTest {
        val reminder = ReminderDTO("Title", null, "Location", 50.0, 8.0, "1")
        repository.saveReminder(reminder)
        val reminder2 = ReminderDTO("Title 2", null, "Location", 50.0, 8.0, "2")
        repository.saveReminder(reminder2)

        val resultList = repository.getReminders()
        val loadedList = (resultList as Result.Success<List<ReminderDTO>>).data
        assertThat(loadedList, CoreMatchers.notNullValue())
        assertThat(loadedList, CoreMatchers.not(emptyList()))
        assertThat(loadedList.size, `is`(2))

        repository.deleteAllReminders()
        val resultClearedList = repository.getReminders()
        val loadedEmptyList = (resultClearedList as Result.Success<List<ReminderDTO>>).data
        assertThat(loadedEmptyList, CoreMatchers.notNullValue())
        assertThat(loadedEmptyList, `is`(emptyList()))
    }
}