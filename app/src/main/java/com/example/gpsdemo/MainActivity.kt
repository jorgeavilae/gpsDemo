package com.example.gpsdemo

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.gpsdemo.databinding.ActivityMainBinding
import com.google.android.gms.common.server.response.FastJsonResponse
import com.google.android.gms.location.*
import java.lang.Exception

const val DEFAULT_UPDATE_INTERVAL: Long = 30
const val FASTEST_UPDATE_INTERVAL: Long = 5
const val PERMISSIONS_FINE_LOCATION: Int = 99

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Configuration for client's location service
    private lateinit var locationRequest: LocationRequest

    // Client for location service
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // For location updates
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        locationRequest = LocationRequest()
            .setInterval(1000 * DEFAULT_UPDATE_INTERVAL)
            .setFastestInterval(1000 * FASTEST_UPDATE_INTERVAL)
            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)

        // Triggered when update interval is met
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                updateUiValues(locationResult.lastLocation)
            }
        }


        binding.swGps.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // More accuracy GPS
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                binding.tvSensor.text = "Using GPS sensor"
            } else {
                // Normal balanced mode
                locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                binding.tvSensor.text = "Using towers + WIFI"
            }
        }

        binding.swLocationsupdates.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Turn on location tracking
                startLocationUpdates()
            } else {
                // Turn off location tracking
                stopLocationUpdates()
            }
        }

        updateGPS()
    }

    private fun updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Request permissions
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission provided. Get the current location from Fused Client
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                // Update UI
                updateUiValues(it)
            }

        } else {
            // Ask for permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_FINE_LOCATION
                )
                finish()
            }
        }


    }

    private fun updateUiValues(location: Location) {
        binding.tvLat.text = location.latitude.toString()
        binding.tvLon.text = location.longitude.toString()
        binding.tvAccuracy.text = location.accuracy.toString()

        binding.tvAltitude.text =
            if (location.hasAltitude())
                location.altitude.toString()
            else
                "Not available"
        binding.tvSpeed.text =
            if (location.hasSpeed())
                location.speed.toString()
            else
                "Not available"

        var geocoder : Geocoder = Geocoder(this)
        try {
            var addresses : List<Address>  = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            binding.tvAddress.text = addresses[0].getAddressLine(0)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clearUiValues() {
        binding.tvLat.text = "Not tracking location"
        binding.tvLon.text = "Not tracking location"
        binding.tvAccuracy.text = "Not tracking location"

        binding.tvAltitude.text = "Not tracking location"
        binding.tvSpeed.text = "Not tracking location"

        binding.tvAddress.text = "Not tracking location"
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        binding.tvUpdates.text = "Location is being tracked"
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun stopLocationUpdates() {
        binding.tvUpdates.text = "Location is not being tracked"
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        clearUiValues()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSIONS_FINE_LOCATION ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS()
                } else {
                    Toast.makeText(this, "GPS permissions are necessary", Toast.LENGTH_SHORT).show()
                }
        }
    }
}