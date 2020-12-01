package com.example.projectmobiledev.home

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.example.projectmobiledev.Activity2
import com.example.projectmobiledev.R
import com.example.projectmobiledev.login.LogIn
import com.example.projectmobiledev.pathFinder.PathFinder
import com.example.projectmobiledev.profile.Profile
import com.example.projectmobiledev.tracker.Tracker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import kotlinx.android.synthetic.main.tracker.*


class Home : AppCompatActivity() {
    private lateinit var toggle: ActionBarDrawerToggle
    private val controller: HomeController = HomeController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        /*
        ##########################
        ## Setten van main info ##
        ##########################
         */
        val username : TextView = findViewById(R.id.username)
        //val profilepic : ImageView = findViewById(R.id.profilepicture)
        username.text = controller.getUserEmail()
        val transformation: Transformation = RoundedTransformationBuilder().cornerRadiusDp(200f).oval(true).build()
        //Picasso.get().load(controller.getUserProfilePictureURI()).fit().transform(transformation).into(profilepic)

        /*
        ########################
        ## Setten van totalen ##
        ########################
         */
        //val tot_km : TextView = findViewById(R.id.amount_of_hikes)
        //val tot_w : TextView = findViewById(R.id.total_kilometers)
        // Not implemented yet
        // tot_km.text = controller.getUserWalkedHikes().toString()
        // tot_w.text = controller.getUserWalkedHikes().toString()

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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}