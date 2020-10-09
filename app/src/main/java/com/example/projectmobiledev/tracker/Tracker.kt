package com.example.projectmobiledev.tracker

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.projectmobiledev.Permissions
import com.example.projectmobiledev.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.CancellationToken
import kotlinx.android.synthetic.main.tracker.*
import java.lang.IllegalArgumentException

class Tracker : AppCompatActivity(), LocationListener, OnMapReadyCallback {

    private lateinit var currentLocation : LatLng
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
        polyLineOptions.width(9f);
        polyLineOptions.color(Color.MAGENTA)
        val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if(Permissions.checkLocationPermission(this)){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,10.0f,this)
        }
        btnCamera.setOnClickListener(cameraOnClick)
    }

    private fun updateCurrentLocation() {
        if (Permissions.checkLocationPermission(this)){
            //val location = locationProvider.getCurrentLocation(0,null)
            locationProvider.lastLocation.addOnSuccessListener(this)
            { location ->
                if (location != null){
                    val loc:LatLng = LatLng(location.latitude,location.longitude)
                    currentLocation = loc
                    startOnCurrentLocation()
                }
            }
        }
    }

    private fun startOnCurrentLocation(){
        if (Permissions.checkLocationPermission(this)){
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,18f))
        }
    }

    override fun onLocationChanged(location: Location) {
        controller.addLocation(location);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude,location.longitude),18f))
        drawMap();
        currentLocation = LatLng(location.latitude,location.longitude)
    }

    override fun onMapReady(googleMap: GoogleMap){
        map = googleMap
        line = map.addPolyline(polyLineOptions)
        updateCurrentLocation()
        if (Permissions.checkLocationPermission(this)) {
            map.isMyLocationEnabled = true
        }
    }

    private fun drawMap(){
        val points = controller.getAllLocations()
        line.points = points.map{location -> LatLng(location.latitude,location.longitude)  }
    }

    private val cameraOnClick = object : View.OnClickListener{
        override fun onClick(v: View?) {
            val openCamera : Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (openCamera.resolveActivity(packageManager) != null){
                startActivityForResult(openCamera,0)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            0 -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    // set the image in the imageView
                    imageView.setImageBitmap(data.extras?.get("data") as Bitmap)
                    // only add the marker when the picture is actually taken
                    val marker = MarkerOptions()
                    marker.position(LatLng(currentLocation.latitude,currentLocation.longitude));
                    map.addMarker(marker)
                }
            }
            else -> throw IllegalStateException("Image error")
        }
    }
}