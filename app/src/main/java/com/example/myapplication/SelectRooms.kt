package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
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
        setupSwipeToDelete()
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

    private fun setupSwipeToDelete() {
        val swipeHandler = SwipeToDeleteCallback(adapter, this) { position ->
            val roomToDelete = roomList[position]
            deleteRoom(roomToDelete)
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.roomRecyclerView)
    }

    private fun deleteRoom(room: Rooms) {
        // First check if there are any units in this room
        val unitsRef = FirebaseDatabase.getInstance().reference.child("units").child(room.id)
        unitsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // If units exist, delete them first
                    unitsRef.removeValue().addOnSuccessListener {
                        // After units are deleted, delete the room
                        deleteRoomFromDatabase(room)
                    }.addOnFailureListener { e ->
                        showDialog("Error", "Failed to delete room units: ${e.message}")
                    }
                } else {
                    // If no units exist, directly delete the room
                    deleteRoomFromDatabase(room)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showDialog("Error", "Failed to check room units: ${error.message}")
            }
        })
    }

    private fun deleteRoomFromDatabase(room: Rooms) {
        database.child(room.id).removeValue()
            .addOnSuccessListener {
                showDialog("Success", "Room deleted successfully")
            }
            .addOnFailureListener { e ->
                showDialog("Error", "Failed to delete room: ${e.message}")
            }
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
                showDialog("Error", "Room number cannot be empty.")
            }
            dialog.dismiss()
        }

        dialogBuilder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        dialogBuilder.show()
    }

    private fun saveRoomToFirebase(roomNumber: String) {
        database.orderByChild("name").equalTo("Room $roomNumber")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        showDialog("Error", "Room number already exists. Please enter a different number.")
                    } else {
                        val roomId = database.push().key ?: return
                        val roomName = "Room $roomNumber"
                        val room = Rooms(id = roomId, name = roomName)

                        database.child(roomId).setValue(room).addOnSuccessListener {
                            showDialog("Success", "Room added successfully.")
                        }.addOnFailureListener {
                            showDialog("Error", "Failed to add room.")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showDialog("Error", "Error checking room number: ${error.message}")
                }
            })
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
                showDialog("Error", "Error loading rooms: ${error.message}")
            }
        })
    }

    private fun navigateToRoomLayout(room: Rooms) {
        val intent = Intent(this, RoomLayout::class.java).apply {
            putExtra("ROOM_ID", room.id)
            putExtra("ROOM_NAME", room.name)
        }
        startActivityForResult(intent, ROOM_LAYOUT_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ROOM_LAYOUT_REQUEST_CODE && resultCode == RESULT_OK) {
            loadRoomsFromFirebase()
        }
    }

    private fun checkUserRole(userId: String) {
        val database = FirebaseDatabase.getInstance().reference
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
                showDialog("Error", "Error checking admin status: ${error.message}")
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

                    if (role == "user") {
                        startActivity(Intent(this@SelectRooms, Userdashboard::class.java))
                        finish()
                    } else {
                        showDialog("Error", "Role not recognized.")
                    }
                } else {
                    showDialog("Error", "User not found.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showDialog("Error", "Error fetching user data: ${error.message}")
            }
        })
    }

    private fun showDialog(title: String, message: String) {
        val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(this)
        dialogBuilder.setTitle(title)
        dialogBuilder.setMessage(message)
        dialogBuilder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        dialogBuilder.show()
    }
}
