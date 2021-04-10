package com.example.tcgokotlin.data.model

import com.google.android.gms.maps.model.LatLng

data class Bounds(
        var northeast: LatLng,
        var southwest: LatLng
)
