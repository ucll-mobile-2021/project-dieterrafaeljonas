package com.example.projectmobiledev.tracker

import android.location.Location

class TrackerModel() {
    private val route = mutableListOf<Location>()
    private val markers = mutableListOf<Location>()

    fun addLocation(location : Location?){
        if (location != null)
            route.add(location)
    }

    fun removeLocation(location: Location?){
        if (location != null)
            route.remove(location)
    }

    fun getAllLocations(): MutableList<Location> {
        return route;
    }

    fun addMarker(location : Location?){
        if (location != null)
            markers.add(location)
    }

    fun removeMarker(location: Location?){
        if (location != null)
            markers.remove(location)
    }

    fun getAllMarkers(): MutableList<Location> {
        return markers;
    }

}