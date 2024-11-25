package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Sign_Up : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignUpBinding
    private var isPasswordVisible = false
    private var isAdminAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Check if an admin account already exists
        checkAdminExists { adminExists ->
            isAdminAvailable = adminExists
            setupRoleSpinner()
        }

        // Email Sign-Up button click
        binding.signUpButton.setOnClickListener {
            val userName = binding.UserName.text.toString()
            val userEmail = binding.etSignInEmail.text.toString()
            val password = binding.etSignInPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (userName.isEmpty() || userEmail.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get selected user type from Spinner
            val selectedUserType = binding.spinnerRole.selectedItem.toString()

            if (selectedUserType == "Admin") {
                // Create Admin account
                if (isAdminAvailable) {
                    Toast.makeText(this, "Admin account already exists.", Toast.LENGTH_SHORT).show()
                } else {
                    createAdminAccount(userName, userEmail, password)
                }
            } else if (selectedUserType == "User") {
                // Register user and send for admin approval
                registerPendingUser(userName, userEmail, password)
            } else {
                Toast.makeText(this, "Please select a valid user type", Toast.LENGTH_SHORT).show()
            }
        }

        // Login link
        binding.tvlogin.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        // Toggle password visibility
        binding.SignUpShowPassword.setOnClickListener {
            togglePasswordVisibility(binding.etSignInPassword)
        }
        binding.ivShowConfirmPassword.setOnClickListener {
            togglePasswordVisibility(binding.etConfirmPassword)
        }
    }

    private fun setupRoleSpinner() {
        // Setup spinner with or without Admin option based on availability
        val userTypes = if (isAdminAvailable) {
            arrayOf("Select Role", "User") // Only User is available
        } else {
            arrayOf("Select Role", "User", "Admin") // Both User and Admin are available
        }

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, userTypes)
        binding.spinnerRole.adapter = spinnerAdapter
    }

    private fun checkAdminExists(callback: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("admins")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // If there are any admins, return true
                callback(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Sign_Up, "Error checking admin status: ${error.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        })
    }

    private fun registerPendingUser(userName: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser!!.uid
                    val database = FirebaseDatabase.getInstance().getReference("pending_users") // Save pending users in "pending_users" node

                    // User data with status set to "pending" for admin approval
                    val pendingUser = mapOf(
                        "id" to userId,
                        "name" to userName, // Save the username
                        "email" to email,
                        "role" to "user", // User role
                        "status" to "pending" // Pending approval by admin
                    )

                    database.child(userId).setValue(pendingUser).addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Account created successfully! Awaiting admin approval.",
                                Toast.LENGTH_SHORT
                            ).show()
                            auth.signOut()
                            startActivity(Intent(this, Login::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to create account. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Sign-Up Failed. ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun createAdminAccount(userName: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser!!.uid
                    val database = FirebaseDatabase.getInstance().getReference("admins")

                    val adminUser = mapOf(
                        "id" to userId,
                        "name" to userName, // Save the admin username
                        "email" to email,
                        "role" to "admin"
                    )

                    database.child(userId).setValue(adminUser).addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            Toast.makeText(this, "Admin account created successfully.", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, AdminDashboard::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to create admin account.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Sign-Up Failed. ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun togglePasswordVisibility(field: android.widget.EditText) {
        if (isPasswordVisible) {
            field.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            isPasswordVisible = false
        } else {
            field.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            isPasswordVisible = true
        }
        field.setSelection(field.text.length)
    }
}
