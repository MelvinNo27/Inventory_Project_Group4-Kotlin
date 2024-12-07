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

        FirebaseDatabase.getInstance().reference.child("admins").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // User is an admin
                    Toast.makeText(this@Login, "Welcome Admin!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@Login, AdminDashboard::class.java))
                    finish()
                } else {
                    // If user is not an admin, check in the "users" node
                    checkUserInUsersNode(userId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Login, "Error fetching admin data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Helper method to check user status in "users" node
    private fun checkUserInUsersNode(userId: String) {
        val userRef =  FirebaseDatabase.getInstance().reference.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val role = snapshot.child("role").getValue(String::class.java)
                    val status = snapshot.child("status").getValue(String::class.java)

                    when {
                        role == "User" -> {
                            // Approved user logged in
                            Toast.makeText(this@Login, "Welcome User!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@Login, Userdashboard::class.java))
                            finish()
                        }
                        status == "pending" -> {
                            // User account is pending approval
                            Toast.makeText(this@Login, "Your account is pending approval.", Toast.LENGTH_SHORT).show()
                            auth.signOut()
                        }
                        else -> {
                            // Invalid status or role
                            Toast.makeText(this@Login, "Invalid user status or role", Toast.LENGTH_SHORT).show()
                            auth.signOut()
                        }
                    }
                } else {
                    // User not found in the database
                    Toast.makeText(this@Login, "User not found.", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Login, "Error fetching user data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



}

