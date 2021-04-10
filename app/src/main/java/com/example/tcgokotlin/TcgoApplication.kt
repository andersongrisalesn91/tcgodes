package com.example.tcgokotlin

import android.app.Application
import android.widget.Toast
import com.example.tcgokotlin.Helper.GPSTracker
import com.example.tcgokotlin.Helper.Tools
import com.example.tcgokotlin.sqliteDBHelper.FuncionesGenerales
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class TcgoApplication: Application() {

    private lateinit var thread: Thread
    private lateinit var gpsInfo: GPSTracker
    private val isRunnable = true
    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
                setupThread()
                thread.start()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        thread.interrupt()
    }

    private fun setupThread() {
        thread = Thread {
            try {
                while (isRunnable) {
                    if (Tools.checkPermissionsUbication(applicationContext)) {
                        chargeLocation()
                    }
                    Thread.sleep(600000)
                }
            } catch (e: InterruptedException) {
                Thread.interrupted()
                e.printStackTrace()
            }
        }
    }

    private fun chargeLocation() {
        /*gpsInfo = GPSTracker(applicationContext)
        val fg = FuncionesGenerales()
        fg.ins_historial(applicationContext,"0", gpsInfo.currentLatitude.toString(), gpsInfo.currentLongitude.toString())*/
    }

}