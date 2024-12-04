package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.example.myapplication.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible = false

    @SuppressLint("QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using View Binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Login Button Logic
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (password.isEmpty()) {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Retrieve the user ID of the logged-in user
                            val userId = auth.currentUser!!.uid
                            checkUserRole(userId)
                        } else {
                            // Login failed
                            Toast.makeText(
                                this,
                                "Login failed. Check your email and password.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

        // Forgot Password Click Listener
        binding.tvForgotPassword.setOnClickListener {
            binding.tvForgotPassword.text =
                HtmlCompat.fromHtml("<u>Forgot Password</u>", HtmlCompat.FROM_HTML_MODE_LEGACY)
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Sign-Up Click Listener
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, Sign_Up::class.java))
            finish()
        }

        // Show Password Toggle
        binding.loginShowPassword.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide Password
            binding.etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.loginShowPassword.setImageResource(R.drawable.offpass) // Use closed-eye icon
            isPasswordVisible = false
        } else {
            // Show Password
            binding.etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.loginShowPassword.setImageResource(R.drawable.baseline_remove_red_eye_24) // Use open-eye icon
            isPasswordVisible = true
        }

        // Move the cursor to the end of the text
        binding.etPassword.setSelection(binding.etPassword.text.length)
    }

    private fun checkUserRole(userId: String) {
        val database = FirebaseDatabase.getInstance()

        // Check if the user is an admin
        val adminRef = database.reference.child("admins").child(userId)
        adminRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // User is an admin
                    Toast.makeText(this@Login, "Welcome Admin!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@Login, AdminDashboard::class.java))
                    finish()
                } else {
                    // Check if the user is in pending_users
                    val pendingUserRef = database.reference.child("pending_users").child(userId)
                    pendingUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(pendingSnapshot: DataSnapshot) {
                            if (pendingSnapshot.exists()) {
                                // User is pending approval
                                Toast.makeText(
                                    this@Login,
                                    "Your account is pending approval.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                auth.signOut()
                            } else {
                                // Check in the users node for approved users
                                val userRef = database.reference.child("users").child(userId)
                                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(userSnapshot: DataSnapshot) {
                                        if (userSnapshot.exists()) {
                                            // User is a regular user
                                            val status = userSnapshot.child("status").getValue(String::class.java)
                                            if (status == "approved") {
                                                Toast.makeText(
                                                    this@Login,
                                                    "Welcome User!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                startActivity(Intent(this@Login, Userdashboard::class.java))
                                                finish()
                                            } else {
                                                Toast.makeText(
                                                    this@Login,
                                                    "Your account is pending approval.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                auth.signOut()
                                            }
                                        } else {
                                            // No role information found
                                            Toast.makeText(
                                                this@Login,
                                                "No role information found.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            auth.signOut()
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(
                                            this@Login,
                                            "Error fetching user data: ${error.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(
                                this@Login,
                                "Error fetching pending user data: ${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@Login,
                    "Error fetching admin data: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
