package com.udacity.project4.locationreminders.details

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

class ReminderDescriptionViewModel(val app: Application,
                                   private val dataSource: ReminderDataSource)
    : BaseViewModel(app) {

    fun markAsComplete(reminderData: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.deleteReminder(reminderData.id)
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            navigationCommand.value = NavigationCommand.Back
        }
    }
}