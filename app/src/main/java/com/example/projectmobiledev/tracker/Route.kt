package com.example.projectmobiledev.tracker

import com.example.projectmobiledev.Time
import com.google.android.gms.maps.model.LatLng
import java.util.*

class Route(_distance: Double, _elapsedTime: Time, _locations: MutableList<LatLng>, _name: String, _guid: UUID?, _startDate: Date?) {
    var distance = _distance
    var elapsedTime = _elapsedTime
    var locations = _locations
    var name = _name
    var guid = _guid
    var startDate = _startDate

    constructor() : this(-1.0, Time(0), mutableListOf<LatLng>(), "", UUID.randomUUID(), null)

    fun getRouteCenter() : LatLng {
        var avgLat = 0.0
        var avgLong = 0.0
        for (location in locations) {
            avgLat += location.latitude
            avgLong += location.longitude
        }
        return LatLng(avgLat / locations.size, avgLong / locations.size)
    }

    fun computeSpeed() : Double {
        val d = distance
        val time : Double = elapsedTime.milliseconds.toDouble()
        val mms = d / time
        return mms * 3600
    }
}