package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, private val dataSource: ReminderDataSource) :
    BaseViewModel(app) {

    val reminderTitle = MutableLiveData<String>()
    val reminderDescription = MutableLiveData<String>()

    private var selectedPOI: PointOfInterest? = null
    val hasSelectedPOI = MutableLiveData(false)

    private val _savedPOI = MutableLiveData<PointOfInterest>()
    val savedPOI: LiveData<PointOfInterest>
            get() = _savedPOI

    val selectedLocation: LiveData<Location> = Transformations.map(_savedPOI) {
        it?.let {
            Location(it.name, it.latLng.latitude, it.latLng.longitude)
        }
    }
    private val _geofenceEvent = MutableLiveData<GeofenceData?>()
    val addGeofenceEvent: LiveData<GeofenceData?>
        get() = _geofenceEvent

    data class GeofenceData(
        var id: String,
        var location: Location
    )

    data class Location(
        var name: String,
        var latitude: Double,
        var longitude: Double
    )


    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        _savedPOI.value = null
        selectedPOI = null
        hasSelectedPOI.value = false
        _geofenceEvent.value = null
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder(reminderData: ReminderDataItem) {
        if (validateEnteredData(reminderData)) {
            saveReminder(reminderData)
            _geofenceEvent.value = GeofenceData(reminderData.id,
                Location(reminderData.location!!, reminderData.latitude!!, reminderData.longitude!!))
        }
    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.id
                )
            )
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            navigationCommand.value = NavigationCommand.Back
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    private fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }

    fun onAddGeofenceCompleted() {
        _geofenceEvent.value = null
    }

    fun onAddGeofenceFailed() {
        showSnackBarInt.value = R.string.error_adding_geofence
    }

    fun selectLocation(poi: PointOfInterest) {
        selectedPOI = poi
        hasSelectedPOI.value = true
    }

    fun saveLocation() {
        selectedPOI.let {
            _savedPOI.value = it
            navigationCommand.value = NavigationCommand.Back
        }
    }
}