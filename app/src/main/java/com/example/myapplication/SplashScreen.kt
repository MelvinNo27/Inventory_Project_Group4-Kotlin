package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySplashScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SplashScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private val handler = Handler()
    private var progressStatus = 0
    private var loadingText = "LOADING"
    private val dots = arrayOf(".", "..", "...", "....")
    private var currentDotIndex = 0
    private val dotDelay = 500L
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference

        // Start rotating dots animation on "LOADING..." text
        animateLoadingText()

        if (!isConnectedToInternet()) {
            showNoInternetAlert()
        } else {
            progressBar()
        }
    }

    private fun animateLoadingText() {
        val runnable = object : Runnable {
            override fun run() {
                binding.loadingText.text = "$loadingText${dots[currentDotIndex]}"
                currentDotIndex = (currentDotIndex + 1) % dots.size
                handler.postDelayed(this, dotDelay)
            }
        }
        handler.post(runnable)
    }

    private fun progressBar() {
        Thread {
            while (progressStatus < 100) {
                progressStatus += 1
                handler.post {
                    binding.progressBarr.progress = progressStatus
                }
                Thread.sleep(30)
            }
            handler.post {
                checkInternetAndUserStatus()
            }
        }.start()
    }

    private fun checkInternetAndUserStatus() {
        if (isConnectedToInternet()) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                // User is logged in; check their role
                checkAdmin(currentUser.uid)
            } else {
                // No user is logged in; navigate to login
                goToLogin()
            }
        } else {
            showNoInternetAlert()
        }
    }

    private fun checkAdmin(userId: String) {
        // Check first in the "admins" node
        val adminRef = database.child("admins").child(userId)

        adminRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // User is found in the "admins" node, treat as Admin
                    val admin = snapshot.getValue(Admin::class.java)
                    if (admin != null) {
                        // Store role locally for future sessions
                        storeUserRoleLocally(admin.role)
                        // Navigate to the Admin Dashboard
                        goToAdminDashboard()
                    }

                } else {
                    // User not found in "admins", check the "users" node
                    checkUser(userId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SplashScreen, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                goToLogin()
            }
        })
    }

    private fun checkUser(userId: String) {
        // Check in the "users" node if not found in "admins"
        val userRef = database.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // User is found in the "users" node, treat as User
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        // Store role locally for future sessions
                        storeUserRoleLocally(user.role)
                        // Navigate to the User Dashboard
                        goToUserDashboard()
                    }
                } else {
                    // If the user is not found in either "admins" or "users", go to login
                    goToLogin()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SplashScreen, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                goToLogin()
            }
        })
    }

    private fun storeUserRoleLocally(role: String) {
        val editor = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE).edit()
        editor.putString("userRole", role)
        editor.apply()
    }

    private fun isConnectedToInternet(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun goToAdminDashboard() {

        startActivity(Intent(this, AdminDashboard::class.java))
        finish()
    }

    private fun goToUserDashboard() {

        startActivity(Intent(this, Userdashboard::class.java))
        finish()
    }

    private fun goToLogin() {

        startActivity(Intent(this, Login::class.java))
        finish()
    }

    private fun showNoInternetAlert() {
        Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("No Internet Connection")
            .setMessage("Please check your internet connection and try again.")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                openWifiSettings()
            }
            .create()
        alertDialog.show()
    }

    private fun openWifiSettings() {
        val intent = Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
        startActivity(intent)
    }
}

