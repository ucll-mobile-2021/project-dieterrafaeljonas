package com.example.projectmobiledev.tracker

import android.graphics.Bitmap
import android.location.Location
import com.google.android.gms.maps.model.LatLng

class TrackerController() {
    private val route : TrackerModel = TrackerModel()


    fun addLocation(location: LatLng?){
        route.addLocation(location);
    }

    fun removeLocation(location: LatLng?){
        route.removeLocation(location);
    }

    fun getAllLocations() : MutableList<LatLng>{
        return route.getAllLocations()
    }

    fun addMarker(location: LatLng?,bitmap: Bitmap){
        route.addMarker(location,bitmap);
    }

    fun removeMarker(location: LatLng?){
        route.removeMarker(location);
    }

    fun getAllMarkers() : MutableMap<LatLng,Bitmap>{
        return route.getAllMarkers()
    }

    fun getTotalDistance() : Float{
        return route.getTotalDistance();
    }

}