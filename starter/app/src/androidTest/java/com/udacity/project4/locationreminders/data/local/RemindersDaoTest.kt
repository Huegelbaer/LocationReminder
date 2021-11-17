package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//    TODO: Add testing implementation to the RemindersDao.kt

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        val reminder = ReminderDTO("Title", null, "Location", 50.0, 8.0, "GetById")
        database.reminderDao().saveReminder(reminder)

        val loaded = database.reminderDao().getReminderById("GetById")
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.title, `is`("Title"))
        assertThat(loaded.description, nullValue())
        assertThat(loaded.location, `is`("Location"))
        assertThat(loaded.latitude, `is`(50.0))
        assertThat(loaded.longitude, `is`(8.0))
    }

    @Test
    fun updateReminderAndGetById() = runBlockingTest {
        val reminder = ReminderDTO("Title", null, "Location", 50.0, 8.0, "Update")
        database.reminderDao().saveReminder(reminder)

        val newReminder = ReminderDTO("Title", "Description", "Location", 50.0, 8.0, "Update")
        database.reminderDao().saveReminder(newReminder)


        val loaded = database.reminderDao().getReminderById("Update")
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.title, `is`("Title"))
        assertThat(loaded.description, `is`("Description"))
        assertThat(loaded.location, `is`("Location"))
        assertThat(loaded.latitude, `is`(50.0))
        assertThat(loaded.longitude, `is`(8.0))
    }

    @Test
    fun addRemindersAndClearDatabase() = runBlockingTest {
        val reminder = ReminderDTO("Title", null, "Location", 50.0, 8.0, "1")
        database.reminderDao().saveReminder(reminder)
        val reminder2 = ReminderDTO("Title 2", null, "Location", 50.0, 8.0, "2")
        database.reminderDao().saveReminder(reminder2)

        val loadedList = database.reminderDao().getReminders()
        assertThat(loadedList, notNullValue())
        assertThat(loadedList, not(emptyList()))
        assertThat(loadedList.size, `is`(2))

        database.reminderDao().deleteAllReminders()
        val loadedEmptyList = database.reminderDao().getReminders()
        assertThat(loadedList, notNullValue())
        assertThat(loadedList, `is`(emptyList()))
    }
}