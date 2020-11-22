package com.example.projectmobiledev.database
import android.graphics.Bitmap
import android.util.Log
import com.example.projectmobiledev.Time
import com.example.projectmobiledev.tracker.TrackerModel
import com.google.android.gms.maps.model.LatLng
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
                        val email: String =  child.child("userEmail").value.toString()
                        //val locations : MutableList<LatLng> = child.child("locations") as MutableList<LatLng>
                        val locations = readLocations(child)
                        val totalDistance : Double = child.child("totalDistance").value as Double
                        val startDate : Time = Time(child.child("startDate").child("time").value as Long)
                        val endDate : Time = Time(child.child("endDate").child("time").value as Long)
                        val guid : UUID = UUID(child.child("guid").child("mostSignificantBits").value as Long,child.child("guid").child("leastSignificantBits").value as Long )
                        val route = TrackerModel()
                        route.userEmail = email
                        route.endDate = java.sql.Time(endDate.milliseconds)
                        route.startDate = java.sql.Time(startDate.milliseconds)
                        route.guid = guid
                        route.calculateDistance() // this should yield the same result as just setting the totaldistance
                        route.setLocations(locations)
                        routes.add(route)
                        //val route = child.getValue(TrackerModel::class.java)
//                        if (route != null)
//                            routes.add(route)
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

    fun readLocations(child : DataSnapshot) : MutableList<LatLng> {
        val locations  = mutableListOf<LatLng>()
        val locationsChild = child.child("locations")
        for (i in 0 until locationsChild.childrenCount){
            val latlngChild = locationsChild.child(i.toString())
            val latLng : LatLng = LatLng(latlngChild.child("latitude").value as Double, latlngChild.child("longitude").value as Double)
            locations.add(latLng)
        }
        return locations
    }

    fun writeRoute(route: TrackerModel) {
        val routes = database.getReference("/Routes")
        // let firebase handle the basic types such
        // normally this would work but the map with bitmaps is throwing errors even after the @Exclude
        // routes.child(route.guid.toString()).setValue(route)
        routes.child(route.guid.toString()).child("userEmail").setValue(route.userEmail)
        routes.child(route.guid.toString()).child("locations").setValue(route.getLocations())
        routes.child(route.guid.toString()).child("totalDistance").setValue(route.getTotalDistance())
        routes.child(route.guid.toString()).child("startDate").setValue(route.startDate)
        routes.child(route.guid.toString()).child("endDate").setValue(route.endDate)
        routes.child(route.guid.toString()).child("guid").setValue(route.guid)

        // write markers with bitmaps
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