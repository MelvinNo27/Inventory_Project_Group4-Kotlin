package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
    private lateinit var auth: FirebaseAuth  // Initialize FirebaseAuth

    @SuppressLint("QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.etEmail)
        val passwordEditText = findViewById<EditText>(R.id.etPassword)
        val loginButton = findViewById<Button>(R.id.btnSignIn)
        val register = findViewById<TextView>(R.id.tvSignUp)
        val facebookButton = findViewById<ImageView>(R.id.facebook_button)
        val instagramButton = findViewById<ImageView>(R.id.instagram_button)

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
                        val intent = Intent(applicationContext, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@Login,"Email doesn't exist pls try again or sign_up.",Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Register TextView Click Listener
        register.setOnClickListener {
            register.text = HtmlCompat.fromHtml("<u>Register</u>", HtmlCompat.FROM_HTML_MODE_LEGACY)
            val intent = Intent(this, Sign_Up::class.java)
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

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
