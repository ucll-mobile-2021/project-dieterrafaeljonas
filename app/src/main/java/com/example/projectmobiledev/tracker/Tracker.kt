package com.example.projectmobiledev.tracker

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.projectmobiledev.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
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
    private lateinit var locationProvider : FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tracker)

        // setup map fragment and get notified when the map is ready to use
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationProvider = LocationServices.getFusedLocationProviderClient(this)

        polyLineOptions = PolylineOptions()
        polyLineOptions.width(5f);
        polyLineOptions.color(Color.MAGENTA)
        val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        // if anyone is reading this i am going to fix this later but for now it works, you can just run this code
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,10.0f,this)
    }

    private fun checkLocationPermission(): Boolean {
        // check if we already have location permission
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            // if not ask for location permission
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1);
        }

        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            return true
        return false
    }

    private fun startOnCurrentLocation(){
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationProvider.lastLocation.addOnSuccessListener(this)
            { location ->
                if (location != null){
                    val loc:LatLng = LatLng(location.latitude,location.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,18f))
                }
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.d("locationListener","location updated")
        controller.addLocation(location);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude,location.longitude),18f))
        drawMap();
    }

    override fun onMapReady(googleMap: GoogleMap){
        map = googleMap
        line = map.addPolyline(polyLineOptions)
        if (checkLocationPermission())
            map.isMyLocationEnabled = true
        val locationRequests = LocationRequest()
        locationRequests.interval = 5000
        locationRequests.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        startOnCurrentLocation()
    }

    private fun drawMap(){
        val points = controller.getAllLocations()
        line.points = points.map{location -> LatLng(location.latitude,location.longitude)  }
    }


}