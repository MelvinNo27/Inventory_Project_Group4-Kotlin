package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.myapplication.databinding.ActivityAdminBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminDashboard : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding // ViewBinding instance
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
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.floatingAddButton.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        binding.Users.setOnClickListener {
            // Handle Users button click
            startActivity(Intent(this, UserList::class.java))
        }

        binding.Rooms.setOnClickListener {
            // Handle Category button click


        }

        binding.Reports.setOnClickListener {
            // Handle Products button click


        }

        // Start the update of date and time
        handler.post(updateDateTimeRunnable)
    }

    private fun updateDateTime() {
        // Get current date and time
        val currentDate = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())

        // Update the TextViews
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
