package com.example.projectmobiledev.tracker

import android.graphics.Bitmap
import android.location.Location
import com.google.android.gms.maps.model.LatLng

class TrackerModel() {
    private val route = mutableListOf<LatLng>()
    //private val markers = mutableListOf<Location>()
    private val markers = mutableMapOf<LatLng,Bitmap>()

    fun addLocation(location : LatLng?){
        if (location != null)
            route.add(location)
    }

    fun removeLocation(location: LatLng?){
        if (location != null)
            route.remove(location)
    }

    fun getAllLocations(): MutableList<LatLng> {
        return route;
    }

    fun addMarker(location : LatLng?, bitmap: Bitmap){
        if (location != null)
            markers.put(location,bitmap);
    }

    fun removeMarker(location: LatLng?){
        if (location != null)
            markers.remove(location)
    }

    fun getAllMarkers(): MutableMap<LatLng,Bitmap> {
        return markers;
    }

}