package com.example.projectmobiledev.home

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectmobiledev.Activity2
import com.example.projectmobiledev.NetworkListener
import com.example.projectmobiledev.R
import com.example.projectmobiledev.Time
import com.example.projectmobiledev.database.Database
import com.example.projectmobiledev.database.RoutesCallback
import com.example.projectmobiledev.login.LogIn
import com.example.projectmobiledev.pathFinder.PathFinder
import com.example.projectmobiledev.profile.Profile
import com.example.projectmobiledev.profile.UserNotFoundException
import com.example.projectmobiledev.routesViewer.RecyclerViewAdapter
import com.example.projectmobiledev.routesViewer.RoutesViewer
import com.example.projectmobiledev.tracker.Route
import com.example.projectmobiledev.tracker.Tracker
import com.example.projectmobiledev.tracker.TrackerModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import kotlinx.android.synthetic.main.tracker.*
import java.time.Instant.now
import java.time.LocalDate.now
import java.util.*
import kotlin.system.exitProcess


class Home : AppCompatActivity() {
    private lateinit var toggle: ActionBarDrawerToggle
    private val controller: HomeController = HomeController()
    private var database = Database()
    private var hikes = mutableListOf<Route>()
    private lateinit var recyclerView : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val networkListener = NetworkListener(this,layoutInflater)
        connectivityManager.registerDefaultNetworkCallback(networkListener)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        val username : TextView = findViewById(R.id.username)
        username.text = "Welcome to your PromenApp," + "\n" + controller.getUserEmail() + "!"

        val intro : TextView = findViewById(R.id.intro)
        intro.text = "On this homepage, you will be able to see your planned routes and: add them to your calendar, open the route tracker for that route, or remove the planned routes. "

        recyclerView = findViewById(R.id.RecyclerViewHome)

        val callback = object : RoutesCallback {
            override fun callback(routes: List<TrackerModel>) {
                setUp(routes);
            }
        }

        database.callback = callback;
        //database.getAll(callback, controller.getUserEmailForDatabase())

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

    fun setUp(routes: List<TrackerModel>) {
        var new_routes = routes.sortedWith(compareBy { it.startDate })
        new_routes = new_routes.filter { it.endDate == null}
        val adapter = RecyclerViewAdapterHome(this, new_routes, hikes)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
    }