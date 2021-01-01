package com.example.projectmobiledev

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import android.view.LayoutInflater

class NetworkListener(val context: Context?, val inflater : LayoutInflater) : ConnectivityManager.NetworkCallback() {

    override fun onLost(network: Network) {
        super.onLost(network)
        val popup = AlertDialog.Builder(context)
        val view = inflater.inflate(R.layout.internet_alert, null)
        popup.setView(view)
        popup.show()
    }
}