package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminDashboard : AppCompatActivity() {
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
        setContentView(R.layout.activity_admin)
        val logoutButton = findViewById<TextView>(R.id.floatingAddButton)
        val btnUsers = findViewById<ImageView>(R.id.btnUsers)
        val btnCategory = findViewById<ImageView>(R.id.btnCategories)
        val btnProducts = findViewById<ImageView>(R.id.btnCurrentStocks)


        // Initialize the DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout)
        dateTextView = findViewById(R.id.dateTextView)
        timeTextView = findViewById(R.id.timeTextView)


        logoutButton.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }


        btnUsers.setOnClickListener {
            // Handle Users button click
        }

        btnCategory.setOnClickListener {
            // Handle Category button click
        }

        btnProducts.setOnClickListener {
            // Handle Products button click
        }

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
