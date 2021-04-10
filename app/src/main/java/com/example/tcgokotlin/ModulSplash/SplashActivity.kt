package com.example.tcgokotlin.ModulSplash

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.tcgokotlin.ModulLogin.LoginActivity
import com.example.tcgokotlin.ModulMain.MainActivity
import com.example.tcgokotlin.R
import com.example.tcgokotlin.data.model.DataUser
import com.example.tcgokotlin.utils.AnimationUtils.AnimationUtils
import com.example.tcgokotlin.utils.Common
import com.example.tcgokotlin.utils.sharedPreferences.SesionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.BuildConfig
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.MetadataChanges
import kotlinx.android.synthetic.main.activity_splash.*


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Common.auth = FirebaseAuth.getInstance()
        executeThread()
    }

    private fun executeThread() {
       Thread {
            run {
                runOnUiThread { AnimationUtils.doBounceAnimation(this, logo) }

                Thread.sleep(3000)

                if (booleanCurrentUser()) {
                    setDataBaseFirebase()
                    setLogin()
                } else {
                    intentLogin()
                }
            }
        }.start()
    }

    private fun intentLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun booleanCurrentUser():Boolean {
        var currentUs = false
        if (Common.auth?.currentUser != null) {
            currentUs = true
        }
        return currentUs
    }

    private fun setLogin () {
        if (Common.auth?.currentUser != null) {
                Common.setDriversInformation("email", Common.auth?.currentUser?.email)
                Common.dbDriversInformation = Common.db?.collection("DriversInformation")
                try { Common.listenerRegDriversInformation?.remove() } catch (e: Exception) { }
                Common.listenerRegDriversInformation = Common.dbDriversInformation?.whereEqualTo(
                    "UserUID",
                    Common.auth?.currentUser?.uid
                )?.addSnapshotListener(MetadataChanges.INCLUDE, EventListener { querySnapshot, e ->
                    if (e != null) {
                        intentLogin()
                        return@EventListener
                        intentLogin()
                    } else {
                        if (querySnapshot?.documentChanges?.size!! > 0) {
                            for (change in querySnapshot.documentChanges) {
                                Common.documentUser = change.document.data
                                Common.documentUser?.put("key", querySnapshot.documents[0].id)
                                login()
                            }
                        } else {
                            intentLogin()
                        }
                    }
                })
        } else {
            intentLogin()
        }
    }


    private fun setDataBaseFirebase() {
        try {
            Common.versionCode = BuildConfig.VERSION_CODE
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        Common.setDbSettings()
        Common.mContext = this
        Common.mLayoutInflater = layoutInflater
        Common.versionListener()
    }



    private fun login() {
            if (Common.documentUser?.get("IMEI") != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                if (Common.documentUser?.get("IMEI").toString() == Common.getDeviceId(this)) {
                    setDataUser()
                    val intent = Intent(baseContext, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    intentLogin()
                }
            } else {
                intentLogin()
            }
    }

    private fun setDataUser() {
        val dataUser = DataUser(Common.auth?.currentUser?.uid, Common.auth?.currentUser?.email)
        val sm = SesionManager(this)
        sm.setDataUser(dataUser)
    }

}