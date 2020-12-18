package com.example.projectmobiledev.tracker

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.projectmobiledev.Activity2
import com.example.projectmobiledev.Permissions
import com.example.projectmobiledev.R
import com.example.projectmobiledev.home.Home
import com.example.projectmobiledev.login.LogIn
import com.example.projectmobiledev.pathFinder.PathFinder
import com.example.projectmobiledev.profile.Profile
import com.example.projectmobiledev.routesViewer.RoutesViewer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.tracker.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class Tracker : AppCompatActivity(), LocationListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var currentLocation : LatLng
    private lateinit var map: GoogleMap
    private lateinit var polyLineOptions : PolylineOptions
    private lateinit var line : Polyline
    private val controller : TrackerController = TrackerController()
    private lateinit var locationProvider : FusedLocationProviderClient
    // dialog for image popup
    private lateinit var popupDialog : Dialog
    private var tracking : Boolean = false

    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tracker)

        val startStopButton = findViewById<FloatingActionButton>(R.id.btnStopTracking);
        controller.startTracking()
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10.0f, this)
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
                R.id.Home -> startActivity(Intent(this, Home::class.java))
                R.id.Tracker -> startActivity(Intent(this, Tracker::class.java))
                R.id.Profile -> startActivity(Intent(this, Profile::class.java))
                R.id.PathFinder -> startActivity(Intent(this, PathFinder::class.java))
                R.id.RoutesOverview -> startActivity(Intent(this, RoutesViewer::class.java))
                R.id.LogOut -> {
                    Firebase.auth.signOut()
                    startActivity(Intent(this, LogIn::class.java))
                }
            }
            true
        }

        btnStopTracking.setOnClickListener{
            if (tracking) {
                val popup = AlertDialog.Builder(this)
                val inflater = layoutInflater
                val view = inflater.inflate(R.layout.save_route, null)
                popup.setView(view)
                    .setPositiveButton("Yes", DialogInterface.OnClickListener { popup, _ ->
                        val textview = view.findViewById<EditText>(R.id.route_name)
                        controller.setName(textview.text.toString())
                        controller.stopTracking()
                        controller.writeToDatabase()
                        Log.d("DB", "Written to database")
                        // redirect to home page
                        startActivity(Intent(this, PathFinder::class.java));
                    })
                    .setNegativeButton("No", DialogInterface.OnClickListener { popup, _ ->
                        controller.stopTracking();
                        // redirect to home page
                        startActivity(Intent(this, PathFinder::class.java));
                    })
                popup.show()
            }else{
                // add current point as starting point
                controller.addLocation(currentLocation);
                tracking = true;
                startStopButton.setImageResource(R.drawable.stop_tracking)
                controller.startTracking()
            }
        }

    }

    private fun saveImages() : Boolean {
        if (Permissions.checkWriteExternalStoragePermission(this)) {
            for ((k, v) in controller.getAllMarkers()) {
                if (v != null)
                    saveImage(v, k);
            }
            Log.d("Save", "Images Saved")
            return true
        }
        else{
            Permissions.askWriteExternalStoragePermission(this)
            return false
        }
    }

    private fun saveImage(image: Bitmap, location: LatLng) {
        // Hopelijk is dit collision proof
        val fileName = "PromenApp_${controller.getGuid()}_${location.latitude}_${location.longitude}.JPG"
        val storagedir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(storagedir, fileName)
        try {
            val stream: OutputStream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e: Exception){
            print("#######################################################")
            print(e.message)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.contentEquals(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))) {
            if (requestCode == Permissions.LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                map.isMyLocationEnabled = true
                updateCurrentLocation()
                if (Permissions.checkLocationPermission(this)){ // geen idee waarom ik dit hier moet zetten want ik zit letterlijk in een PermissionResult callback :confused:
                    val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        5000,
                        10.0f,
                        this
                    )
                }
            }
        }
        else if (permissions.contentEquals(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            if (requestCode == Permissions.WRITE_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                saveImages()
            }
        }
    }

    private fun updateCurrentLocation() {
        if (Permissions.checkLocationPermission(this)){
            //val location = locationProvider.getCurrentLocation(0,null)
            locationProvider.lastLocation.addOnSuccessListener(this)
            { location ->
                if (location != null){
                    val loc:LatLng = LatLng(location.latitude, location.longitude)
                    currentLocation = loc
                    startOnCurrentLocation()
                }
            }
        }
    }

    private fun startOnCurrentLocation(){
        if (Permissions.checkLocationPermission(this)){
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18f))
        }
    }

    override fun onLocationChanged(location: Location) {
        if(tracking){
            controller.addLocation(LatLng(location.latitude, location.longitude))
            drawMap()
        }
        else{
            Toast.makeText(
                this, "Location tracking not enabled, click on the play button to enable it",
                Toast.LENGTH_SHORT
            ).show()
        }
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    location.latitude,
                    location.longitude
                ), 18f
            )
        )
        currentLocation = LatLng(location.latitude, location.longitude)
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
        line.points = points.map{ location -> LatLng(location.latitude, location.longitude)  }
    }

    private val cameraOnClick = object : View.OnClickListener{
        override fun onClick(v: View?) {
            val openCamera : Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (openCamera.resolveActivity(packageManager) != null){
                startActivityForResult(openCamera, 0)
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
                    marker.position(LatLng(currentLocation.latitude, currentLocation.longitude))
                    map.addMarker(marker)
                    controller.addMarker(currentLocation, image)
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

    override fun onProviderEnabled(provider: String){
        super.onProviderEnabled(provider)
    }

    override fun onProviderDisabled(provider: String){
        super.onProviderDisabled(provider)
    }


    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        //super.onStatusChanged(provider, status, extras)
        when (status) {
            LocationProvider.OUT_OF_SERVICE -> {
                Log.d("Status", "Status Changed: Out of Service")
                Toast.makeText(
                    this, "Location Status Changed: Out of Service",
                    Toast.LENGTH_SHORT
                ).show()
            }
            LocationProvider.TEMPORARILY_UNAVAILABLE -> {
                Log.d("Status", "Status Changed: Temporarily Unavailable")
                Toast.makeText(
                    this, "Location Status Changed: Temporarily Unavailable",
                    Toast.LENGTH_SHORT
                ).show()
            }
            LocationProvider.AVAILABLE -> {
                Log.d("Status", "Status Changed: Available")
                Toast.makeText(
                    this, "Location Status Changed: Available",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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