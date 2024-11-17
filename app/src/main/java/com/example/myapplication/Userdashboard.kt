package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.myapplication.databinding.ActivityUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import com.google.firebase.database.DatabaseReference
import java.util.*

class Userdashboard : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityUserBinding
    private lateinit var rootDatabaseRef: DatabaseReference
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
        rootDatabaseRef = FirebaseDatabase.getInstance().getReference("users")


        val user = auth.currentUser
        if (user != null) {
            fetchUserNameFromDatabase(user.uid )
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

    private fun fetchUserNameFromDatabase(userId: String) {
        rootDatabaseRef.child(userId).get().addOnSuccessListener { dataSnapshot ->
            // Check if data exists for the user
            val name = dataSnapshot.child("name").value.toString()
            if (name.isNotEmpty()) {
                binding.welcomeTextView.text = "Hello, $name!"
            } else {
                Toast.makeText(this, "User name not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
        }
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
