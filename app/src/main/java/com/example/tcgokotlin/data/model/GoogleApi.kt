package com.example.tcgokotlin.data.model

data class GoogleApi(
    var geocoded_waypoints: List<GeocodedWaypoint>,
    var routes: List<Route>,
    var status: String
)