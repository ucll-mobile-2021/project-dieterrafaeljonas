package com.example.projectmobiledev.tracker

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationProvider
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.projectmobiledev.R
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

class Tracker : AppCompatActivity(), LocationListener, OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var polyLineOptions : PolylineOptions
    private lateinit var line : Polyline
    private val controller : TrackerController = TrackerController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tracker)

        // setup map fragment and get notified when the map is ready to use
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        polyLineOptions = PolylineOptions()
        polyLineOptions.width(5f);
        polyLineOptions.color(Color.MAGENTA)
    }

    private fun checkLocationPermission(){
        // check if we already have location permission
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            // if not ask for location permission
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1);
        }
    }

    override fun onLocationChanged(location: Location) {
        controller.addLocation(location);
        drawMap();
    }

    override fun onMapReady(googleMap: GoogleMap){
        map = googleMap
        line = map.addPolyline(polyLineOptions)
        checkLocationPermission()
        val locationRequests = LocationRequest()
        locationRequests.interval = 5000
        locationRequests.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun drawMap(){
        val points = controller.getAllLocations()
        line.points = points.map{location -> LatLng(location.latitude,location.longitude)  }
    }


}