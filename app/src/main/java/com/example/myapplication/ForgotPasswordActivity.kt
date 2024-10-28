package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()
        val emailEditText = findViewById<EditText>(R.id.etEmailReset)
        val sendResetLinkButton = findViewById<Button>(R.id.btnSendResetLink)

        sendResetLinkButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_SHORT).show()
                            finish()  // Close the activity after sending the link
                        } else {
                            Toast.makeText(this, "Failed to send reset email", Toast.LENGTH_SHORT).show()
                            return@addOnCompleteListener
                        }
                    }
            }
        }
    }
}
