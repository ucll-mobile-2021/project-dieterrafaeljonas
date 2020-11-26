package com.example.projectmobiledev.tracker

import android.graphics.Bitmap
import com.example.projectmobiledev.Time
import com.example.projectmobiledev.database.Database
import com.example.projectmobiledev.database.RoutesCallback
import com.google.android.gms.maps.model.LatLng

class TrackerController() {
    private val route : TrackerModel = TrackerModel()
    private val database: Database = Database()

    fun startTracking(){
        route.start()
    }

    fun addLocation(location: LatLng?){
        route.addLocation(location);
    }

    fun removeLocation(location: LatLng?){
        route.removeLocation(location);
    }

    fun getAllLocations() : MutableList<LatLng>{
        return route.getLocations()
    }

    fun addMarker(location: LatLng?,bitmap: Bitmap){
        route.addMarker(location,bitmap);
    }

    fun removeMarker(location: LatLng?){
        route.removeMarker(location);
    }

    fun getAllMarkers() : MutableMap<LatLng,Bitmap?>{
        return route.getAllMarkers()
    }

    fun getTotalDistance() : Double{
        return route.getTotalDistance();
    }

    fun stopTracking() {
        route.end()
    }

    fun getElapsedTime(): Time {
        return route.getElapsedTime()
    }

    fun writeToDatabase() {
        database.writeRoute(route)
    }

    fun getAll(callback: RoutesCallback) {
        database.getAll(callback)
    }

    fun getGuid(): String {
        return route.guid.toString()
    }



}