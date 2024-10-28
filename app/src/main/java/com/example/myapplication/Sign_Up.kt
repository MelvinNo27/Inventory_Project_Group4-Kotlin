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

class Sign_Up : AppCompatActivity() {
    // Define a TAG for logging
    private val TAG = "RegisterActivity"
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // UI Elements
        val fullNameEditText = findViewById<EditText>(R.id.User_Name)
        val emailEditText = findViewById<EditText>(R.id.etSign_inEmail)
        val passwordEditText = findViewById<EditText>(R.id.etSign_inPassword)
        val confirmPasswordEditText = findViewById<EditText>(R.id.etConfirmPassword)
        val signupButton = findViewById<Button>(R.id.signUpButton)
        val login = findViewById<TextView>(R.id.tvlogin)
        val googleButton = findViewById<ImageView>(R.id.google_button)
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
                findViewById<EditText>(R.id.etSign_inPassword).text.clear()
                findViewById<EditText>(R.id.etConfirmPassword).text.clear()
                return@setOnClickListener
            } else {
                // All conditions met, proceed with Firebase registration
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
        googleButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://accounts.google.com/v3/signin/identifier?continue=https%3A%2F%2Faccounts.google.com%2F&followup=https%3A%2F%2Faccounts.google.com%2F&ifkv=AcMMx-dR8A6C_Lmy1Xn0jrj6niUBdB_Y0ZPDKfN6b6QwEem4ZNZ0BmGc3UvKQteqEQNBRg4cxHmZ&passive=1209600&flowName=GlifWebSignIn&flowEntry=ServiceLogin&dsh=S201590499%3A1730116311149282&ddm=0"))
            startActivity(intent)
        }
    }

    // Function to register the user using Firebase Auth
    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    Toast.makeText(this, "Sign-up Successful!", Toast.LENGTH_SHORT).show()
                    updateUI(user)
                } else {
                    // If registration fails, log the exception and show an error message
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    val errorMessage = task.exception?.message ?: "Authentication failed."
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    updateUI(null)
                    findViewById<EditText>(R.id.etSign_inEmail).text.clear()
                    findViewById<EditText>(R.id.etSign_inPassword).text.clear()
                    findViewById<EditText>(R.id.etConfirmPassword).text.clear()
                }
            }
    }

    // Function to update the UI based on registration status
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // Navigate to HomeActivity if registration is successful
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Finish the current activity to prevent going back to it
        } else {
            // Stay on the same screen if user is null (registration failed)
            Toast.makeText(this, "Please try again.", Toast.LENGTH_SHORT).show()
            findViewById<EditText>(R.id.etSign_inEmail).text.clear()
            findViewById<EditText>(R.id.etSign_inPassword).text.clear()
            findViewById<EditText>(R.id.etConfirmPassword).text.clear()
        }
    }
}
