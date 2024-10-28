package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.google.firebase.auth.FirebaseAuth


class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth // Initialize FirebaseAuth
    private var isPasswordVisible = false // Variable to toggle password visibility

    @SuppressLint("QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // UI Elements
        val ivShowPassword = findViewById<ImageView>(R.id.ivShowPassword)
        val emailEditText = findViewById<EditText>(R.id.etEmail)
        val passwordEditText = findViewById<EditText>(R.id.etPassword)
        val loginButton = findViewById<Button>(R.id.btnSignIn)
        val register = findViewById<TextView>(R.id.tvSignUp)
        val forgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val googleButton = findViewById<ImageView>(R.id.google_button) // Google button

        // Login Button Logic
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Sign in with Firebase Auth
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext, "Login Successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(applicationContext, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@Login, "Email doesn't exist, please try again or sign up.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Register TextView Click Listener
        register.setOnClickListener {
            register.text = HtmlCompat.fromHtml("<u>Sign Up</u>", HtmlCompat.FROM_HTML_MODE_LEGACY)
            startActivity(Intent(this, Sign_Up::class.java))
        }

        // Forgot Password Click Listener
        forgotPassword.setOnClickListener {
            forgotPassword.text = HtmlCompat.fromHtml("<u>Forgot Password</u>", HtmlCompat.FROM_HTML_MODE_LEGACY)
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Show Password Toggle
        ivShowPassword.setOnClickListener {
            togglePasswordVisibility(passwordEditText, ivShowPassword)
        }

    }

    private fun togglePasswordVisibility(passwordEditText: EditText, ivShowPassword: ImageView) {
        if (isPasswordVisible) {
            // Hide Password
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            ivShowPassword.setImageResource(R.drawable.offpass) // Use closed-eye icon
            isPasswordVisible = false
        } else {
            // Show Password
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            ivShowPassword.setImageResource(R.drawable.baseline_remove_red_eye_24) // Use open-eye icon
            isPasswordVisible = true
        }

        // Move the cursor to the end of the text
        passwordEditText.setSelection(passwordEditText.text.length)
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
