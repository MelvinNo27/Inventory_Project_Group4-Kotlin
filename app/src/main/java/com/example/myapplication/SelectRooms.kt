package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivitySelectRoomBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SelectRooms : AppCompatActivity() {

    private lateinit var binding: ActivitySelectRoomBinding
    private lateinit var database: DatabaseReference
    private val roomList = mutableListOf<Rooms>()
    private lateinit var adapter: RoomAdapter
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val ROOM_LAYOUT_REQUEST_CODE = 1001  // Request code to return from RoomLayout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySelectRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("rooms")
        auth = FirebaseAuth.getInstance()

        binding.selectRoomBack.setOnClickListener {
            checkUserRole(auth.currentUser!!.uid)
        }

        val showAddRoomButton = intent.getBooleanExtra("showAddRoomButton", true)

        if (!showAddRoomButton) {
            binding.addRoomButton.visibility = View.GONE
        }

        setupRecyclerView()
        setupAddRoomButton()
        loadRoomsFromFirebase()
    }

    private fun setupRecyclerView() {
        adapter = RoomAdapter(roomList) { room ->
            navigateToRoomLayout(room)
        }
        binding.roomRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.roomRecyclerView.adapter = adapter
    }

    private fun setupAddRoomButton() {
        binding.addRoomButton.setOnClickListener {
            showAddRoomDialog()
        }
    }

    private fun showAddRoomDialog() {
        val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(this)
        dialogBuilder.setTitle("Add Room")

        val input = android.widget.EditText(this).apply {
            hint = "Enter Room Number"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        dialogBuilder.setView(input)

        dialogBuilder.setPositiveButton("Add") { dialog, _ ->
            val roomNumber = input.text.toString()
            if (roomNumber.isNotBlank()) {
                saveRoomToFirebase(roomNumber)
            } else {
                showToast("Room number cannot be empty")
            }
            dialog.dismiss()
        }

        dialogBuilder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        dialogBuilder.show()
    }

    private fun saveRoomToFirebase(roomNumber: String) {
        val roomId = database.push().key ?: return
        val roomName = "Room $roomNumber"
        val room = Rooms(id = roomId, name = roomName)

        database.child(roomId).setValue(room).addOnSuccessListener {
            showToast("Room added successfully")
        }.addOnFailureListener {
            showToast("Failed to add room")
        }
    }

    private fun loadRoomsFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                roomList.clear()
                for (roomSnapshot in snapshot.children) {
                    val room = roomSnapshot.getValue(Rooms::class.java)
                    room?.let { roomList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Error loading rooms: ${error.message}")
            }
        })
    }

    private fun navigateToRoomLayout(room: Rooms) {
        val intent = Intent(this, RoomLayout::class.java).apply {
            putExtra("ROOM_ID", room.id)
            putExtra("ROOM_NAME", room.name)
        }
        startActivityForResult(intent, ROOM_LAYOUT_REQUEST_CODE)  // Use startActivityForResult
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ROOM_LAYOUT_REQUEST_CODE && resultCode == RESULT_OK) {
            // Reload rooms from Firebase when coming back from RoomLayout
            loadRoomsFromFirebase()
        }
    }

    private fun checkUserRole(userId: String) {
        val database = FirebaseDatabase.getInstance().reference

        // Check if the user is an admin first
        database.child("admins").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    startActivity(Intent(this@SelectRooms, AdminDashboard::class.java))
                    finish()
                } else {
                    checkUserInUsersNode(userId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Error checking admin status: ${error.message}")
            }
        })
    }

    private fun checkUserInUsersNode(userId: String) {
        val database = FirebaseDatabase.getInstance().reference
        val userRef = database.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val role = snapshot.child("role").getValue(String::class.java)

                    if (role == "User") {
                        startActivity(Intent(this@SelectRooms, Userdashboard::class.java))
                        finish()
                    } else {
                        showToast("Role not recognized.")
                    }
                } else {
                    showToast("User not found.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Error fetching user data: ${error.message}")
            }
        })
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}

