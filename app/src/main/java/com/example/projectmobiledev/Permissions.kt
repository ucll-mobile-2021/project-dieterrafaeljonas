package com.example.projectmobiledev

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Permissions {



    companion object Permissions // static in java
    {
        val LOCATION = 1
        val READ_EXTERNAL_STORAGE = 2
        val WRITE_EXTERNAL_STORAGE = 3
        fun checkLocationPermission(context : AppCompatActivity): Boolean {
            return (ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        }

        fun askLocationPermission(context: AppCompatActivity){
            ActivityCompat.requestPermissions(context, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),LOCATION);
        }

        fun checkReadExternalStoragePermission(context : AppCompatActivity): Boolean {
            return (ContextCompat.checkSelfPermission(context,android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        }

        fun askReadExternalStoragePermission(context: AppCompatActivity){
            ActivityCompat.requestPermissions(context, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE);
        }

        fun checkWriteExternalStoragePermission(context : AppCompatActivity): Boolean {
            return (ContextCompat.checkSelfPermission(context,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        }

        fun askWriteExternalStoragePermission(context: AppCompatActivity){
            ActivityCompat.requestPermissions(context, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE);
        }
    }

}