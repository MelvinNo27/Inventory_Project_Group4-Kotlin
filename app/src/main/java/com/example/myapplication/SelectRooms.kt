package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySelectRoomBinding


class SelectRooms : AppCompatActivity() {

    private lateinit var binding: ActivitySelectRoomBinding // ViewBinding instance


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Initialize ViewBinding
        binding = ActivitySelectRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonRoom7.setOnClickListener {
            startActivity(Intent(this, Room7::class.java))
            finish()
        }

        binding.buttonRoom14.setOnClickListener {
            // Ensure that it is not triggering an unintended action
            startActivity(Intent(this, Room14::class.java))
            finish()

        }
    }

    }


