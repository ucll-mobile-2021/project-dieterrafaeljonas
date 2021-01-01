package com.example.projectmobiledev.pathFinder

import `in`.blogspot.kmvignesh.googlemapexample.GoogleMapDTO
import android.app.AlertDialog
import android.app.SearchableInfo
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.*
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.projectmobiledev.NetworkListener
import com.example.projectmobiledev.Permissions
import com.example.projectmobiledev.R
import com.example.projectmobiledev.database.Database
import com.example.projectmobiledev.home.Home
import com.example.projectmobiledev.login.LogIn
import com.example.projectmobiledev.profile.Profile
import com.example.projectmobiledev.routesViewer.RoutesViewer
import com.example.projectmobiledev.tracker.Tracker
import com.example.projectmobiledev.tracker.TrackerModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.pathfinder.*
import kotlinx.android.synthetic.main.tracker.*
import kotlinx.android.synthetic.main.tracker.drawerLayout
import kotlinx.android.synthetic.main.tracker.nav_view
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.time.LocalDateTime
import java.time.Month
import java.util.*
import kotlin.collections.ArrayList

class PathFinder : AppCompatActivity(), OnMapReadyCallback,  ActivityCompat.OnRequestPermissionsResultCallback {

    lateinit var map: GoogleMap
    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var startPoint: LatLng
    private lateinit var endPoint: LatLng
    private lateinit var pointFromForMethod : LatLng
    private var firstPointSelected: Boolean = false
    private var firstTwoPointsAlreadySelectedInPath = false
    private lateinit var pathMode: String
    private lateinit var locationProvider : FusedLocationProviderClient
    private lateinit var wayPoints : ArrayList<Marker>
    private lateinit var searchResults : ArrayList<Marker>
    private lateinit var searchView : SearchView
    private lateinit var addresList : List<Address>
    private lateinit var geocoder: Geocoder
    private lateinit var address: Address
    private lateinit var positionFound : LatLng
    private val routeToStore = mutableListOf<LatLng>()
    private var indexRoute = 0
    private val database: Database = Database()
    private lateinit var storeButton: FloatingActionButton
    private lateinit var cancelButton : FloatingActionButton
    private var lookedUpOnce : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
//        if(!isOnline(this)){
//            val inflater = layoutInflater
//            val popup = AlertDialog.Builder(this@PathFinder)
//            val view = inflater.inflate(R.layout.internet_alert, null)
//            popup.setView(view)
//            popup.show()
//        }

        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val networkListener = NetworkListener(this,layoutInflater)
        connectivityManager.registerDefaultNetworkCallback(networkListener)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.pathfinder)
        // setup map fragment and get notified when the map is ready to use
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //setting the spinner content to choose cycling walking or driving
        val pathfinderMethods = resources.getStringArray(R.array.pathfinder_methods)
        val spinner = findViewById<Spinner>(R.id.spinner1)
        if (spinner != null) {
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, pathfinderMethods)
            spinner.adapter = adapter
        }

        //determining what happens when clicking on the spinner contents
        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                pathMode = pathfinderMethods[position]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                pathMode = "driving"
            }
        }
        locationProvider = LocationServices.getFusedLocationProviderClient(this)
        val locationManager: LocationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager
        if (!Permissions.checkLocationPermission(this)) {
            Permissions.askLocationPermission(this)
        } else {
            locationProvider.lastLocation.addOnSuccessListener {
                if (it != null) {
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                it.latitude,
                                it.longitude
                            ), 18f
                        )
                    )
                }
            }
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
            when (it.itemId) {
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
        //initialise the waypoint marker array
        wayPoints = ArrayList<Marker>()

        //looks up the stuff and gives you the location
        searchResults = ArrayList<Marker>()
        searchView = findViewById(R.id.sv_location)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextSubmit(p0: String?): Boolean {
                if (!lookedUpOnce) {
                    if (p0 != null || p0 != "") {
                        geocoder = Geocoder(this@PathFinder)
                        try {
                            addresList = geocoder.getFromLocationName(p0, 1)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        if (!addresList.isEmpty()) {
                            address = addresList.get(0)
                            positionFound = LatLng(address.latitude, address.longitude)
                            pointFromForMethod = positionFound
                            if (!searchResults.isEmpty()) {
                                var marker = searchResults.last()
                                marker.remove()
                            }
                            searchResults.add(
                                map.addMarker(
                                    MarkerOptions().position(positionFound).title(p0)
                                )
                            )
                            map.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    positionFound,
                                    18f
                                )
                            )
                            firstPointSelected = true
                            lookedUpOnce = true
                            return true
                        } else {
                            val inflater = layoutInflater
                            val popup = AlertDialog.Builder(this@PathFinder)
                            val view = inflater.inflate(R.layout.look_up_alert2, null)
                            popup.setView(view)
                                .setNegativeButton(
                                    "Cancel",
                                    DialogInterface.OnClickListener { popup, _ ->
                                        popup.dismiss()
                                    })
                            popup.show()
                        }
                    } else {
                        return false
                    }
                } else {
                    val inflater = layoutInflater
                    val popup = AlertDialog.Builder(this@PathFinder)
                    val view = inflater.inflate(R.layout.look_up_alert, null)
                    popup.setView(view)
                        .setNegativeButton(
                            "Cancel",
                            DialogInterface.OnClickListener { popup, _ ->
                                popup.dismiss()
                            })
                    popup.show()
                }
                return false
            }
        }
        )
        //set on click listener for store
        storeButton = findViewById(R.id.btnSavePath)
        storeButton.setOnClickListener {
            val inflater = layoutInflater
            val popup = AlertDialog.Builder(this)
            val view = inflater.inflate(R.layout.save_pathfinder, null)
            val editText = view.findViewById<EditText>(R.id.name_route)
            val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
            val date = Date(calendarView.getDate())
            calendarView.setOnDateChangeListener(object : CalendarView.OnDateChangeListener {
                override fun onSelectedDayChange(p0: CalendarView, p1: Int, p2: Int, p3: Int) {
                    date.date = p3
                    date.month = p2
                    date.year = p1 - 1900
                }
            })
            val timeView = view.findViewById<TimePicker>(R.id.timePicker1)
            popup.setView(view)
                .setPositiveButton("Save", DialogInterface.OnClickListener { popup, _ ->
                    if(editText.text.toString() != null && editText.text.toString() != "") {
                        if(!routeToStore.isEmpty()) {
                            date.hours = timeView.hour
                            date.minutes = timeView.minute
                            storeRoute(editText.text.toString(), date)
                            startActivity(Intent(this, Home::class.java))
                        }
                        else
                        {
                            val inflater3 = layoutInflater
                            val popup3 = AlertDialog.Builder(this)
                            val view3 = inflater3.inflate(R.layout.no_points_on_path, null)
                            popup3.setView(view3)
                                .setNegativeButton("Close",  DialogInterface.OnClickListener { popup3, _ ->
                                    popup3.dismiss()
                                })
                            popup3.show()
                        }
                    }
                    else
                    {
                        val inflater2 = layoutInflater
                        val popup2 = AlertDialog.Builder(this)
                        val view2 = inflater2.inflate(R.layout.no_name_given_error, null)
                        popup2.setView(view2)
                            .setNegativeButton("Close",  DialogInterface.OnClickListener { popup2, _ ->
                                popup2.dismiss()
                            })
                        popup2.show()
                    }
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { popup, _ ->
                    popup.dismiss()
                })
            popup.show()
        }

        //set om click listener for cancel
        cancelButton = findViewById(R.id.btnDeletePath)
        cancelButton.setOnClickListener {
            var home = Intent(this, Home::class.java).apply {}
            startActivity(home)
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
                path.forEach(){
                    routeToStore.add(indexRoute,it)
                    indexRoute++
                }
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
        if(!firstPointSelected){
            wayPoints.add(map.addMarker(MarkerOptions().position(it).title("StartPoint")))
            startPoint = LatLng(it.latitude, it.longitude)
            pointFromForMethod = startPoint
            firstPointSelected = true;
        }
        else{
            if(firstTwoPointsAlreadySelectedInPath){
                var marker = wayPoints.last()
                marker.remove()
                pointFromForMethod = endPoint
                endPoint = LatLng(it.latitude, it.longitude)
            }else {
                endPoint = LatLng(it.latitude, it.longitude)
                firstTwoPointsAlreadySelectedInPath = true;
            }
            wayPoints.add(map.addMarker(MarkerOptions().title("EndPoint").position(it)))
            var URL = getDirectionURL(pointFromForMethod, endPoint)
            //draw route between the 2 points
            GetDirection(URL).execute()

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.contentEquals(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)))
        {
            if (requestCode == Permissions.LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
               if(Permissions.checkLocationPermission(this))
               {
                   map.isMyLocationEnabled = true
                   locationProvider.lastLocation.addOnSuccessListener {
                       map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 18f))
                   }
               }
            }
        }
    }

    private fun storeRoute(route_name: String, date: Date){
        val trackerModelToStore = TrackerModel()
        trackerModelToStore.endDate = null
        trackerModelToStore.name = route_name
        trackerModelToStore.startDate = date
        val user = FirebaseAuth.getInstance().currentUser;
        trackerModelToStore.userEmail = user?.email!!
        trackerModelToStore.setLocations(routeToStore)
        database.writeRoute(trackerModelToStore)
    }

    //check for internet connection
    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }
}

