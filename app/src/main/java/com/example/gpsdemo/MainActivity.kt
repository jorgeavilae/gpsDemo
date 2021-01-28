package com.example.gpsdemo

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.gpsdemo.databinding.ActivityMainBinding
import com.google.android.gms.common.server.response.FastJsonResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

const val DEFAULT_UPDATE_INTERVAL: Long = 30
const val FASTEST_UPDATE_INTERVAL: Long = 5
const val PERMISSIONS_FINE_LOCATION: Int = 99

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Configuration for client's location service
    private lateinit var locationRequest: LocationRequest

    // Client for location service
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        locationRequest = LocationRequest()
            .setInterval(1000 * DEFAULT_UPDATE_INTERVAL)
            .setFastestInterval(1000 * FASTEST_UPDATE_INTERVAL)
            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)

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