package com.example.projectmobiledev.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.example.projectmobiledev.Activity2
import com.example.projectmobiledev.R
import com.example.projectmobiledev.database.Database
import com.example.projectmobiledev.database.RoutesCallback
import com.example.projectmobiledev.home.Home
import com.example.projectmobiledev.login.LogIn
import com.example.projectmobiledev.pathFinder.PathFinder
import com.example.projectmobiledev.routesViewer.RoutesViewer
import com.example.projectmobiledev.tracker.Route
import com.example.projectmobiledev.tracker.Tracker
import com.example.projectmobiledev.tracker.TrackerModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import kotlinx.android.synthetic.main.tracker.drawerLayout
import kotlinx.android.synthetic.main.tracker.nav_view
import kotlin.math.round

class Profile : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var toggle : ActionBarDrawerToggle
    private val controller : ProfileController = ProfileController()
    private val database = Database()
    private var route : Route = Route()
    private lateinit var polyLineOptions : PolylineOptions
    private lateinit var map : GoogleMap
    private lateinit var line : Polyline

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        /*
        ##########################
        ## Setten van main info ##
        ##########################
         */
        val username : TextView = findViewById(R.id.username)
        val profilepic : ImageView = findViewById(R.id.profilepicture)
        username.text = controller.getUserEmail()
        val transformation: Transformation = RoundedTransformationBuilder().cornerRadiusDp(200f).oval(true).build()
        Picasso.get().load(controller.getUserProfilePictureURI()).fit().transform(transformation).into(profilepic)

        /*
        ########################
        ## Setten van totalen ##
        ########################
         */

        val callback = object : RoutesCallback {
            override fun callback(routes: List<TrackerModel>) {
                setUp(routes);
            }
        }

        database.callback = callback;
        //database.getAll(callback, controller.getUserDBEmail())

        /*
        ##############################
        ## Setup voor de navigation ##
        ##############################
         */
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

        /*
        #######################
        ## Setup voor de map ##
        #######################
         */
        polyLineOptions = PolylineOptions()
        polyLineOptions.width(9f)
        polyLineOptions.color(Color.MAGENTA)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.longest_hike) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("SetTextI18n")
    fun setUp(routes: List<TrackerModel>) {
        val totW : TextView = findViewById(R.id.amount_of_hikes)
        val totKm : TextView = findViewById(R.id.total_kilometers)
        val longestHikeKm : TextView = findViewById(R.id.longest_hike_km)
        val longestHikeTime : TextView = findViewById(R.id.longest_hike_time)
        val longestHikeAvgSpeed : TextView = findViewById(R.id.longest_hike_avg_speed)
        route = getLongestRoute(routes)
        totW.text = routes.size.toString()
        totKm.text = getTotalKm(routes).round(3).toString()
        if (route.distance != -1.0) {
            val km = route.distance / 1000
            longestHikeKm.text = km.round(3).toString()
            longestHikeTime.text = route.elapsedTime.toString()
            longestHikeAvgSpeed.text = route.computeSpeed().round(3).toString() + " km/h"
        } else {
            longestHikeKm.text = 0.0.toString()
            longestHikeTime.text = 0.toString()
            longestHikeAvgSpeed.text = 0.toString() + " km/s"
        }
    }

    private fun Double.round(decimals: Int) : Double {
        var multiplier = 1.0
        repeat(decimals) {multiplier *= 10}
        return round(this * multiplier) / multiplier
    }

    private fun getTotalKm(routes: List<TrackerModel>) : Double {
        var distance = 0.0;
        for (r in routes) {
            distance += r.getTotalDistance()
        }
        return distance / 1000
    }

    private fun getLongestRoute(routes: List<TrackerModel>) : Route {
        if (routes.isNotEmpty()) {
            var res = routes[0]
            for (r in routes) {
                if (r.getTotalDistance() > res.getTotalDistance() && r.endDate != null) {
                    res = r
                }
            }
            return Route(res.getTotalDistance(), res.getElapsedTime(), res.getLocations(), res.name, res.guid)
        } else {
            return Route()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        if (route.distance != -1.0) {
            line = map.addPolyline(polyLineOptions)
            line.points = route.locations
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(route.getRouteCenter(), 18.0f))
        }
    }
}