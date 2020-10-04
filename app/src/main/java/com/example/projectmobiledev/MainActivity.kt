package com.example.projectmobiledev

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import com.example.projectmobiledev.util.SwipeUtil
import maes.tech.intentanim.CustomIntent
import kotlin.math.abs

//Implementeerd ook GestureDetector om de swipe events te kunnen afhandelen
class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener {
    //Variabele van gestureDetector om de OnTouch methode te kunnen oproepen
    private val gestureDetector = GestureDetector(this)

    override fun onCreate (savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onShowPress(e: MotionEvent?) {
        //TO DO("Nog niet echt een functie voor, misschien in de toekomst")
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        //TO DO("Nog niet echt een functie voor, misschien in de toekomst")
        return false
    }

    //Nog niet echt een functie voor, misschien in de toekomst
    override fun onDown(e: MotionEvent?): Boolean {
        return false
    }

    //Methode die swipes afhandelt
    override fun onFling(
        downEvent: MotionEvent?,
        moveEvent: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        var result = false

        if (moveEvent != null && downEvent != null) {
            val yDiff = moveEvent.y - downEvent.y
            val xDiff = moveEvent.x - downEvent.x
            //Is het een swipe naar boven of onder?
            if (abs(xDiff) > abs(yDiff)) {
                //Is het swipewaardig? dwz is de afstand en de snelheid van de swipe hoog genoeg? Kan aangepast worden voor de gevoeligheid te vergroten/verkleinen
                if (abs(xDiff) > SwipeUtil.SWIPE_THRESHOLD && abs(velocityX) > SwipeUtil.VELOCITY_THRESHOLD) {
                    //Swipe naar rechts
                    if (xDiff > 0) {
                        startActivity(Intent(this, Activity2::class.java))
                        //Override de intent animatie naar een animatie die op een swipe lijkt
                        CustomIntent.customType(this, "right-to-left")
                        result = true
                    } else {
                        //Atm nog geen andere activity van uit het main scherm bij een linkse swipe
                        result = true
                    }
                }
            } else {
                //Kan gebruikt worden voor swipes naar boven/onder, misschien handig voor in de toekomst
            }
        }
        return result
    }

    //Override van de onTouchEvent methode om te delegeren naar de onTouch van de gestureDetector die door delegeert naar de onFling methode
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    //Nog niet echt een functie voor, misschien in de toekomst
    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        xVel: Float,
        yVel: Float
    ): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
        //TO DO("Nog niet echt een functie voor, misschien in de toekomst")
    }
}