package com.example.projectmobiledev.tracker

import android.location.Location
import com.google.android.gms.maps.model.PolylineOptions

class TrackerModel() {
    private val route = mutableListOf<Location>()

    fun addLocation(location : Location?){
        if (location != null)
            this@TrackerModel.route.add(location)
    }

    fun removeLocation(location: Location?){
        if (location != null)
            this@TrackerModel.route.remove(location)
    }

    fun getAllLocations(): MutableList<Location> {
        return route;
    }

}