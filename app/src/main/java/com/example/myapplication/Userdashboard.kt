package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.myapplication.databinding.ActivityUserBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class Userdashboard : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityUserBinding
    private val handler = android.os.Handler()

    private val updateDateTimeRunnable: Runnable = object : Runnable {
        override fun run() {
            updateDateTime()  // Call the method to update both date and time
            handler.postDelayed(this, 1000) // Update every second
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Get the user's name and display in the welcomeTextView
        val user = auth.currentUser
        if (user != null) {
            val displayName = user.displayName
            binding.welcomeTextView.text = "Hello Sir, $displayName!"
        }

        // Set onClickListener for the logout button (floatingAddButton in XML)
        binding.floatingAddButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        // Start updating the date and time
        handler.post(updateDateTimeRunnable)
    }

    private fun updateDateTime() {
        // Get current date and time
        val currentDate = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())

        // Update the TextViews with current date and time
        binding.dateTextView.text = currentDate
        binding.timeTextView.text = currentTime
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateDateTimeRunnable) // Stop updating when the activity is destroyed
    }

    // Method to open the navigation drawer
    fun openDrawer(view: View) {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }
}
