package com.example.projectmobiledev.tracker

import android.graphics.Bitmap
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import java.sql.Time

class TrackerModel() {
    private val route = mutableListOf<LatLng>()
    //private val markers = mutableListOf<Location>()
    private val markers = mutableMapOf<LatLng,Bitmap>()
    private var totalDistance = 0.0f;
    private var totalTime = Time(0,0,0);

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

    fun getTotalDistance() : Float {
        var totalDistance = 0.0f
        if (route.size > 1) {
            val floatArray = floatArrayOf(1.0f)
            for (i in 0 until route.size - 1) {
                Location.distanceBetween(
                    route[i].latitude,
                    route[i].longitude,
                    route[i + 1].latitude,
                    route[i + 1].longitude,
                    floatArray
                );
                totalDistance += floatArray[0];
            }
        }
        this@TrackerModel.totalDistance = totalDistance;
        return totalDistance;
    }

}