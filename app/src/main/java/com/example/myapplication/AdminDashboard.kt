package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.myapplication.databinding.ActivityAdminBinding
import com.google.firebase.auth.FirebaseAuth
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

        // Log out button click
        binding.logOutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Instructors button click
        binding.btnInstructors.setOnClickListener {
            startActivity(Intent(this, UserList::class.java))
            finish()
        }

        // Rooms button click
        binding.btnRooms.setOnClickListener {
            startActivity(Intent(this, SelectRooms::class.java))
            finish()
        }

        // Reports button click (you can add the functionality here if needed)
        binding.btnReports.setOnClickListener {
            // Add functionality for Reports here if needed
        }

        // Start the update of date and time every second
        handler.post(updateDateTimeRunnable)
    }

    private fun showLogoutConfirmationDialog() {
        // Create and show the confirmation dialog
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to log out?")
            .setCancelable(false) // Prevent dialog from being dismissed by tapping outside
            .setPositiveButton("Yes") { dialog, _ ->
                logout()
                dialog.dismiss() // Dismiss the dialog after confirming
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss() // Dismiss the dialog without logging out
            }
            .show()
    }

    private fun logout() {
        // Sign out from Firebase Authentication
        FirebaseAuth.getInstance().signOut()

        // Close the navigation drawer if it's open
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Optionally, clear SharedPreferences or any other user data
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()  // Clear all stored preferences
        editor.apply()

        // Show a toast message to inform the user they have logged out
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Redirect to the Login activity
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()  // Close the AdminDashboard activity
    }

    private fun updateDateTime() {
        // Get the current date and time
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

    // Handle back press to exit app (close all activities)
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity() // Close all activities and exit the app
    }
}
