package com.example.projectmobiledev.tracker

import android.location.Location

class TrackerController() {
    private val route : TrackerModel = TrackerModel()


    fun addLocation(location: Location?){
        route.addLocation(location);
    }

    fun removeLocation(location: Location?){
        route.removeLocation(location);
    }

    fun getAllLocations() : MutableList<Location>{
        return route.getAllLocations()
    }

    fun addMarker(location: Location?){
        route.addMarker(location);
    }

    fun removeMarker(location: Location?){
        route.removeMarker(location);
    }

    fun getAllMarkers() : MutableList<Location>{
        return route.getAllMarkers()
    }

}