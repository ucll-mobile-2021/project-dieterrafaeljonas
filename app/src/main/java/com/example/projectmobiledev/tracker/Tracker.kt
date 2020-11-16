package com.example.projectmobiledev.tracker

import android.app.Activity
import android.app.Dialog
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
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.projectmobiledev.Activity2
import com.example.projectmobiledev.Permissions
import com.example.projectmobiledev.R
import com.example.projectmobiledev.login.LogIn
import com.example.projectmobiledev.pathFinder.PathFinder
import com.example.projectmobiledev.profile.Profile
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.tracker.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Tracker : AppCompatActivity(), LocationListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var currentLocation : LatLng
    private lateinit var map: GoogleMap
    private lateinit var polyLineOptions : PolylineOptions
    private lateinit var line : Polyline
    private val controller : TrackerController = TrackerController()
    private lateinit var locationProvider : FusedLocationProviderClient
    // dialog for image popup
    private lateinit var popupDialog : Dialog

    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        controller.startTracking()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tracker)
        popupDialog = Dialog(this)
        // setup map fragment and get notified when the map is ready to use
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationProvider = LocationServices.getFusedLocationProviderClient(this)
        polyLineOptions = PolylineOptions()
        polyLineOptions.width(9f)
        polyLineOptions.color(Color.MAGENTA)
        val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if(Permissions.checkLocationPermission(this)){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,10.0f,this)
        }else{
            Permissions.askLocationPermission(this);
        }

        btnCamera.setOnClickListener(cameraOnClick)

        //Initialiseren van de toggle
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        //Toggle instellen als de knop waar op te klikken valt
        drawerLayout.addDrawerListener(toggle)
        //Toggle klaar zetten voor gebruik
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //Clicks op menu items afhandelen
        nav_view.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.Tracker -> startActivity(Intent(this, Tracker::class.java))
                R.id.Example -> startActivity(Intent(this, Activity2::class.java))
                R.id.Profile -> startActivity(Intent(this, Profile::class.java))
                R.id.PathFinder -> startActivity(Intent(this, PathFinder::class.java))
                R.id.LogOut -> {
                    Firebase.auth.signOut()
                    startActivity(Intent(this, LogIn::class.java))
                }
            }
            true
        }

        btnStopTracking.setOnClickListener{
            controller.stopTracking()
            Log.d("Time",controller.getElapsedTime().toString())
            // TODO write to database
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.contentEquals(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))) {
            if (requestCode == Permissions.LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                map.isMyLocationEnabled = true
                updateCurrentLocation()
                if (Permissions.checkLocationPermission(this)){ // geen idee waarom ik dit hier moet zetten want ik zit letterlijk in een PermissionResult callback :confused:
                    val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,10.0f,this)
                }
            }
        }
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
        controller.addLocation(LatLng(location.latitude,location.longitude))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude,location.longitude),18f))
        drawMap()
        currentLocation = LatLng(location.latitude,location.longitude)
        val distance = controller.getTotalDistance()
        println(distance)
    }

    override fun onMapReady(googleMap: GoogleMap){
        map = googleMap
        line = map.addPolyline(polyLineOptions)
        updateCurrentLocation()
        if (Permissions.checkLocationPermission(this)) {
            map.isMyLocationEnabled = true
        }
        map.setOnMarkerClickListener(this)
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
                    val image = data.extras?.get("data") as Bitmap
                    // only add the marker when the picture is actually taken
                    val marker = MarkerOptions()
                    marker.position(LatLng(currentLocation.latitude,currentLocation.longitude))
                    map.addMarker(marker)
                    controller.addMarker(currentLocation,image)
                }
            }
            else -> throw IllegalStateException("Image error")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker != null) {
            val markers = controller.getAllMarkers()
            val markerlatLng: LatLng? = marker.position
            for (markerEntry in markers) {
                if (markerEntry.key == markerlatLng){
                    // found the right marker
                    // show image on popup
                    popupDialog.setContentView(R.layout.image_popup)
                    popupDialog.findViewById<ImageView>(R.id.imagePopup).setImageBitmap(markerEntry.value)
                    popupDialog.findViewById<ImageButton>(R.id.btnClose).setOnClickListener { popupDialog.dismiss() }
                    popupDialog.show()
                    return true
                }
            }
        }
        return false
    }
}