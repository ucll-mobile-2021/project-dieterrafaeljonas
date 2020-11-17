package com.example.projectmobiledev.database
import android.graphics.Bitmap
import android.util.Log
import com.example.projectmobiledev.tracker.TrackerModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.*
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.*

class Database() {
    private val database = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getAll(callback: RoutesCallback){
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                if (data.exists()) {
                    val routes = mutableListOf<TrackerModel>()
                    for (child in data.children) {
                        val route = child.getValue(TrackerModel::class.java)
                        if (route != null)
                            routes.add(route)
                    }
                    callback.callback(routes)
                }
            }

            override fun onCancelled(data: DatabaseError) {
                println("Error occurred while reading users data")
            }
        }
        database.getReference("/Routes").addValueEventListener(valueEventListener)
    }

    fun writeRoute(route: TrackerModel) {
        val routes = database.getReference("/Routes")
        // let firebase handle the basic types such
        // normally this would work but the map with bitmpas is throwing errors even after the @Exclude
        // routes.child(route.guid.toString()).setValue(route)
        routes.child(route.guid.toString()).child("userEmail").setValue(route.userEmail)
        routes.child(route.guid.toString()).child("locations").setValue(route.getLocations())
        routes.child(route.guid.toString()).child("totalDistance").setValue(route.getTotalDistance())
        routes.child(route.guid.toString()).child("startDate").setValue(route.startDate)
        routes.child(route.guid.toString()).child("endDate").setValue(route.endDate)

        //TODO(write markers)
        val images  = storage.getReference("/Images")
        val currentRouteImages = images.child(route.guid.toString())

        var teller = 0
        for ((k,v) in route.getAllMarkers()){
            val value = "PromenApp_${route.guid}_${k.latitude}_${k.longitude}.JPG"
            val baos = ByteArrayOutputStream()
            v.compress(Bitmap.CompressFormat.JPEG,100,baos)
            val data = baos.toByteArray()
            currentRouteImages.child(value).putBytes(data)
                .addOnFailureListener{
                    Log.d("Storage", "something has gone wrong")
                }.addOnSuccessListener {
                    Log.d("Storage", "Image uploaded succesfully")
                }
            teller++
        }
    }


}