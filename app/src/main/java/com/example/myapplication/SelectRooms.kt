package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySelectRoomBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SelectRooms : AppCompatActivity() {

    private lateinit var binding: ActivitySelectRoomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivitySelectRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle button clicks for room selection
        binding.buttonRoom7.setOnClickListener {
            showRoom(7)
        }

        binding.buttonRoom14.setOnClickListener {
            showRoom(14)
        }


        // Handle back button click
        binding.selectRoomBack.setOnClickListener {
            // Retrieve the current user's ID using Firebase Authentication
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (userId != null) {
                // Check user role and navigate
                checkUserRoleAndNavigate(userId)
            } else {
                // If no user is logged in, redirect to login screen
                Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, Login::class.java))
                finish()
            }
        }
    }

    private fun showRoom(roomNo: Int){

        val i = Intent(this, Room14::class.java);
        i.putExtra("roomNo", roomNo)
        startActivity(i)


    }

    private fun checkUserRoleAndNavigate(userId: String) {
        // First, check in the 'admins' node for the user
        val adminRef = FirebaseDatabase.getInstance().reference.child("admins").child(userId)

        adminRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // User is an admin, navigate to the Admin Dashboard
                    startActivity(Intent(this@SelectRooms, AdminDashboard::class.java))
                    finish()
                } else {
                    // If the user is not found in the 'admins' node, check in the 'users' node
                    val userRef =
                        FirebaseDatabase.getInstance().reference.child("users").child(userId)

                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            if (userSnapshot.exists()) {
                                val role = userSnapshot.child("role").getValue(String::class.java)

                                if (role.isNullOrEmpty()) {
                                    Toast.makeText(
                                        this@SelectRooms,
                                        "Role is missing or invalid",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return
                                }

                                when (role) {
                                    "User" -> { startActivity(Intent(this@SelectRooms, Userdashboard::class.java))
                                        finish()
                                    }

                                    else -> {
                                        Toast.makeText(
                                            this@SelectRooms,
                                            "Unknown user role",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    this@SelectRooms,
                                    "User not found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(
                                this@SelectRooms,
                                "Error fetching user data: ${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@SelectRooms,
                    "Error fetching admin data: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
