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
import com.example.myapplication.databinding.ActivityAdminBinding
import com.example.myapplication.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminDashboard : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding // ViewBinding instance
    private val handler = android.os.Handler()
    private lateinit var rootDatabaseRef: DatabaseReference
    private lateinit var currentProfileDialog: AlertDialog
    private lateinit var currentProfileBinding: ActivityProfileBinding

    private val updateDateTimeRunnable: Runnable = object : Runnable {
        override fun run() {
            updateDateTime()  // Call the method to update both date and time
            handler.postDelayed(this, 1000) // Update every second
        }
    }

    private val imagePickerRequestCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootDatabaseRef = FirebaseDatabase.getInstance().getReference("MyData") // Correct database reference

        // Initialize ViewBinding
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Log out button click
        binding.logOutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding.profile.setOnClickListener {
            showUserProfileDialog()
        }

        binding.btnReports.setOnClickListener {
            startActivity(Intent(this, Reports::class.java))
            finish()
        }

        // Instructors button click
        binding.btnInstructors.setOnClickListener {
            startActivity(Intent(this, UserList::class.java))
            finish()
        }

        // Rooms button click
        binding.btnRooms.setOnClickListener {
            startActivity(Intent(this, SelectRooms::class.java))
            finish()
        }

        // Start the update of date and time every second
        handler.post(updateDateTimeRunnable)
    }

    private fun showLogoutConfirmationDialog() {
        // Create and show the confirmation dialog
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to log out?")
            .setCancelable(false) // Prevent dialog from being dismissed by tapping outside
            .setPositiveButton("Yes") { dialog, _ ->
                logout()
                dialog.dismiss() // Dismiss the dialog after confirming
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss() // Dismiss the dialog without logging out
            }
            .show()
    }

    private fun logout() {
        // Sign out from Firebase Authentication
        FirebaseAuth.getInstance().signOut()

        // Optionally, clear SharedPreferences or any other user data
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()  // Clear all stored preferences
        editor.apply()

        // Show a toast message to inform the user they have logged out
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Redirect to the Login activity
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()  // Close the AdminDashboard activity
    }

    private fun updateDateTime() {
        // Get the current date and time
        val currentDate = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())

        // Update the TextViews with current date and time
        binding.dateTextView.text = currentDate
        binding.timeTextView.text = currentTime
    }

    private fun showUserProfileDialog() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val userEmail = currentUser.email ?: "No email"
            val userUid = currentUser.uid

            currentProfileBinding = ActivityProfileBinding.inflate(layoutInflater)

            // Load existing avatar
            val avatarUrl = currentUser.photoUrl?.toString()
            if (avatarUrl != null) {
                Glide.with(this)
                    .load(avatarUrl)
                    .error(R.drawable.avatar1) // Add a default avatar drawable
                    .into(currentProfileBinding.profileAvatar)
            }

            currentProfileDialog = AlertDialog.Builder(this)
                .setTitle("User Profile")
                .setView(currentProfileBinding.root)
                .setPositiveButton("Close") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .create()

            // Fetch admin's name
            rootDatabaseRef.child(userUid).child("name").get()
                .addOnSuccessListener { snapshot ->
                    val adminName = snapshot.value as? String ?: "Unknown"
                    currentProfileBinding.profileName.text = "Name: $adminName"
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to fetch user name: ${exception.message}", Toast.LENGTH_SHORT).show()
                    currentProfileBinding.profileName.text = "Name: Error"
                }

            currentProfileBinding.profileEmail.text = "Email: $userEmail"
            currentProfileBinding.profileUid.text = "UID: $userUid"

            currentProfileBinding.editAvatarButton.setOnClickListener {
                selectAvatarImage()
            }

            currentProfileBinding.forgotPasswordText.setOnClickListener {
                val intent = Intent(this, ForgotPasswordActivity::class.java)
                startActivity(intent)
                currentProfileDialog.dismiss()
            }

            currentProfileDialog.show()
        } else {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectAvatarImage() {
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, imagePickerRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == imagePickerRequestCode && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            // Show selected image immediately
            Glide.with(this)
                .load(imageUri)
                .error(R.drawable.avatar1)
                .into(currentProfileBinding.profileAvatar)
            uploadAvatarToFirebase(imageUri)
        }
    }


    private fun uploadAvatarToFirebase(imageUri: Uri?) {
        if (imageUri != null) {
            val user = FirebaseAuth.getInstance().currentUser
            val storageRef = FirebaseStorage.getInstance().reference.child("avatars/${user?.uid}.jpg")

            // Show loading state
            currentProfileBinding.editAvatarButton.isEnabled = false
            currentProfileBinding.editAvatarButton.text = "Uploading..."

            val uploadTask = storageRef.putFile(imageUri)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    updateUserProfileWithAvatar(uri.toString())
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Avatar upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                // Reset button state
                currentProfileBinding.editAvatarButton.isEnabled = true
                currentProfileBinding.editAvatarButton.text = "Edit Avatar"
            }
        }
    }

    private fun updateUserProfileWithAvatar(avatarUrl: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(Uri.parse(avatarUrl))
            .build()

        user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Avatar updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error updating avatar", Toast.LENGTH_SHORT).show()
            }
            // Reset button state
            currentProfileBinding.editAvatarButton.isEnabled = true
            currentProfileBinding.editAvatarButton.text = "Edit Avatar"
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateDateTimeRunnable) // Stop updating when the activity is destroyed
    }

    // Method to open the navigation drawer
    fun openDrawer(view: View) {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    // Handle back press to exit app (close all activities)
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity() // Close all activities and exit the app
    }
}
