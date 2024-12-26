package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityForgotPasswordBinding
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.backBtn.setOnClickListener {
            checkUserRoleAndNavigate()
        }

        binding.btnSendResetLink.setOnClickListener {
            val email = binding.etEmailReset.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_SHORT).show()
                            // Check user role and navigate accordingly
                            checkUserRoleAndNavigate()
                        } else {
                            Toast.makeText(this, "Failed to send reset email", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun checkUserRoleAndNavigate() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // First check if user is admin
            database.child("admins").child(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // User is an admin
                            startActivity(Intent(this@ForgotPasswordActivity, AdminDashboard::class.java))
                            finish()
                        } else {
                            // Not an admin, check if regular user
                            checkUserInUsersNode(currentUser.uid)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            "Error checking user role: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this@ForgotPasswordActivity, Login::class.java))
                        finish()
                    }
                })
        } else {
            // No user logged in, go to login activity
            startActivity(Intent(this@ForgotPasswordActivity, Login::class.java))
            finish()
        }
    }

    private fun checkUserInUsersNode(userId: String) {
        database.child("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val role = snapshot.child("role").getValue(String::class.java)
                        when (role) {
                            "user" -> {
                                startActivity(Intent(this@ForgotPasswordActivity, Userdashboard::class.java))
                                finish()
                            }
                            else -> {
                                // Unknown role, go to login
                                startActivity(Intent(this@ForgotPasswordActivity, Login::class.java))
                                finish()
                            }
                        }
                    } else {
                        // User not found in users node, go to login
                        startActivity(Intent(this@ForgotPasswordActivity, Login::class.java))
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "Error checking user role: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Navigate to login activity as fallback
                    startActivity(Intent(this@ForgotPasswordActivity, Login::class.java))
                    finish()
                }
            })
    }
}