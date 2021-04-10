package com.example.tcgokotlin.utils.AnimationUtils

import android.app.Activity
import com.example.tcgokotlin.R
import com.tapadoo.alerter.Alerter

class NotificationAlerter {

    companion object {
        fun createAlertError(message: String, activity: Activity){
            Alerter.create(activity)
                .setTitle(R.string.error)
                .setText(message)
                .setDuration(4000)
                .setDismissable(true)
                .setBackgroundColorRes(R.color.red)
                .show()
        }
        fun createAlert(message: String, activity: Activity){
            Alerter.create(activity)
                .setDuration(4000)
                .setDismissable(true)
                .setText(message)
                .setBackgroundColorRes(R.color.holo_green_light)
                .show()
        }
    }


}