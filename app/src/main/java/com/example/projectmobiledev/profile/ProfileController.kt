package com.example.projectmobiledev.profile

import android.net.Uri
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.system.exitProcess

class ProfileController {
    private val model: User = User()
    private val database: DatabaseReference = Firebase.database.reference

    fun getUserEmail() : String {
        try {
            return model.getUserEmail()
        } catch (e: UserNotFoundException) {
            exitProcess(0)
        }
    }

    fun getUserDBEmail() : String {
        try {
            return model.getUserEmailForDatabase()
        } catch (e: UserNotFoundException) {
            exitProcess(0)
        }
    }

    fun getUserProfilePictureURI() : Uri {
        try {
            return model.getUserProfilePictureURI()
        } catch (e: UserNotFoundException) {
            exitProcess(0)
        }
    }

    fun getUserWalkedKilometers() : Double {
        return model.getWalkedKilometers()
    }

    fun getUserLongestHike() {
        throw NotImplementedError()
    }
}