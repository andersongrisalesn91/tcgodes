package com.example.tcgokotlin.data.model

data class Step(
    var distance: DistanceX,
    var duration: DurationX,
    var end_location: EndLocationX,
    var html_instructions: String,
    var maneuver: String,
    var polyline: Polyline,
    var start_location: StartLocationX,
    var travel_mode: String
)