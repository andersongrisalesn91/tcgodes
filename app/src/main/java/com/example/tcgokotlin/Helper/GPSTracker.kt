package com.example.tcgokotlin.Helper

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import java.util.concurrent.Executor
import java.util.function.Consumer

class GPSTracker(private val mContext: Context) : Service(),
    LocationListener {
    // flag for GPS status
    var isGPSEnabled = false

    // flag for network status
    var isNetworkEnabled = false
    var getFor = ""

    // flag for GPS status
    var canGetLocation = false
    var currentLocation: Location? = null
    var currentLatitude = 0.0
    var currentLongitude = 0.0
    var strError = ""

    // Declaring a Location Manager
    protected var locationManager: LocationManager? = null

    @SuppressLint("MissingPermission")
    fun getLocation(): Location? {
        // try {
        locationManager = mContext
            .getSystemService(LOCATION_SERVICE) as LocationManager

        // getting GPS status
        isGPSEnabled = locationManager!!
            .isProviderEnabled(LocationManager.GPS_PROVIDER)

        // getting network status
        isNetworkEnabled = locationManager!!
            .isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!isGPSEnabled && !isNetworkEnabled) {
            // no network provider is enabled
        } else {
            canGetLocation = true
            if (isGPSEnabled) {
                getFor = "GPS"
                // if (location == null) {
                locationManager!!.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                )
                if (locationManager != null) {
                    currentLocation =
                        locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (currentLocation != null) {
                        currentLatitude = currentLocation!!.latitude
                        currentLongitude = currentLocation!!.longitude
                    }
                }
                //  }
            }
            if (isNetworkEnabled) {
                if (currentLatitude == 0.0 || currentLongitude == 0.0) {
                    getFor = "NETWORK"
                    locationManager!!.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                    )
                    if (locationManager != null) {
                        currentLocation =
                            locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (currentLocation != null) {
                            currentLatitude = currentLocation!!.latitude
                            currentLongitude = currentLocation!!.longitude
                        }
                    }
                }
            }
            // if GPS Enabled get lat/long using GPS Services
        }

        /*
        } catch (Exception e) {
            strError = e.getMessage();
        }*/return currentLocation
    }

    private val locationCallback = Consumer<Location> { location ->
        if (null != location) {
            val latx = "Latitude: " + location.latitude
            val lonx = "Longitude: " + location.longitude.toString()
            val latlon = latx.toString() + " - " + lonx.toString()
        }
    }

    /**
     * Stop using GPS listener Calling this function will stop using GPS in your
     * app.
     */
    fun stopUsingGPS() {
        if (locationManager != null) {
            locationManager!!.removeUpdates(this@GPSTracker)
        }
    }

    /**
     * Function to get latitude
     */
    fun getLatitude(): Double {
        if (currentLocation != null) {
            currentLatitude = currentLocation!!.latitude
        }
        // return latitude
        return currentLatitude
    }

    /**
     * Function to get longitude
     */
    fun getLongitude(): Double {
        if (currentLocation != null) {
            currentLongitude = currentLocation!!.longitude
        }

        // return longitude
        return currentLongitude
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     */
    fun canGetLocation(): Boolean {
        return canGetLocation
    }

    /**
     * Function to show settings alert dialog On pressing Settings button will
     * lauch Settings Options
     */
    fun showSettingsAlert() {
        val alertDialog = AlertDialog.Builder(mContext)

        // Setting DialogHelp Title
        alertDialog.setTitle("GPS is settings")

        // Setting DialogHelp Message
        alertDialog
            .setMessage("GPS is not enabled. Do you want to go to settings menu?")

        // On pressing Settings button
        alertDialog.setPositiveButton(
            "Settings"
        ) { dialog, which ->
            val intent = Intent(
                Settings.ACTION_LOCATION_SOURCE_SETTINGS
            )
            mContext.startActivity(intent)
        }

        // on pressing cancel button
        alertDialog.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog.cancel() }

        // Showing Alert Message
        alertDialog.show()
    }

    override fun onLocationChanged(location: Location) {
        var bestAccuracy = -1f
        /* if (location.getAccuracy() != 0.0f
                && (location.getAccuracy() < bestAccuracy) || bestAccuracy == -1f) {
            locationManager.removeUpdates(this);
        }*/bestAccuracy = location.accuracy
        if (location != null) {
            if (location.latitude != 0.0) {
                currentLatitude = location.latitude
            }
            if (location.longitude != 0.0) {
                currentLongitude = location.longitude
            }
        }
    }

    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    companion object {
        // The minimum distance to change Updates in meters
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // 10 meters

        // The minimum time between updates in milliseconds
        private const val MIN_TIME_BW_UPDATES = 1000 * 60 * 1 // 1 minute
            .toLong()
    }

    init {
        getLocation()
    }
}