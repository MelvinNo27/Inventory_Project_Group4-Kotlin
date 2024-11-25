package com.example.myslambook_magsalay

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myslambook_magsalay.databinding.ActivityDetailsBinding

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MySlamBook", MODE_PRIVATE)

        // Display user details
        binding.fullName.text = "${sharedPreferences.getString("firstName", "")} ${sharedPreferences.getString("lastName", "")}"
        binding.nickname.text = "Nickname: ${sharedPreferences.getString("nickname", "")}"
        binding.friendsCallYou.text = "Friends Call You: ${sharedPreferences.getString("friendsCallYou", "")}"
        binding.likeToCallMe.text = "Like To Call Me: ${sharedPreferences.getString("likeToCallMe", "")}"
        binding.email.text = "Email: ${sharedPreferences.getString("email", "")}"
        binding.status.text = "Status: ${sharedPreferences.getString("status", "")}"
        binding.address.text = "Address: ${sharedPreferences.getString("address", "")}"
        binding.contactNumber.text = "Contact Number: ${sharedPreferences.getString("contactNumber", "")}"
        binding.gender.text = "Gender: ${sharedPreferences.getString("gender", "")}"
        binding.birthDate.text = "Birth Date: ${sharedPreferences.getString("birthDate", "")}"


        binding.DetailsBackButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
