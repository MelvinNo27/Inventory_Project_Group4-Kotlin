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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class Sign_Up : AppCompatActivity() {
    private val TAG = "RegisterActivity"
    private lateinit var auth: FirebaseAuth

    // Declare fullNameEditText as a member variable
    private lateinit var fullNameEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

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
                // Proceed with Firebase registration if all checks pass
                registerUser(email, password)
            }
        }

        login.setOnClickListener {
            login.text = HtmlCompat.fromHtml("<u>Login</u>", HtmlCompat.FROM_HTML_MODE_LEGACY)
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        // Facebook and Instagram Button Logic
        facebookButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/login"))
            startActivity(intent)
        }
        instagramButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/accounts/login/"))
            startActivity(intent)
        }
    }

    // Function to handle potential errors during authentication, such as existing accounts
    private fun handleAuthError(exception: Exception?) {
        if (exception is FirebaseAuthUserCollisionException) {
            // Inform the user and redirect to login
            Toast.makeText(this, "Account already exists. Redirecting to login.", Toast.LENGTH_LONG).show()
            redirectToLogin()
        } else {
            Log.w(TAG, "signInWithCredential:failure", exception)
            Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
        }
    }

    // Redirect the user to the login screen
    private fun redirectToLogin() {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }

    // Prompt user to enter their full name if it's missing
    private fun promptForFullName(user: FirebaseUser?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter your full name")

        // Set up the input
        val input = EditText(this)
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("Save") { dialog, _ ->
            val fullName = input.text.toString().trim()
            if (fullName.isNotEmpty()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build()
                user?.updateProfile(profileUpdates)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Welcome, $fullName!", Toast.LENGTH_SHORT).show()
                            updateUI(user)
                        } else {
                            Toast.makeText(this, "Failed to save name.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Name cannot be empty.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    // Function to register the user using Firebase Auth
    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // Update the user's display name with full name input
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
            val intent = Intent(this, Userdashboard::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, Userdashboard::class.java)
            startActivity(intent)
            finish()
        }
    }
}