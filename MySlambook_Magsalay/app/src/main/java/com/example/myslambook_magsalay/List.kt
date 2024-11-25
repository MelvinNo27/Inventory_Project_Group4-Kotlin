package com.example.myslambook_magsalay

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.myslambook_magsalay.databinding.ActivityListBinding

class List : AppCompatActivity() {

    private lateinit var binding: ActivityListBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val users = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MySlamBook", MODE_PRIVATE)

        // Retrieve data from SharedPreferences
        val firstName = sharedPreferences.getString("firstName", "")
        val lastName = sharedPreferences.getString("lastName", "")
        if (!firstName.isNullOrEmpty() && !lastName.isNullOrEmpty()) {
            users.add("$firstName $lastName") // Add full name to the list
        }

        // Set up ListView with Adapter
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            users
        )
        binding.listView.adapter = adapter

        // Handle Item Click to View Details
        binding.listView.setOnItemClickListener { _, _, _, _ ->
            val intent = Intent(this, DetailsActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.ListBackButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
