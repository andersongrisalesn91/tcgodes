package com.example.tcgokotlin.ModulMain

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.tcgokotlin.R
import com.example.tcgokotlin.sqliteDBHelper.Backups
import com.example.tcgokotlin.sqliteDBHelper.FuncionesGenerales
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var mLocationRequest: LocationRequest? = null
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 300000
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }else {
            createLocationRequest()
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?: return
                    for (location in locationResult.locations){
                        locationStart()
                    }
                }
            }
            locationStart()
        }
        
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_map,
                R.id.navigation_tasks,
                R.id.navigation_Sinc,
                R.id.navigation_menu
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        setBottomNavVisibility()
    }
    
    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient?.requestLocationUpdates(
            mLocationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }
    
    private fun locationStart() {
        val mlocManager: LocationManager =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val Local = Localizacion()
        Local.mainActivity = this
        val gpsEnabled: Boolean = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val met = (0.0).toFloat()
        mlocManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            300000,
            met,
            Local as LocationListener
        )
        mlocManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            300000,
            met,
            Local as LocationListener
        )
    }

    inner class Localizacion : LocationListener {
        val fg = FuncionesGenerales()
        var mainActivity: MainActivity? = null
        override fun onLocationChanged(loc: Location) {
            loc.getLatitude()
            loc.getLongitude()
            val bk = Backups()
            bk.backupdDatabase(baseContext)
            fg.ins_historial(baseContext, "0", loc.latitude.toString(), loc.longitude.toString())
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            when (status) {
                LocationProvider.AVAILABLE -> Log.d("debug", "LocationProvider.AVAILABLE")
                LocationProvider.OUT_OF_SERVICE -> Log.d("debug", "LocationProvider.OUT_OF_SERVICE")
                LocationProvider.TEMPORARILY_UNAVAILABLE -> Log.d(
                    "debug",
                    "LocationProvider.TEMPORARILY_UNAVAILABLE"
                )
            }
        }
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest?.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
        mLocationRequest?.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
        mLocationRequest?.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }

    private fun setBottomNavVisibility(){
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when(destination.id){
                R.id.editProfileFragment -> hideBottomNav()
                R.id.navigation_options -> hideBottomNav()
                R.id.formaFragment -> hideBottomNav()
                R.id.MenuFragment -> hideBottomNav()
                R.id.SubMenuFragment -> hideBottomNav()
                R.id.Tipo2Fragment -> hideBottomNav()
                R.id.Tipo3Fragment -> hideBottomNav()
                R.id.Tipo8Fragment -> hideBottomNav()
                R.id.Tipo9Fragment -> hideBottomNav()
                R.id.Tipo10Fragment -> hideBottomNav()
                R.id.Tipo11Fragment -> hideBottomNav()
                R.id.FotosFragment -> hideBottomNav()
                else -> showBottomNav()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp()
    }

    private fun showBottomNav(){
        nav_view.visibility = View.VISIBLE
    }

    private fun hideBottomNav(){
        nav_view.visibility = View.GONE
    }


}