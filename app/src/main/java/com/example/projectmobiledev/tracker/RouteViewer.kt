package com.example.projectmobiledev.tracker

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.example.projectmobiledev.Activity2
import com.example.projectmobiledev.R
import com.example.projectmobiledev.Time
import com.example.projectmobiledev.database.Database
import com.example.projectmobiledev.database.ImageReadyCallback
import com.example.projectmobiledev.home.Home
import com.example.projectmobiledev.login.LogIn
import com.example.projectmobiledev.pathFinder.PathFinder
import com.example.projectmobiledev.profile.Profile
import com.example.projectmobiledev.routesViewer.RoutesViewer
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.image_popup.*
import kotlinx.android.synthetic.main.tracker.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

@Suppress("CAST_NEVER_SUCCEEDS")
class RouteViewer() : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var map : GoogleMap
    private lateinit var route: TrackerModel
    private lateinit var polyLineOptions : PolylineOptions
    private lateinit var line : Polyline
    private val locations = mutableListOf<LatLng>()
    private val markers = mutableMapOf<LatLng, Bitmap?>()
    private val database = Database()
    private lateinit var popupDialog : Dialog
    private lateinit var route_guid : String
    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.route_viewer)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        routeInit()
        //btnClose.setOnClickListener(goToHome)
        polyLineOptions = PolylineOptions()
        polyLineOptions.width(9f)
        polyLineOptions.color(resources.getColor(R.color.colorAccent))
        popupDialog = Dialog(this)
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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun routeInit() {
        val routeJson = intent.extras?.getString("route")
        //val routeJson = intent.getStringExtra("route")
        val json = JsonParser.parseString(routeJson).asJsonObject
        route = TrackerModel()
        route.userEmail = json.get("userEmail").asString
        route_guid = json.get("guid").asString
        val locationString = json.getAsJsonPrimitive("route").asString
        if (locationString != "[]"){
            locations.addAll(readLocations(locationString))
        }
        val markerString = json.getAsJsonPrimitive("markers").asString
        if (markerString != "[]") {
            val markers_list = readLocations(markerString)
            for (i in markers_list) {
                markers.put(i, null)
            }
        }
        route.name = json.get("name").asString
        route.endDate = Date(json.get("endDate").asLong)
        route.startDate = Date(json.get("startDate").asLong)
        route.setLocations(locations)
        route.calculateDistance()
        // set time and distance
        val time_text = findViewById<TextView>(R.id.txtTime)
        val distance_text = findViewById<TextView>(R.id.txtDistance)
        val distance = BigDecimal(route.getTotalDistance() / 1000.0)
        val speed = BigDecimal(route.computeSpeed())
        time_text.text = "Elapsed time: \n${route.getElapsedTime()}"
        distance_text.text = "Distance: ${distance.setScale(2,RoundingMode.HALF_EVEN)} Km\nSpeed: ${speed.setScale(2,RoundingMode.HALF_EVEN)} Km/h"
    }

    private fun readLocations(locations : String) : List<LatLng> {
        val result = mutableListOf<LatLng>()
        var locationString = locations
        locationString = locationString.replace("[","")
        locationString = locationString.replace("]","")
        val locations_ = locationString.split(",")
        for (location in locations_){
            val latlng = location.split(";")
            result.add(LatLng(latlng[0].toDouble(),latlng[1].toDouble()))
        }
        return result
    }

    private fun drawlines() {
        line.points = route.getLocations()
    }

    private fun setupmarkers() {
        for ((k,v) in markers){
            val markeroptions = MarkerOptions()
            markeroptions.position(k)
            map.addMarker(markeroptions)
        }
    }

    override fun onMapReady(googlemap: GoogleMap) {
        map = googlemap
        line = map.addPolyline(polyLineOptions)
        map.setOnMarkerClickListener(this)
        line.points = locations
        setupmarkers()
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(line.points[0],18.0f))
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker != null){
            for (markerEntry in markers){
                if (markerEntry.key == marker.position){
                    if (markerEntry.value != null){
                        Log.d("Markers", "Cached image")
                        val inflater = layoutInflater
                        val view = inflater.inflate(R.layout.image_popup, null)
                        val imageview = view.findViewById<ImageView>(R.id.imagePopup);
                        val image = markerEntry.value
                        val aspect = image?.width?.toDouble()!! / image?.height?.toDouble()!!;
                        popupDialog.setContentView(R.layout.image_popup)
                        popupDialog.findViewById<ImageView>(R.id.imagePopup).setImageBitmap(markerEntry.value)
                        popupDialog.findViewById<ImageButton>(R.id.btnClose).setOnClickListener{ popupDialog.dismiss() }
                        popupDialog.show()
                    }
                    else{
                        Log.d("Markers", "New image, reading from database")
                        val onImageReady = object : ImageReadyCallback{
                            override fun callback(image: Bitmap) {
                                markers[markerEntry.key] = image
                                popupDialog.setContentView(R.layout.image_popup)
                                popupDialog.findViewById<ImageView>(R.id.imagePopup).setImageBitmap(image)
                                popupDialog.findViewById<ImageButton>(R.id.btnClose).setOnClickListener{ popupDialog.dismiss() }
                                popupDialog.show()
                            }
                        }
                        database.getImage(route_guid, markerEntry.key, onImageReady)
                    }
                    break;
                }
            }
            return true;
        }
        return false;
    }
}