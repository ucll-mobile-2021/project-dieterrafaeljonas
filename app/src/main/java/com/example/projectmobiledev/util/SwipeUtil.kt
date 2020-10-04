package com.example.projectmobiledev.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.example.projectmobiledev.Activity2

//Simpele hulpklasse die de tresholds van de swipes bevat als globale variabelen
//Misschien swipeRight() en swipeLeft() methodes hier in verplaatsen in de toekomst

class SwipeUtil {
    companion object {
        const val SWIPE_THRESHOLD = 100
        const val VELOCITY_THRESHOLD = 100
    }
}