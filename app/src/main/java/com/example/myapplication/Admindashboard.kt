package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class AdminDashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val btnUsers = findViewById<ImageView>(R.id.btnUsers)
        val btnCategory = findViewById<ImageView>(R.id.btnCategories)
        val btnProducts = findViewById<ImageView>(R.id.btnCurrentStocks)


        btnUsers.setOnClickListener {
            // Handle Users button click
        }

        btnCategory.setOnClickListener {
            // Handle Category button click
        }

        btnProducts.setOnClickListener {
            // Handle Products button click
        }

    }
}
