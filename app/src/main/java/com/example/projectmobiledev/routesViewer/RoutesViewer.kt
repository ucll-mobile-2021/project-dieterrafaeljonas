package com.example.projectmobiledev.routesViewer

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginTop
import android.widget.LinearLayout.LayoutParams
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectmobiledev.Activity2
import com.example.projectmobiledev.R
import com.example.projectmobiledev.Time
import com.example.projectmobiledev.database.Database
import com.example.projectmobiledev.database.RoutesCallback
import com.example.projectmobiledev.home.Home
import com.example.projectmobiledev.login.LogIn
import com.example.projectmobiledev.pathFinder.PathFinder
import com.example.projectmobiledev.profile.Profile
import com.example.projectmobiledev.profile.ProfileController
import com.example.projectmobiledev.tracker.Route
import com.example.projectmobiledev.tracker.Tracker
import com.example.projectmobiledev.tracker.TrackerModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.routes_overview.*
import kotlinx.android.synthetic.main.tracker.*
import kotlinx.android.synthetic.main.tracker.drawerLayout
import kotlinx.android.synthetic.main.tracker.nav_view

class RoutesViewer : AppCompatActivity() {
    private lateinit var toggle : ActionBarDrawerToggle
    private var database = Database()
    private val controller : ProfileController = ProfileController()
    private var hikes = mutableListOf<Route>()
    private lateinit var recyclerView : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.routes_overview)

        recyclerView = findViewById(R.id.RecyclerView)

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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun setUp(routes: List<TrackerModel>) {
        var new_routes = routes.sortedWith(compareBy { it.startDate })
        new_routes = new_routes.filter { it.endDate != null }
        for (route in new_routes) {
                this.hikes.add(Route(route.getTotalDistance(), route.getElapsedTime(), route.getLocations(), route.name, route.guid))
        }
        val adapter = RecyclerViewAdapter(this, hikes, new_routes)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
}