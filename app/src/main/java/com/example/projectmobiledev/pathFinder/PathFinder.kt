package com.example.projectmobiledev.pathFinder

import `in`.blogspot.kmvignesh.googlemapexample.GoogleMapDTO
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.example.projectmobiledev.Activity2
import com.example.projectmobiledev.Permissions
import com.example.projectmobiledev.R
import com.example.projectmobiledev.home.Home
import com.example.projectmobiledev.login.LogIn
import com.example.projectmobiledev.profile.Profile
import com.example.projectmobiledev.routesViewer.RoutesViewer
import com.example.projectmobiledev.tracker.Tracker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.tracker.*
import okhttp3.OkHttpClient
import okhttp3.Request

class PathFinder : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    lateinit var map: GoogleMap
    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var pointFrom: LatLng
    private lateinit var pointTo: LatLng
    private var firstPointSelected: Boolean = false
    private var firstPathMade: Boolean = false
    private lateinit var pathMode: String
    private lateinit var locationProvider : FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pathfinder)
        // setup map fragment and get notified when the map is ready to use
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //setting the spinner content to choose cycling walking or driving
        val pathfinderMethods = resources.getStringArray(R.array.pathfinder_methods)
        val spinner = findViewById<Spinner>(R.id.spinner1)
        if(spinner != null){
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, pathfinderMethods)
            spinner.adapter = adapter
        }

        //determining what happens when clicking on the spinner contents
        spinner.onItemSelectedListener = object :
        AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                pathMode = pathfinderMethods[position]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                pathMode = "driving"
            }
        }
        locationProvider = LocationServices.getFusedLocationProviderClient(this)
        val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if(Permissions.checkLocationPermission(this)){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10.0f,this)
        }else{
            Permissions.askLocationPermission(this)
        }
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
                R.id.PathFinder -> startActivity(Intent(this,PathFinder::class.java))
                R.id.RoutesOverview -> startActivity(Intent(this, RoutesViewer::class.java))
                R.id.LogOut -> {
                    Firebase.auth.signOut()
                    startActivity(Intent(this, LogIn::class.java))
                }
            }
            true
        }
    }



    fun getDirectionURL(origin:LatLng,dest:LatLng) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=${pathMode}&key=AIzaSyAa5lqRLaC2jS8fR_IhGgvwVhxk3p2aPCs"
    }

    private inner class GetDirection(val url : String) : AsyncTask<Void, Void, List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body()!!.string()
            Log.d("GoogleMap" , " data : $data")
            val result =  ArrayList<List<LatLng>>()
            try{
                val respObj = Gson().fromJson(data, GoogleMapDTO::class.java)

                val path =  ArrayList<LatLng>()

                for (i in 0..(respObj.routes[0].legs[0].steps.size-1)){
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }catch (e:Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.BLUE)
                lineoption.geodesic(true)
            }
            map.addPolyline(lineoption)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private fun decodePolyline(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        //zet listener op om te luisteren naar taps
        map.setOnMapClickListener {
            handleClickOnMap(it)
        }
    }

    //handle de tab op de map
    private fun handleClickOnMap(it: LatLng) {
        map.addMarker(MarkerOptions().position(it))
        //bij het selecteren van het eerste punt dit punt opslaan
        if(!firstPointSelected){
            //if this is the second time the path is calculated we want to deleted the old path
            if(firstPathMade){
               lineoption.remove()
            }
            pointFrom = LatLng(it.latitude, it.longitude)
            firstPointSelected = true
        }
        //bij het selecteren van het tweede het pad tusen de 2 geven
        else{
            pointTo = LatLng(it.latitude, it.longitude)
            var URL = getDirectionURL(pointFrom,pointTo)
            GetDirection(URL).execute()
            firstPointSelected = false
            //So we now a path was made before
            if(!firstPathMade)
            {
                firstPathMade = true
            }
        }
    }

    override fun onLocationChanged(p0: Location) {
        //doe tot nu toe nix

    }
}