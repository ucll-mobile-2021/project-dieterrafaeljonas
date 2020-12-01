package com.example.projectmobiledev.home

import android.net.Uri
import com.example.projectmobiledev.profile.UserNotFoundException
import com.google.firebase.auth.FirebaseAuth

class HomeController {
    private val user = FirebaseAuth.getInstance().currentUser

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
}