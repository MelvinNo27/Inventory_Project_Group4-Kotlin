package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.myapplication.databinding.ActivityAdminBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdminDashboard : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding // ViewBinding instance
    private val handler = android.os.Handler()
    private lateinit var rootDatabaseRef: DatabaseReference

    private val updateDateTimeRunnable: Runnable = object : Runnable {
        override fun run() {
            updateDateTime()  // Call the method to update both date and time
            handler.postDelayed(this, 1000) // Update every second
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootDatabaseRef = FirebaseDatabase.getInstance().getReference("MyData") // Correct database reference

        // Initialize ViewBinding
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logOutButton.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        binding.Users.setOnClickListener {
            // Ensure that it is not triggering an unintended action
            startActivity(Intent(this, UserList::class.java))
            finish()

        }


        binding.Rooms.setOnClickListener {
            // Handle Category button click


        }

        binding.reports.setOnClickListener {
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
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity() // Close all activities and exit the app
    }

}

