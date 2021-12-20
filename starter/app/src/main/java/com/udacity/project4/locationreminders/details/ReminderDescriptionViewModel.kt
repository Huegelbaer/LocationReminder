package com.udacity.project4.locationreminders.details

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class ReminderDescriptionViewModel(val app: Application,
                                   private val dataSource: ReminderDataSource)
    : ViewModel() {

    val navigateBack: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val showToast: SingleLiveEvent<String> = SingleLiveEvent()

    fun markAsComplete(reminderData: ReminderDataItem) {
        viewModelScope.launch {
            dataSource.deleteReminder(reminderData.id)
            showToast.value = app.getString(R.string.reminder_saved)
            navigateBack.value = true
        }
    }
}