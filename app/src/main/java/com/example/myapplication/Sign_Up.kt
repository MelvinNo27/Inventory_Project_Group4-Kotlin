package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class Sign_Up : AppCompatActivity() {
    private val TAG = "RegisterActivity"
    private lateinit var auth: FirebaseAuth

    // Declare fullNameEditText as a member variable
    private lateinit var fullNameEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()

        // Initialize fullNameEditText
        fullNameEditText = findViewById(R.id.User_Name)
        val emailEditText = findViewById<EditText>(R.id.etSign_inEmail)
        val passwordEditText = findViewById<EditText>(R.id.etSign_inPassword)
        val confirmPasswordEditText = findViewById<EditText>(R.id.etConfirmPassword)
        val signupButton = findViewById<Button>(R.id.signUpButton)
        val login = findViewById<TextView>(R.id.tvlogin)
        val facebookButton = findViewById<ImageView>(R.id.facebook_button)
        val instagramButton = findViewById<ImageView>(R.id.instagram_button)

        signupButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                // Proceed with Firebase registration
                registerUser(email, password)
            }
        }

        login.setOnClickListener {
            // Underline the login text
            login.text = HtmlCompat.fromHtml("<u>Login</u>", HtmlCompat.FROM_HTML_MODE_LEGACY)
            // Navigate to Login activity
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        // Facebook Button Logic
        facebookButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/login"))
            startActivity(intent)
        }

        // Instagram Button Logic
        instagramButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/accounts/login/"))
            startActivity(intent)
        }
    }

    // Function to register the user using Firebase Auth
    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // Update the user's display name
                    val fullName = fullNameEditText.text.toString()
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build()
                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                Toast.makeText(this, "Sign-up Successful!", Toast.LENGTH_SHORT).show()
                                updateUI(user)
                            } else {
                                Toast.makeText(this, "Failed to set user name.", Toast.LENGTH_SHORT).show()
                                updateUI(null)
                            }
                        }
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    val errorMessage = task.exception?.message ?: "Authentication failed."
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    // Function to update the UI based on registration status
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // Successful registration
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish() // Finish the current activity to prevent going back to it
        } else {
            // Registration failed
            Toast.makeText(this, "Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onStart() {
        super.onStart()
        // Check if the user is already signed in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
