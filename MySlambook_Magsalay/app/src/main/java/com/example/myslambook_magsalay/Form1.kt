package com.example.myslambook_magsalay

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myslambook_magsalay.databinding.ActivityForm1Binding
import com.google.android.material.imageview.ShapeableImageView


class Form1 : AppCompatActivity() {

    private lateinit var bindingForm: ActivityForm1Binding
    private lateinit var sharedPreferences: SharedPreferences

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the SharedPreferences
        sharedPreferences = getSharedPreferences("MySlamBook", MODE_PRIVATE)

        // Use view binding to inflate Form1 and Profile layout
        bindingForm = ActivityForm1Binding.inflate(layoutInflater)

        // Display the Form1 (for user details) layout by default
        setContentView(bindingForm.root)

        setupForm1()

        // Switch to Profile (Avatar editing) layout
        bindingForm.editButton.setOnClickListener {
            setContentView(bindingForm.root)
            setupProfile()
        }
    }

    private fun setupForm1() {

        val months = resources.getStringArray(R.array.monthName)
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bindingForm.monthSpinner.adapter = monthAdapter


        val days = resources.getStringArray(R.array.monthDay)
        val dayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, days)
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bindingForm.daySpinner.adapter = dayAdapter


        val years = resources.getStringArray(R.array.Years)
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bindingForm.yearSpinner.adapter = yearAdapter


        val gender = resources.getStringArray(R.array.gender)
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, gender)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bindingForm.genderSpinner.adapter = genderAdapter

        // Submit Button Click
        bindingForm.btnSubmit.setOnClickListener {
            // Save user data in SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putString("firstName", bindingForm.firstName.text.toString())
            editor.putString("lastName", bindingForm.lastName.text.toString())
            editor.putString("nickname", bindingForm.nickname.text.toString())
            editor.putString("friendsCallYou", bindingForm.friendsCallYou.text.toString())
            editor.putString("likeToCallMe", bindingForm.likeToCallMe.text.toString())
            editor.putString("email", bindingForm.email.text.toString())
            editor.putString("status", bindingForm.status.text.toString())
            editor.putString("address", bindingForm.address.text.toString())
            editor.putString("contactNumber", bindingForm.contactNumber.text.toString())
            editor.putString("gender", bindingForm.genderSpinner.selectedItem.toString())
            editor.putString(
                "birthDate",
                "${bindingForm.monthSpinner.selectedItem} ${bindingForm.daySpinner.selectedItem}, ${bindingForm.yearSpinner.selectedItem}"
            )
            editor.apply()

            startActivity(Intent(this, List::class.java))

            // Show confirmation toast
            Toast.makeText(this, "User data submitted successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Back Button Click for Form1
        bindingForm.backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun setupProfile() {

        val profileImage: ShapeableImageView = bindingForm.profileImage
        profileImage.setImageResource(R.drawable.avatar)

        //Edit button (to choose image from gallery)
        bindingForm.editButton.setOnClickListener {
            startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), PICK_IMAGE_REQUEST)
        }

        // Back button functionality to return to Form1
        bindingForm.backButton.setOnClickListener {
            setContentView(bindingForm.root)
            setupForm1()
        }
    }

    // Handle the result of the image picker or camera photo
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    if (data?.data != null) {
                        bindingForm.profileImage.setImageURI(data.data)
                        Toast.makeText(this, "Avatar updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
