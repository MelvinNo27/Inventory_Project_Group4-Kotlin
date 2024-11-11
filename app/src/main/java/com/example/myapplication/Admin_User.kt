package com.example.myapplication


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityAdminUserBinding


class Admin_User : AppCompatActivity() {

    private lateinit var binding: ActivityAdminUserBinding  // Use the correct class name

    @SuppressLint("QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminUserBinding.inflate(layoutInflater)  // Correct usage
        setContentView(binding.root)

        binding.btnAdmin.setOnClickListener {
            // Create an Intent to navigate to the Login activity
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        binding.btnUser.setOnClickListener {
            // Create an Intent to navigate to the Login activity
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }
}

