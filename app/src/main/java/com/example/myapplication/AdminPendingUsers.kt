package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import com.example.myapplication.databinding.ActivityPendingUsersBinding
import com.example.myapplication.databinding.TableRowPendingUserBinding

class AdminPendingUsers : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var binding: ActivityPendingUsersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("pending_users")

        // Back button navigation
        binding.backBtnUsers.setOnClickListener {
            startActivity(Intent(this, UserList::class.java))
            finish()
        }

        // Fetch pending users when the activity is created
        fetchPendingUsers()
    }

    // Function to fetch pending users from Firebase
    private fun fetchPendingUsers() {
        binding.tableLayoutPending.removeAllViews() // Clear previous rows

        // Fetch pending users
        database.orderByChild("status").equalTo("pending").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val pendingUser = userSnapshot.getValue(PendingUser::class.java)
                        pendingUser?.let {
                            // Add user data to the table
                            addItemToTable(it, userSnapshot.key ?: "")
                        }
                    }
                } else {
                    Toast.makeText(this@AdminPendingUsers, "No pending users", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminPendingUsers, "Failed to fetch pending users", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Function to add a pending user to the table
    private fun addItemToTable(item: PendingUser, userId: String) {
        // Inflate the table row layout using View Binding
        val tableRowBinding = TableRowPendingUserBinding.inflate(layoutInflater)

        // Get references to the views from the ViewBinding class
        val nameTextView = tableRowBinding.textViewName
        val emailTextView = tableRowBinding.textViewEmail
        val approveTextView = tableRowBinding.textViewApprove
        val rejectTextView = tableRowBinding.textViewReject

        // Set the user data to the TextViews
        nameTextView.text = item.name
        emailTextView.text = item.email

        approveTextView.setOnClickListener {
            approveUser(userId, item)
        }

        rejectTextView.setOnClickListener {
            rejectUser(userId)
            // Remove the row from the TableLayout
            binding.tableLayoutPending.removeView(tableRowBinding.root)
        }

        // Add the row to the table layout
        binding.tableLayoutPending.addView(tableRowBinding.root)
    }


    // Function to approve a user
    private fun approveUser(userId: String, pendingUser: PendingUser) {
        val pendingUserRef = FirebaseDatabase.getInstance().getReference("pending_users").child(userId)
        val usersRef = FirebaseDatabase.getInstance().getReference("users")

        // Ensure password is not null or blank
        if (pendingUser.password.isNullOrBlank()) {
            Toast.makeText(this@AdminPendingUsers, "Password is missing for the pending user.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create the Firebase Auth account
        auth.createUserWithEmailAndPassword(pendingUser.email!!, pendingUser.password!!).addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                val user = auth.currentUser
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(pendingUser.name)
                    .build()

                user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                    if (profileTask.isSuccessful) {
                        val approvedUser = mapOf(
                            "uid" to user?.uid,
                            "name" to pendingUser.name,
                            "email" to pendingUser.email,
                            "role" to "user",
                            "status" to "approved"
                        )

                        user?.uid?.let { uid ->
                            usersRef.child(uid).setValue(approvedUser).addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    // Remove the user from the "pending_users" node
                                    pendingUserRef.removeValue().addOnCompleteListener { removeTask ->
                                        if (removeTask.isSuccessful) {
                                            Toast.makeText(this@AdminPendingUsers, "User approved and moved to active users.", Toast.LENGTH_SHORT).show()
                                            fetchPendingUsers() // Refresh the list
                                        } else {
                                            Toast.makeText(this@AdminPendingUsers, "Failed to remove user from pending list", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(this@AdminPendingUsers, "Failed to approve user.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        // Failed to update profile
                        Toast.makeText(this@AdminPendingUsers, "Failed to update user profile.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Firebase Auth user creation failed
                Toast.makeText(this@AdminPendingUsers, "Failed to create user in Firebase Auth: ${authTask.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }






    // Function to reject a user
    private fun rejectUser(userId: String) {

        // Remove the user
        FirebaseDatabase.getInstance().getReference("pending_users").child(userId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@AdminPendingUsers, "User rejected", Toast.LENGTH_SHORT).show()
                fetchPendingUsers() // Refresh the list
            } else {
                Toast.makeText(this@AdminPendingUsers, "Failed to reject user", Toast.LENGTH_SHORT).show()
            }
        }
    }
}