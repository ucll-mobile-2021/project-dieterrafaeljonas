package com.example.projectmobiledev.profile

import android.net.Uri
import kotlin.system.exitProcess

class ProfileController {
    private val model: User = User()

    fun getUserEmail() : String {
        try {
            return model.getUserEmail()
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

    fun getUserWalkedKilometers() : Float {
        throw NotImplementedError()
    }

    fun getUserWalkedHikes() : Int {
        throw NotImplementedError()
    }

    fun getUserLongestHike() {
        throw NotImplementedError()
    }
}