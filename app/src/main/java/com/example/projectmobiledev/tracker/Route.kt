package com.example.projectmobiledev.tracker

import com.example.projectmobiledev.Time
import com.google.android.gms.maps.model.LatLng

class Route(_distance: Double, _elapsedTime: Time, _locations: MutableList<LatLng>) {
    var distance = _distance
    var elapsedTime = _elapsedTime
    var locations = _locations

    constructor() : this(-1.0, Time(0), mutableListOf<LatLng>())

    fun getRouteCenter() : LatLng {
        var avgLat = 0.0
        var avgLong = 0.0
        for (location in locations) {
            avgLat += location.latitude
            avgLong += location.longitude
        }
        return LatLng(avgLat / locations.size, avgLong / locations.size)
    }
}