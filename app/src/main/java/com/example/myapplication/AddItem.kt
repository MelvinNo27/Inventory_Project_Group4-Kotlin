package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityAddItemBinding

class AddItem : AppCompatActivity() {

    private lateinit var binding: ActivityAddItemBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if data was passed for editing
        val id = intent.getStringExtra("ID") ?: ""
        val name = intent.getStringExtra("Name") ?: ""
        val quantity = intent.getStringExtra("Quantity") ?: ""
        val status = intent.getStringExtra("Status") ?: ""

        // Pre-fill fields if editing

        binding.nameEditText.setText(name)
        binding.EmailEditText.setText(quantity)
        binding.statusEditText.setText(status)

        binding.AddButton.setOnClickListener {
            val newId = binding.idEditText.text.toString()
            val newName = binding.nameEditText.text.toString()
            val newQuantity = binding.EmailEditText.text.toString()
            val newStatus = binding.statusEditText.text.toString()

            val intent = Intent()
            intent.putExtra("ID", newId)
            intent.putExtra("Name", newName)
            intent.putExtra("Quantity", newQuantity)
            intent.putExtra("Status", newStatus)

            setResult(Activity.RESULT_OK, intent)
            finish()  // Close the AddItem activity
        }
    }
}

