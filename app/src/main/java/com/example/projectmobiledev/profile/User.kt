package com.example.projectmobiledev.profile

import android.content.Intent
import android.net.Uri
import com.example.projectmobiledev.database.Database
import com.example.projectmobiledev.database.RoutesCallback
import com.example.projectmobiledev.tracker.RouteViewer
import com.example.projectmobiledev.tracker.TrackerModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class User {
    private val user = FirebaseAuth.getInstance().currentUser
    private val database = Database()

    fun getUserEmail() : String {
        if (user != null) {
            return user.email!!
        }
        throw UserNotFoundException("User not found!")
    }

    fun getUserEmailForDatabase() : String{
        var email = getUserEmail()
        email = email.replace(".","")
        email = email.replace("#","")
        email = email.replace("$","")
        email = email.replace("[","")
        email = email.replace("]","")
        return email
    }

    fun getUserProfilePictureURI() : Uri {
        if (user != null) {
            return user.photoUrl!!
        }
        throw UserNotFoundException("User not found!")
    }

    fun getWalkedKilometers() : Double {
        throw NotImplementedError()
    }
}