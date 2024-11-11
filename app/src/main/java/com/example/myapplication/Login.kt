package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.example.myapplication.databinding.ActivityLogInBinding
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLogInBinding
    private var isPasswordVisible = false

    @SuppressLint("QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using View Binding
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Login Button Logic
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email == "Admin" && password == "Admin123") {
                // Redirect to Admin Dashboard
                startActivity(Intent(this, AdminDashboard::class.java))
            } else if (email.isEmpty()) {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (password.isEmpty()) {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Check if email exists in Firebase
                            if (auth.currentUser != null) {
                                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, Userdashboard::class.java))
                                finish()
                            }
                        } else {
                            // Login failed
                            Toast.makeText(this, "Email doesn't exist or incorrect password. Please try again or sign up.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        // Register TextView Click Listener
        binding.tvSignUp.setOnClickListener {
            binding.tvSignUp.text = HtmlCompat.fromHtml("<u>Sign Up</u>", HtmlCompat.FROM_HTML_MODE_LEGACY)
            startActivity(Intent(this, Sign_Up::class.java))
        }

        // Forgot Password Click Listener
        binding.tvForgotPassword.setOnClickListener {
            binding.tvForgotPassword.text = HtmlCompat.fromHtml("<u>Forgot Password</u>", HtmlCompat.FROM_HTML_MODE_LEGACY)
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Show Password Toggle
        binding.loginShowPassword.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide Password
            binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.loginShowPassword.setImageResource(R.drawable.offpass) // Use closed-eye icon
            isPasswordVisible = false
        } else {
            // Show Password
            binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.loginShowPassword.setImageResource(R.drawable.baseline_remove_red_eye_24) // Use open-eye icon
            isPasswordVisible = true
        }

        // Move the cursor to the end of the text
        binding.etPassword.setSelection(binding.etPassword.text.length)
    }
}
