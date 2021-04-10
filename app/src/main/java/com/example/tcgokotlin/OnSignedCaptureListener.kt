package com.example.tcgokotlin

import android.graphics.Bitmap

interface OnSignedCaptureListener {
    fun onSignatureCaptured(bitmap: Bitmap, fileUri: String)
}