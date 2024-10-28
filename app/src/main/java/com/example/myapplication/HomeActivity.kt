package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private val handler = android.os.Handler()

    private val updateDateTimeRunnable: Runnable = object : Runnable {
        override fun run() {
            updateDateTime()  // Call the method to update both date and time
            handler.postDelayed(this, 1000) // Update every second
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize Firebase Auth
        val auth = FirebaseAuth.getInstance()

        // Find the logout button
        val logoutButton = findViewById<TextView>(R.id.floatingAddButton)

        // Initialize the DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout)

        // Initialize TextViews for date and time
        dateTextView = findViewById(R.id.dateTextView)
        timeTextView = findViewById(R.id.timeTextView)

        // Get the user's name
        val user = auth.currentUser
        val welcomeTextView = findViewById<TextView>(R.id.welcomeTextView)

        // Display the user's name
        if (user != null) {
            val displayName = user.displayName
            welcomeTextView.text = "Hello Sir, $displayName!"
        }

        // Set onClickListener for the logout button
        logoutButton.setOnClickListener {
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

        // Update the TextViews
        dateTextView.text = currentDate
        timeTextView.text = currentTime
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateDateTimeRunnable) // Stop updating when the activity is destroyed
    }

    // Method to open the navigation drawer
    fun openDrawer(view: View) {
        drawerLayout.openDrawer(GravityCompat.START)
    }
}
