package com.example.projectmobiledev

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Permissions {



    companion object Permissions // static in java
    {
        val LOCATION = 1
        fun checkLocationPermission(context : AppCompatActivity): Boolean {
            return (ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        }

        fun askLocationPermission(context: AppCompatActivity){
            ActivityCompat.requestPermissions(context, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),LOCATION);
        }
    }

}