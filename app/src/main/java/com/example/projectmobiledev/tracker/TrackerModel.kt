package com.example.projectmobiledev.tracker

import android.graphics.Bitmap
import android.location.Location
import com.example.projectmobiledev.Time
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject
import java.util.*

class TrackerModel() {
    var guid: UUID? = UUID.randomUUID()
    lateinit var userEmail: String
    private val route = mutableListOf<LatLng>()
    private val markers = mutableMapOf<LatLng,Bitmap?>()
    private var totalDistance : Double = 0.0;
    lateinit var startDate : Date
    var  endDate : Date? = null
    lateinit var name: String

    fun start(){
        val user = FirebaseAuth.getInstance().currentUser;
        userEmail = user?.email!!
        startDate = Calendar.getInstance().time
        name = startDate.toString();
    }

    fun end(){
        endDate = Calendar.getInstance().time;
    }

    fun addLocation(location : LatLng?){
        if (location != null){
            route.add(location)
            calculateDistance()
        }
    }

    fun removeLocation(location: LatLng?){
        if (location != null)
            route.remove(location)
    }

    fun getLocations(): MutableList<LatLng> {
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

    fun getAllMarkers(): MutableMap<LatLng,Bitmap?> {
        return markers;
    }

    fun getTotalDistance(): Double {
        return totalDistance;
    }

    fun getElapsedTime() : Time {
        if (endDate != null){
            val elapsedTime = endDate?.time?.minus(startDate.time);
            return Time(elapsedTime!!)
        }
        else{
            // if the route hasn't ended yet return current elapsed time
            val elapsedTime = Calendar.getInstance().time.time.minus(startDate.time);
            return Time(elapsedTime)
        }
    }

    fun calculateDistance() {
        var totalDistance = 0.0
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
    }

    override fun toString(): String {
        return "TrackerModel(guid=$guid, userEmail='$userEmail', route=$route, totalDistance=$totalDistance, startDate=$startDate, endDate=$endDate)"
    }

    fun setLocations(locations: MutableList<LatLng>) {
        route.clear()
        route.addAll(locations)
    }

    fun setMarkers(new_markers: MutableMap<LatLng, Bitmap?>) {
        markers.clear()
        markers.putAll(new_markers)
    }

    fun toJson() : String {
        val json : JSONObject = JSONObject()
        json.put("guid", guid)
        json.put("userEmail", userEmail)
        json.put("route", route.map { latLng -> "${latLng.latitude};${latLng.longitude}"  })
        val locationslist = markers.map { entry -> entry.key }
        json.put("markers", locationslist.map { location -> "${location.latitude};${location.longitude}" })
        json.put("totalDistance", totalDistance)
        json.put("startDate",startDate.time)
        json.put("endDate",endDate?.time)
        json.put("name",name)
        return json.toString()
    }

    fun getRouteCenter() : LatLng {
        var avgLat = 0.0
        var avgLong = 0.0
        for (location in route) {
            avgLat += location.latitude
            avgLong += location.longitude
        }
        return LatLng(avgLat / route.size, avgLong / route.size)
    }

    fun computeSpeed() : Double {
        val d = totalDistance
        val time : Double = getElapsedTime().milliseconds.toDouble()
        val mms = d / time
        return mms * 3600
    }


}