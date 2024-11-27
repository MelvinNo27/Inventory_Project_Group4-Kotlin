package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityRoom14Binding


class Room14 : AppCompatActivity() {

    private lateinit var binding: ActivityRoom14Binding // ViewBinding instance


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRoom14Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRoom14.setOnClickListener{
            val backtoselectroom = Intent (this, SelectRooms::class.java )
            startActivity(backtoselectroom)
        }

        }
    }




