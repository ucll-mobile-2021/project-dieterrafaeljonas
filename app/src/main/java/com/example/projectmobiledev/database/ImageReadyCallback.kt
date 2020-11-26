package com.example.projectmobiledev.database

import android.graphics.Bitmap

interface ImageReadyCallback {
    fun callback(image : Bitmap)
}