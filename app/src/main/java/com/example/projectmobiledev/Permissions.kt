package com.example.projectmobiledev

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Permissions {

    companion object Permissions // static in java
    {
        fun checkLocationPermission(context : AppCompatActivity): Boolean {
            // check if we already have location permission
            if (ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                // if not ask for location permission
                ActivityCompat.requestPermissions(context, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1);
            }

            if (ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                return true
            return false
        }
    }

}