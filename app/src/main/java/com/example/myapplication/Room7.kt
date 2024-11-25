package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityRoom7Binding


class Room7 : AppCompatActivity() {

    private lateinit var binding: ActivityRoom7Binding // ViewBinding instance


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Initialize ViewBinding
        binding = ActivityRoom7Binding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.Room7back.setOnClickListener {
            startActivity(Intent(this, SelectRooms::class.java))
            finish()
        }
    }

}


