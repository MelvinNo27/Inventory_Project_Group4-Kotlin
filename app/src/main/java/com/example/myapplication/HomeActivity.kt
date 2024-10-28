package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val auth = FirebaseAuth.getInstance()
        val logoutButton = findViewById<ImageView>(R.id.floatingAddButton)

        logoutButton.setOnClickListener {
            // Sign out the user
            auth.signOut()

            // Redirect to Login activity
            val intent = Intent(this, Login::class.java)
            startActivity(intent)

            // Finish current activity
            finish()
        }
    }
}
