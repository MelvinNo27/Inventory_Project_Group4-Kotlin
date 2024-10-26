package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat

class Sign_in : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val fullNameEditText = findViewById<EditText>(R.id.FullName)
        val emailEditText = findViewById<EditText>(R.id.etSign_inEmail)
        val passwordEditText = findViewById<EditText>(R.id.etSign_inPassword)
        val confirmPasswordEditText = findViewById<EditText>(R.id.etConfirmPassword)
        val signupButton = findViewById<Button>(R.id.btn_sign_Up)
        val login = findViewById<TextView>(R.id.tvlogin)
        val facebookButton = findViewById<ImageButton>(R.id.facebook_button)
        val instagramButton = findViewById<ImageButton>(R.id.instagram_button)


        signupButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                // All conditions met, proceed to next activity
                Toast.makeText(this, "Sign-up Successful!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }
        }




        login.setOnClickListener {
            val loginTextView = findViewById<TextView>(R.id.tvRegister)
            loginTextView.text = HtmlCompat.fromHtml("<u>Login</u>", HtmlCompat.FROM_HTML_MODE_LEGACY)

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
}
