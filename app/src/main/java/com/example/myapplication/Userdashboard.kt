package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ActivityProfileBinding
import com.example.myapplication.databinding.ActivityUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class Userdashboard : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityUserBinding
    private lateinit var bindings: ActivityProfileBinding
    private lateinit var rootDatabaseRef: DatabaseReference
    private val PICK_IMAGE_REQUEST = 1
    private val handler = android.os.Handler()

    // Runnable to update date and time
    private val updateDateTimeRunnable = object : Runnable {
        override fun run() {
            updateDateTime()
            handler.postDelayed(this, 1000) // Update every second
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityUserBinding.inflate(layoutInflater)
        bindings = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Database Reference
        auth = FirebaseAuth.getInstance()
        rootDatabaseRef = FirebaseDatabase.getInstance().getReference("users")

        // Load user profile picture if available
        loadUserProfilePicture()

        // Handle button clicks
        binding.btnUserRooms.setOnClickListener {
            navigateToSelectRooms()
        }

        binding.UserlogOutButton.setOnClickListener {
            logoutUser()
        }

        binding.btnUserReports.setOnClickListener {
            startActivity(Intent(this , UserReports::class.java))
        }

        binding.profile.setOnClickListener {
            showUserProfileDialog()
        }

        // Start updating the date and time
        handler.post(updateDateTimeRunnable)
    }

    private fun loadUserProfilePicture() {
        val currentUser = auth.currentUser
        if (currentUser?.photoUrl != null) {
            Glide.with(this)
                .load(currentUser.photoUrl)
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .into(bindings.profileAvatar) // Update main avatar
        }
    }

    private fun navigateToSelectRooms() {
        val intent = Intent(this, SelectRooms::class.java)
        intent.putExtra("showAddRoomButton", false) // Passing flag as false
        startActivity(intent)
        finish()
    }

    private fun logoutUser() {
        auth.signOut()
        startActivity(Intent(this, Login::class.java))
        finish()
    }

    private fun showUserProfileDialog() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userUid = currentUser.uid

            // Inflate dialog layout
            val dialogBinding = ActivityProfileBinding.inflate(layoutInflater)

            // Load avatar using Glide
            Glide.with(this)
                .load(currentUser.photoUrl)
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .into(dialogBinding.profileAvatar)

            // Fetch user name from the Realtime Database
            rootDatabaseRef.child(userUid).child("name").get()
                .addOnSuccessListener { snapshot ->
                    dialogBinding.profileName.text = "Name: ${snapshot.value ?: "Unknown"}"
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to fetch user name: ${exception.message}", Toast.LENGTH_SHORT).show()
                    dialogBinding.profileName.text = "Name: Error"
                }

            // Set static email and UID fields
            dialogBinding.profileEmail.text = "Email: ${currentUser.email ?: "No email"}"
            dialogBinding.profileUid.text = "UID: $userUid"

            // Show the dialog
            val dialog = AlertDialog.Builder(this)
                .setTitle("User Profile")
                .setView(dialogBinding.root)
                .setPositiveButton("Close") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .create()

            // Handle edit avatar button click
            dialogBinding.editAvatarButton.setOnClickListener {
                // Open the gallery to select an image
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, PICK_IMAGE_REQUEST)
            }

            dialog.show()

            // Add report button click listener
            dialogBinding.forgotPasswordText.setOnClickListener {
                val intent = Intent(this, ForgotPasswordActivity::class.java)
                startActivity(intent)
                dialog.dismiss() // Close the profile dialog
            }
        } else {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data // Get the selected image URI

            // Display the selected image in the dialog using Glide (optional)
            val dialogBinding = ActivityProfileBinding.inflate(layoutInflater)
            Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.avatar) // Placeholder image while loading
                .error(R.drawable.avatar) // Error image if loading fails
                .into(dialogBinding.profileAvatar) // Update avatar in the profile dialog

            // Upload the image to Firebase Storage
            imageUri?.let {
                uploadAvatarToFirebase(it) { uploadedImageUrl ->
                    // After uploading, update the ImageView on the main screen with the new image
                    Glide.with(this)
                        .load(uploadedImageUrl) // Use the uploaded image URL from Firebase
                        .placeholder(R.drawable.avatar) // Placeholder image while loading
                        .error(R.drawable.avatar) // Error image if loading fails
                        .into(bindings.profileAvatar) // Update main avatar
                }
            }
        }
    }

    private fun uploadAvatarToFirebase(imageUri: Uri, onUploadSuccess: (String) -> Unit) {
        val userUid = auth.currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().getReference("avatars/$userUid.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val uploadedImageUrl = uri.toString()

                    // Update FirebaseAuth user profile with the new image URL
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setPhotoUri(uri)
                        .build()

                    auth.currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            updateAvatarInDatabase(uploadedImageUrl) // Update database
                            onUploadSuccess(uploadedImageUrl) // Return the uploaded image URL
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload avatar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateAvatarInDatabase(imageUrl: String) {
        val userUid = auth.currentUser?.uid ?: return

        val userRef = FirebaseDatabase.getInstance().getReference("users/$userUid")
        userRef.child("avatarUrl").setValue(imageUrl)
            .addOnSuccessListener {
                Toast.makeText(this, "Avatar updated in database", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update database: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateDateTime() {
        val currentDate = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())

        binding.UserdateTextView.text = currentDate
        binding.usertimeTextView.text = currentTime
    }

    // Open the navigation drawer
    fun openDrawer(view: View) {
        if (!binding.UserDrawer.isDrawerOpen(GravityCompat.START)) {
            binding.UserDrawer.openDrawer(GravityCompat.START)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateDateTimeRunnable) // Stop updating when the activity is destroyed
    }
}
