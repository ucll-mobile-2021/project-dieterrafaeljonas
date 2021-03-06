package com.example.projectmobiledev.database
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.projectmobiledev.Time
import com.example.projectmobiledev.profile.User
import com.example.projectmobiledev.tracker.TrackerModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.*
import java.io.ByteArrayOutputStream
import java.lang.Error
import java.lang.Exception
import java.time.Instant
import java.util.*

class Database() : ValueEventListener {
    private val database = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance()
    val user = User()
    var callback : RoutesCallback? = null
    set(value){
        field = value
        database.getReference("/Routes/${user.getUserEmailForDatabase()}").addValueEventListener(this)
    }


//    fun getAll(callback: RoutesCallback, user : String){
//        val valueEventListener = object : ValueEventListener {
//            override fun onDataChange(data: DataSnapshot) {
//                if (data.exists()) {
//                    val routes = mutableListOf<TrackerModel>()
//                    for (child in data.children) {
//                        val route = TrackerModel()
//                        val email: String =  child.child("userEmail").value.toString()
//                        val locations = readLocations(child.child("locations"))
//                        val markers_database = readLocations(child.child("markers"))
//                        val markers = mutableMapOf<LatLng,Bitmap?>()
//                        for (i in markers_database){
//                            markers.put(i,null)
//                        }
//                        // val totalDistance : Double = child.child("totalDistance").value as Double
//                        var startDate : Date = Calendar.getInstance().time
//                        if (child.child("startDate").exists() && child.child("startDate").child("time").exists()){
//                            startDate = Date(child.child("startDate").child("time").value as Long)
//                        }
//                        var endDate : Date? = null;
//                        if (child.child("endDate").exists()){
//                            endDate = Date(child.child("endDate").child("time").value as Long)
//                        }
//                        var guid : UUID? = null
//                        if (child.child("guid").exists()){
//                            guid = UUID(child.child("guid").child("mostSignificantBits").value as Long,child.child("guid").child("leastSignificantBits").value as Long )
//                        }
//                        var name = ""
//                        if (child.child("name").exists()){
//                            name  = child.child("name").value as String
//                        }
//                        route.userEmail = email
//                        route.name = name
//                        route.endDate = endDate
//                        route.startDate = startDate!!
//                        route.guid = guid
//                        route.setLocations(locations)
//                        route.setMarkers(markers)
//                        route.calculateDistance() // this should yield the same result as just setting the totaldistance
//                        routes.add(route)
//                    }
//                    callback.callback(routes)
//                } else {
//                    callback.callback(mutableListOf())
//                }
//            }
//
//            override fun onCancelled(data: DatabaseError) {
//                println("Error occurred while reading users data")
//            }
//        }
//        database.getReference("/Routes/${user}").addValueEventListener(valueEventListener)
//    }

    fun removeRoute(guid : UUID?){
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                // remove roue
                if (guid != null){
                    if (data.exists()){
                        data.ref.removeValue()
                    }
                    removeImages(guid)
                }
            }

            override fun onCancelled(data: DatabaseError) {
                println("Error occurred while reading users data")
            }
        }
        database.getReference("/Routes/${user.getUserEmailForDatabase()}/${guid.toString()}").addValueEventListener(valueEventListener)
    }

    private fun removeImages(guid: UUID) {
        val ref = storage.getReference("/Images/${guid}")
        val errorhandeler = object : OnFailureListener{
            override fun onFailure(e: Exception) {
               val errorcode = (e as StorageException).errorCode
                if (errorcode == StorageException.ERROR_OBJECT_NOT_FOUND){
                    Log.e("images","Images not found")
                }
                else{
                    Log.e("images", "Random error $errorcode messaage:\n${e.message}")
                }
            }

        }
        ref.delete()
            .addOnFailureListener(errorhandeler)
            .addOnSuccessListener {
                Log.d("Storage delete","Removed images succesfully")
            }
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
            val latLng = LatLng(latlngChild.child("latitude").value as Double, latlngChild.child("longitude").value as Double)
            locations.add(latLng)
        }
        return locations
    }

    fun writeRoute(route: TrackerModel) {
        val routeUser = database.getReference("/Routes").child(user.getUserEmailForDatabase())

        routeUser.child(route.guid.toString()).child("userEmail").setValue(route.userEmail)
        routeUser.child(route.guid.toString()).child("locations").setValue(route.getLocations())
        routeUser.child(route.guid.toString()).child("totalDistance").setValue(route.getTotalDistance())
        routeUser.child(route.guid.toString()).child("startDate").setValue(route.startDate)
        routeUser.child(route.guid.toString()).child("endDate").setValue(route.endDate)
        routeUser.child(route.guid.toString()).child("guid").setValue(route.guid)
        routeUser.child(route.guid.toString()).child("markers").setValue(route.getAllMarkers().map { marker -> marker.key })
        routeUser.child(route.guid.toString()).child("name").setValue(route.name)

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
                        println("error has occured while writing to storage")
                        it.printStackTrace();
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
                var startDate : Date = Calendar.getInstance().time
                if (child.child("startDate").exists() && child.child("startDate").child("time").exists()){
                    startDate = Date(child.child("startDate").child("time").value as Long)
                }
                var endDate : Date? = null;
                if (child.child("endDate").exists()){
                    endDate = Date(child.child("endDate").child("time").value as Long)
                }
                var guid : UUID? = null
                if (child.child("guid").exists()){
                    guid = UUID(child.child("guid").child("mostSignificantBits").value as Long,child.child("guid").child("leastSignificantBits").value as Long )
                }
                var name = ""
                if (child.child("name").exists()){
                    name  = child.child("name").value as String
                }
                route.userEmail = email
                route.name = name
                route.endDate = endDate
                route.startDate = startDate
                route.guid = guid
                route.setLocations(locations)
                route.setMarkers(markers)
                route.calculateDistance() // this should yield the same result as just setting the totaldistance
                routes.add(route)
            }
            callback?.callback(routes)
        } else {
            callback?.callback(mutableListOf())
        }
    }

    override fun onCancelled(error: DatabaseError) {
        println("Error reading database")
    }
}

