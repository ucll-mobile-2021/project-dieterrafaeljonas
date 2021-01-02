package com.example.projectmobiledev

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import android.view.LayoutInflater

class NetworkListener(val context: Context?, val inflater : LayoutInflater) : ConnectivityManager.NetworkCallback() {

    override fun onLost(network: Network) {
        super.onLost(network)
        isOnline(context!!)
    }

    fun isOnline(context: Context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                }
            } else {
                val popup = AlertDialog.Builder(context)
                    .setPositiveButton("Okay, I'm connected again", DialogInterface.OnClickListener { _, _ ->
                        isOnline(context)
                    }).create()
                val view = inflater.inflate(R.layout.internet_alert, null)
                popup.setCanceledOnTouchOutside(false)
                popup.setView(view)
                popup.show()
            }
        }
    }
}