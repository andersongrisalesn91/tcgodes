package com.example.tcgokotlin.utils.AnimationUtils

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import com.example.tcgokotlin.R

class AnimationUtils {

    companion object {

        fun doBounceAnimation(mContext: Context, targetView: View) {
            val bounceAnimation = AnimationUtils.loadAnimation(mContext, R.anim.bounce_animation)
            targetView.startAnimation(bounceAnimation)
        }
    }

}