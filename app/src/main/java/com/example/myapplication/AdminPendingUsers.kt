package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import com.example.myapplication.databinding.ActivityPendingUsersBinding

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
        // Inflate the table row layout
        val tableRowView = layoutInflater.inflate(R.layout.table_row_pending_user, null)

        // Get references to the TextViews in the row layout
        val nameTextView = tableRowView.findViewById<TextView>(R.id.text_view_name)
        val emailTextView = tableRowView.findViewById<TextView>(R.id.text_view_email)
        val approveTextView = tableRowView.findViewById<TextView>(R.id.text_view_approve)
        val rejectTextView = tableRowView.findViewById<TextView>(R.id.text_view_reject)

        // Set the user data to the TextViews
        nameTextView.text = item.name
        emailTextView.text = item.email

        // Set click listeners for the buttons
        approveTextView.setOnClickListener {
            approveUser(userId, item)
        }

        rejectTextView.setOnClickListener {
            rejectUser(userId)
            binding.tableLayoutPending.removeView(tableRowView)
        }

        // Add the row to the table layout
        binding.tableLayoutPending.addView(tableRowView)
    }

    // Function to approve a user
    // Function to approve a user
    private fun approveUser(userId: String, pendingUser: PendingUser) {
        // Reference to the "pending_users" node
        val pendingUserRef = FirebaseDatabase.getInstance().getReference("pending_users").child(userId)

        // Reference to the "users" node
        val usersRef = FirebaseDatabase.getInstance().getReference("users")

        // Add the user to the "users" node with an approved status
        val approvedUser = mapOf(
            "id" to userId,
            "name" to pendingUser.name,
            "email" to pendingUser.email,
            "status" to "approved"
        )

        usersRef.child(userId).setValue(approvedUser).addOnCompleteListener { userTask ->
            if (userTask.isSuccessful) {
                // Remove the user from the "pending_users" node
                pendingUserRef.removeValue().addOnCompleteListener { removeTask ->
                    if (removeTask.isSuccessful) {
                        Toast.makeText(this@AdminPendingUsers, "User approved and added to database", Toast.LENGTH_SHORT).show()
                        fetchPendingUsers() // Refresh the list
                    } else {
                        Toast.makeText(this@AdminPendingUsers, "Failed to remove user from pending list", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this@AdminPendingUsers, "Failed to add user to approved list", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to reject a user
    private fun rejectUser(userId: String) {
        // Reference to the "pending_users" node
        val pendingUserRef = FirebaseDatabase.getInstance().getReference("pending_users").child(userId)

        // Remove the user
        pendingUserRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@AdminPendingUsers, "User rejected", Toast.LENGTH_SHORT).show()
                fetchPendingUsers() // Refresh the list
            } else {
                Toast.makeText(this@AdminPendingUsers, "Failed to reject user", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

