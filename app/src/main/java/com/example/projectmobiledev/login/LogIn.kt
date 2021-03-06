package com.example.projectmobiledev.login

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projectmobiledev.NetworkListener
import com.example.projectmobiledev.R
import com.example.projectmobiledev.database.Database
import com.example.projectmobiledev.database.RoutesCallback
import com.example.projectmobiledev.home.Home
import com.example.projectmobiledev.profile.User
import com.example.projectmobiledev.tracker.RouteViewer
import com.example.projectmobiledev.tracker.Tracker
import com.example.projectmobiledev.tracker.TrackerModel
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.log_in.*
import kotlinx.android.synthetic.main.profile.*
import org.json.JSONObject
import kotlin.math.log

class LogIn : AppCompatActivity() {
    private lateinit var gso: GoogleSignInOptions
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private val RC_SIGN_IN = 123

/*    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        startActivity(Intent(this, Tracker::class.java))
    }*/

    override fun onBackPressed() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val networkListener = NetworkListener(this,layoutInflater)
        connectivityManager.registerDefaultNetworkCallback(networkListener)
        networkListener.isOnline(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_in)

        auth = FirebaseAuth.getInstance()

        // Configure Google Sign In
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        google_button.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d("auth", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("auth", "Google sign in failed" + e.message, e)
                // ...
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("auth", "signInWithCredential:success")
                    val user = auth.currentUser
                    startNextActivity(this)
                    //startActivity(Intent(this, Tracker::class.java))
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("auth", "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun startNextActivity(logIn: LogIn) {

        // code to go to a routeViewer
//        val callback = object : RoutesCallback {
//            override fun callback(routes: List<TrackerModel>) {
//                if (routes.isNotEmpty()){
//                    val intent = Intent(logIn,RouteViewer::class.java)
//                    val json = routes[0].toJson()
//                    intent.putExtra("route", json)
//                    startActivity(intent)
//                }
//            }
//        }
//        val database = Database()
//        val user = User()
//        database.getAll(callback, user.getUserEmailForDatabase())
        startActivity(Intent(logIn,Home::class.java))
    }
}