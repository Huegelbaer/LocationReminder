package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import com.google.android.gms.maps.model.MarkerOptions
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback, GoogleMap.OnPoiClickListener, GoogleMap.OnMapLongClickListener {

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 10
    }

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var googleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val supportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync(this)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(p0: GoogleMap?) {
        p0?.let { map ->
            setupMap(map)
            checkPermissionAndEnableLocationIfGranted()
            _viewModel.savedPOI.value?.let { poi ->
                setMarker(poi)
            }
        }
    }

    private fun setupMap(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isCompassEnabled = true
        map.setOnMapLongClickListener(this)
        map.setOnPoiClickListener(this)
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
    }

    private fun getDeviceLocation(map: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        map.isMyLocationEnabled = true

        val lastLocationTask = LocationServices.getFusedLocationProviderClient(requireActivity())
            .lastLocation
        lastLocationTask.addOnCompleteListener { task ->
            task.result?.let { lastLocation ->
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(lastLocation.latitude, lastLocation.longitude),
                        16.0F
                    )
                )
            }
        }
    }

    override fun onMapLongClick(p0: LatLng?) {
        p0?.let { latLng ->
            val poi = PointOfInterest(latLng, "", "Custom location")
            setMarker(poi)
            _viewModel.selectLocation(poi)
        }
    }

    override fun onPoiClick(p0: PointOfInterest?) {
        p0?.let { poi ->
            setMarker(poi)
            _viewModel.selectLocation(poi)
        }
    }

    private fun setMarker(poi: PointOfInterest) {
        googleMap.clear()
        googleMap.addMarker(
            MarkerOptions()
                .position(poi.latLng)
                .title(poi.name)
        )
    }

    private fun checkPermissionAndEnableLocationIfGranted() {
        val isGranted = PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        if (isGranted) {
            getDeviceLocation(googleMap)
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissionAndEnableLocationIfGranted()
                } else {
                    showLocationPermissionExplanation()
                }
            }
            else -> {}
        }
    }

    private fun showLocationPermissionExplanation() {
        Snackbar
            .make(
                requireView(),
                R.string.permission_denied_explanation,
                BaseTransientBottomBar.LENGTH_LONG
            )
            .setAction(R.string.settings) {
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
            .show()
    }
}
