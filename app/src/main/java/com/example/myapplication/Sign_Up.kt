package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
            } else if (!isValidEmail(userEmail)) {
                Toast.makeText(this, "Please enter a valid Gmail address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get selected user type from Spinner, default to "User" if the spinner is hidden
            val selectedUserType = if (binding.spinnerRole.visibility == View.VISIBLE) {
                binding.spinnerRole.selectedItem.toString()
            } else {
                "User" // Default to "User" if the spinner is not visible
            }

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
            startActivity(Intent(this, Login::class.java))
        }

        // Toggle password visibility
        binding.SignUpShowPassword.setOnClickListener {
            togglePasswordVisibility(binding.etSignInPassword)
        }
        binding.ivShowConfirmPassword.setOnClickListener {
            togglePasswordVisibility(binding.etConfirmPassword)
        }
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun setupRoleSpinner() {
        // Check if the admin exists
        if (isAdminAvailable) {
            binding.tvRole.visibility = View.GONE
            binding.spinnerRole.visibility = View.GONE
        } else {
            // Show the spinner and allow selecting "User" or "Admin"
            binding.spinnerRole.visibility = View.VISIBLE
            val userTypes = arrayOf("Select Role", "User", "Admin")
            val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, userTypes)
            binding.spinnerRole.adapter = spinnerAdapter
        }
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
        val userId = email.hashCode().toString()
        val database = FirebaseDatabase.getInstance().getReference("pending_users")
        val dateTime = getCurrentDateTime()

        val pendingUser = mapOf(
            "id" to userId,
            "name" to userName,
            "email" to email,
            "password" to password,
            "role" to "user",
            "status" to "pending",
            "signUpTime" to dateTime
        )

        database.child(userId).setValue(pendingUser).addOnCompleteListener { dbTask ->
            if (dbTask.isSuccessful) {
                Toast.makeText(this, "Account created successfully! Awaiting admin approval.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, Login::class.java))
                finish()
            } else {
                Toast.makeText(this, "Failed to create account. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createAdminAccount(userName: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser!!.uid
                    val database = FirebaseDatabase.getInstance().getReference("admins")
                    val dateTime = getCurrentDateTime()

                    val adminUser = mapOf(
                        "id" to userId,
                        "name" to userName,
                        "email" to email,
                        "role" to "admin",
                        "signUpTime" to dateTime
                    )

                    database.child(userId).setValue(adminUser).addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            Toast.makeText(this, "Admin account created successfully.", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, Login::class.java))
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

    private fun isValidEmail(email: String): Boolean {
        return email.endsWith("@gmail.com")
    }
}


