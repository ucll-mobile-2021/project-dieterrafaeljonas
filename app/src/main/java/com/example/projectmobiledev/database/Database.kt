package com.example.projectmobiledev.database
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.projectmobiledev.Time
import com.example.projectmobiledev.profile.User
import com.example.projectmobiledev.tracker.TrackerModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
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

    fun getAll(callback: RoutesCallback, user : String){
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                if (data.exists()) {
                    val routes = mutableListOf<TrackerModel>()
                    for (child in data.children) {
                        val route = TrackerModel()
                        val email: String =  child.child("userEmail").value.toString()
                        val locations = readLocations(child.child("locations"))
                        val markers_database = readLocations(child.child("markers"))
                        val markers = mutableMapOf<LatLng,Bitmap?>()
                        for (i in markers_database){
                            markers.put(i,null)
                        }
                        // val totalDistance : Double = child.child("totalDistance").value as Double
                        val startDate : Time = Time(child.child("startDate").child("time").value as Long)
                        val endDate : Time = Time(child.child("endDate").child("time").value as Long)
                        val guid : UUID = UUID(child.child("guid").child("mostSignificantBits").value as Long,child.child("guid").child("leastSignificantBits").value as Long )
                        route.userEmail = email
                        route.endDate = java.sql.Time(endDate.milliseconds)
                        route.startDate = java.sql.Time(startDate.milliseconds)
                        route.guid = guid
                        route.calculateDistance() // this should yield the same result as just setting the totaldistance
                        route.setLocations(locations)
                        route.setMarkers(markers)
                        routes.add(route)
                    }
                    callback.callback(routes)
                }
            }

            override fun onCancelled(data: DatabaseError) {
                println("Error occurred while reading users data")
            }
        }
        database.getReference("/Routes/${user}").addValueEventListener(valueEventListener)
    }

    private fun decodeString(refName: String): LatLng {
        val mainstring = refName.split(".JPG")[0]
        val important = mainstring.split("_")
        val guid = important[1]
        val lat = important[2]
        val lng = important[3]
        return LatLng(lat.toDouble(), lng.toDouble() )
    }

    fun readLocations(locationsChild : DataSnapshot) : MutableList<LatLng> {
        val locations  = mutableListOf<LatLng>()
        for (i in 0 until locationsChild.childrenCount){
            val latlngChild = locationsChild.child(i.toString())
            val latLng : LatLng = LatLng(latlngChild.child("latitude").value as Double, latlngChild.child("longitude").value as Double)
            locations.add(latLng)
        }
        return locations
    }

    fun writeRoute(route: TrackerModel) {
        val user = User()
        val routeUser = database.getReference("/Routes").child(user.getUserEmailForDatabase())

        routeUser.child(route.guid.toString()).child("userEmail").setValue(route.userEmail)
        routeUser.child(route.guid.toString()).child("locations").setValue(route.getLocations())
        routeUser.child(route.guid.toString()).child("totalDistance").setValue(route.getTotalDistance())
        routeUser.child(route.guid.toString()).child("startDate").setValue(route.startDate)
        routeUser.child(route.guid.toString()).child("endDate").setValue(route.endDate)
        routeUser.child(route.guid.toString()).child("guid").setValue(route.guid)
        routeUser.child(route.guid.toString()).child("markers").setValue(route.getAllMarkers().map { marker -> marker.key })

        // write markers with bitmaps
        val images  = storage.getReference("/Images")
        val currentRouteImages = images.child(route.guid.toString())

        for ((k,v) in route.getAllMarkers()){
            if (v != null){
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
            }
        }
    }

    fun getImage(routeGuid: String, position : LatLng, onImageReady: ImageReadyCallback) {
        val image = "PromenApp_${routeGuid}_${position.latitude}_${position.longitude}.JPG"
        val routeReference = storage.reference.child("/Images/${routeGuid}/${image}")
        val ONE_MEGABYTE : Long = 1024 * 1024
        routeReference.getBytes(ONE_MEGABYTE).addOnSuccessListener { bytes ->
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0 , bytes.size)
            onImageReady.callback(bitmap)
        }
    }

    private fun readImagesAndMarkers(route: TrackerModel) {
        val routeReference = storage.reference.child("/Images/${route.guid}")
        routeReference.listAll().addOnSuccessListener { items ->
            var markerCount = 0
            val markers = mutableMapOf<LatLng,Bitmap>()
            val tasks = mutableListOf<Task<ByteArray>>()
            for (item in items.items){
                val ONE_MEGABYTE : Long = 1024 * 1024
                val task = item.getBytes(ONE_MEGABYTE)
                task.addOnSuccessListener {bytes ->
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val latLng = decodeString(item.toString())
                    markers.put(latLng,bitmap)
                }
                tasks.add(task)
            }
            println("all markers have been read")
        }
    }



}