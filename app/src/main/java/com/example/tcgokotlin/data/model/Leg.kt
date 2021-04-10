package com.example.tcgokotlin.data.model

data class Leg(
    var distance: Distance,
    var duration: Duration,
    var duration_in_traffic: DurationInTraffic,
    var end_address: String,
    var end_location: EndLocation,
    var start_address: String,
    var start_location: StartLocation,
    var steps: List<Step>,
    var traffic_speed_entry: List<Any>,
    var via_waypoint: List<Any>
)