package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityAddUnitBinding
import com.example.myapplication.databinding.ActivityEditUnitBinding
import com.example.myapplication.databinding.ActivityRoomLayoutBinding
import com.example.myapplication.databinding.ActivityUnitDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RoomLayout : AppCompatActivity() {

    private lateinit var binding: ActivityRoomLayoutBinding
    private lateinit var unitAdapter: UnitAdapter
    private val unitList = mutableListOf<UnitClass>()
    private lateinit var database: FirebaseDatabase
    private lateinit var unitsRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the room ID and name from the Intent
        val roomId = intent.getStringExtra("ROOM_ID") ?: ""
        val roomName = intent.getStringExtra("ROOM_NAME") ?: "Unknown Room"

        // Display the room name
        binding.textViewRoom.text = roomName

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            checkIfUserIsAdmin(currentUser.uid)
        } else {
            binding.addBtn.visibility = android.view.View.VISIBLE
        }

        database = FirebaseDatabase.getInstance()
        unitsRef = database.reference.child("units").child(roomId)

        binding.RoomBack.setOnClickListener {
            setResult(RESULT_OK)  // Set the result to OK when navigating back
            finish()  // Close the activity and return to SelectRooms
        }

        setupRecyclerView()
        setupAddUnitButton()
        loadUnitsFromFirebase()
    }


    private fun checkIfUserIsAdmin(userId: String) {
        val database = FirebaseDatabase.getInstance().reference

        // Check if the user is an admin by querying the "admins" node
        database.child("admins").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.addBtn.visibility = android.view.View.VISIBLE
                } else {

                    binding.addBtn.visibility = android.view.View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Error checking admin status: ${error.message}")
            }
        })
    }


    private fun setupRecyclerView() {
        unitAdapter = UnitAdapter(unitList, this) // Pass 'this' (RoomLayout) as context
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = unitAdapter
    }


    private fun setupAddUnitButton() {
        binding.addBtn.setOnClickListener {
            showAddUnitDialog()
        }
    }

    private fun showAddUnitDialog() {
        val dialogBinding = ActivityAddUnitBinding.inflate(LayoutInflater.from(this))

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnAddUnit.setOnClickListener {
            if (validateInputs(dialogBinding)) {
                val unit = createUnitFromInputs(dialogBinding)
                saveUnitToFirebase(unit)
                dialog.dismiss()
            } else {
                showToast("Please fill in all fields")
            }
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun validateInputs(dialogBinding: ActivityAddUnitBinding): Boolean {
        return dialogBinding.run {
            monitorID.text.isNotEmpty() && mouseID.text.isNotEmpty() &&
                    keyboardID.text.isNotEmpty() && mousePadID.text.isNotEmpty() &&
                    unitID.text.isNotEmpty() && MonitorQuantity.text.isNotEmpty() &&
                    MouseQuantity.text.isNotEmpty() && KeyboardQuantity.text.isNotEmpty() &&
                    mousePadQuantity.text.isNotEmpty() && unitQuantity.text.isNotEmpty()
        }
    }

    private fun createUnitFromInputs(dialogBinding: ActivityAddUnitBinding, existingUnit: UnitClass? = null): UnitClass {
        return dialogBinding.run {
            UnitClass(
                monitorID.text.toString().toInt(),
                mouseID.text.toString().toInt(),
                keyboardID.text.toString().toInt(),
                mousePadID.text.toString().toInt(),
                existingUnit?.unitID ?: unitID.text.toString().toInt(), // Use existing unitID if editing
                MonitorQuantity.text.toString().toInt(),
                MouseQuantity.text.toString().toInt(),
                KeyboardQuantity.text.toString().toInt(),
                mousePadQuantity.text.toString().toInt(),
                unitQuantity.text.toString().toInt(),
            )
        }
    }

    private fun saveUnitToFirebase(unit: UnitClass) {
        val roomId = intent.getStringExtra("ROOM_ID") ?: ""

        if (roomId.isNotEmpty()) {
            val unitsRefForRoom = database.reference.child("units").child(roomId)
            val newUnitRef = unitsRefForRoom.push()
            newUnitRef.setValue(unit).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Unit added successfully")
                    loadUnitsFromFirebase()
                    unitList.add(unit)
                    unitAdapter.notifyDataSetChanged()
                } else {
                    showToast("Failed to add unit")
                }
            }
        } else {
            showToast("Room ID is missing")
        }
    }

    private fun loadUnitsFromFirebase() {
        val roomId = intent.getStringExtra("ROOM_ID") ?: ""
        if (roomId.isNotEmpty()) {
            val unitsRefForRoom = database.reference.child("units").child(roomId)

            unitsRefForRoom.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    unitList.clear()
                    snapshot.children.forEach { unitSnapshot ->
                        val unit = unitSnapshot.getValue(UnitClass::class.java)
                        unit?.let { unitList.add(it) }
                    }
                    unitAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    showToast("Error loading units: ${error.message}")
                }
            })
        } else {
            showToast("Room ID is missing")
        }
    }

    fun showUnitDetailsDialog(unit: UnitClass) {
        val dialogBinding = ActivityUnitDetailBinding.inflate(LayoutInflater.from(this))

        dialogBinding.ListMonitorID.text = unit.monitorID.toString()
        dialogBinding.ListMouseID.text = unit.mouseID.toString()
        dialogBinding.ListKeyboardID.text = unit.keyboardID.toString()
        dialogBinding.ListMousePadID.text = unit.mousePadID.toString()
        dialogBinding.ListUnitID.text = unit.unitID.toString()
        dialogBinding.MonitorQuantity.text = unit.monitorQuantity.toString()
        dialogBinding.MouseQuantity.text = unit.mouseQuantity.toString()
        dialogBinding.KeyboardQuantity.text = unit.keyboardQuantity.toString()
        dialogBinding.mousePadQuantity.text = unit.mousePadQuantity.toString()
        dialogBinding.unitQuantity.text = unit.unitQuantity.toString()

        dialogBinding.ListMonitorID.isEnabled = false
        dialogBinding.ListMouseID.isEnabled = false
        dialogBinding.ListKeyboardID.isEnabled = false
        dialogBinding.ListMousePadID.isEnabled = false
        dialogBinding.ListUnitID.isEnabled = false
        dialogBinding.MonitorQuantity.isEnabled = false
        dialogBinding.MouseQuantity.isEnabled = false
        dialogBinding.KeyboardQuantity.isEnabled = false
        dialogBinding.mousePadQuantity.isEnabled = false
        dialogBinding.unitQuantity.isEnabled = false

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnEdit.setOnClickListener {
            showEditUnitDialog(unit)
            dialog.dismiss()
        }

        dialogBinding.RoomBack.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }



        fun showEditUnitDialog(unit: UnitClass) {
        val dialogBinding = ActivityEditUnitBinding.inflate(LayoutInflater.from(this))

        // Pre-fill the fields with the existing unit data
        dialogBinding.editMonitorID.setText(unit.monitorID.toString())
        dialogBinding.editMouseID.setText(unit.mouseID.toString())
        dialogBinding.editKeyboardID.setText(unit.keyboardID.toString())
        dialogBinding.editMousePadID.setText(unit.mousePadID.toString())
        dialogBinding.editUnitID.setText(unit.unitID.toString())
        dialogBinding.editMonitorQuantity.setText(unit.monitorQuantity.toString())
        dialogBinding.editMouseQuantity.setText(unit.mouseQuantity.toString())
        dialogBinding.editKeyboardQuantity.setText(unit.keyboardQuantity.toString())
        dialogBinding.editMousePadQuantity.setText(unit.mousePadQuantity.toString())
        dialogBinding.editUnitQuantity.setText(unit.unitQuantity.toString())

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnSave.setOnClickListener {
            if (validateInputs(dialogBinding)) {
                val updatedUnit = createUnitFromInputs(dialogBinding, unit) // Pass the current unit to preserve the unitID
                updateUnitInFirebase(unit, updatedUnit)
                dialog.dismiss()
            } else {
                showToast("Please fill in all fields")
            }
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun createUnitFromInputs(dialogBinding: ActivityEditUnitBinding, existingUnit: UnitClass): UnitClass {
        return dialogBinding.run {
            UnitClass(
                monitorID = editMonitorID.text.toString().toIntOrNull() ?: 0,  // Default to 0 if input is invalid
                mouseID = editMouseID.text.toString().toIntOrNull() ?: 0,
                keyboardID = editKeyboardID.text.toString().toIntOrNull() ?: 0,
                mousePadID = editMousePadID.text.toString().toIntOrNull() ?: 0,
                unitID = existingUnit.unitID,
                monitorQuantity = editMonitorQuantity.text.toString().toIntOrNull() ?: 0,
                mouseQuantity = editMouseQuantity.text.toString().toIntOrNull() ?: 0,
                keyboardQuantity = editKeyboardQuantity.text.toString().toIntOrNull() ?: 0,
                mousePadQuantity = editMousePadQuantity.text.toString().toIntOrNull() ?: 0,
                unitQuantity = editUnitQuantity.text.toString().toIntOrNull() ?: 0
            )
        }
    }




    private fun validateInputs(dialogBinding: ActivityEditUnitBinding): Boolean {
        return dialogBinding.run {
            // Check if all fields are filled
            editMonitorID.text.isNotEmpty() &&
                    editMouseID.text.isNotEmpty() &&
                    editKeyboardID.text.isNotEmpty() &&
                    editMousePadID.text.isNotEmpty() &&
                    editUnitID.text.isNotEmpty() &&
                    editMonitorQuantity.text.isNotEmpty() &&
                    editMouseQuantity.text.isNotEmpty() &&
                    editKeyboardQuantity.text.isNotEmpty() &&
                    editMousePadQuantity.text.isNotEmpty() &&
                    editUnitQuantity.text.isNotEmpty()
        }
    }


    private fun updateUnitInFirebase(oldUnit: UnitClass, updatedUnit: UnitClass) {
        val roomId = intent.getStringExtra("ROOM_ID") ?: ""

        if (roomId.isNotEmpty()) {
            val unitsRefForRoom = database.reference.child("units").child(roomId)

            // Locate the unit in Firebase using the unitID
            unitsRefForRoom.orderByChild("unitID").equalTo(oldUnit.unitID.toDouble()).limitToFirst(1)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val unitKey = snapshot.children.firstOrNull()?.key
                        if (unitKey != null) {
                            unitsRefForRoom.child(unitKey).setValue(updatedUnit)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        showToast("Unit updated successfully")
                                        loadUnitsFromFirebase()  // Reload the list after update
                                    } else {
                                        showToast("Failed to update unit")
                                    }
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        showToast("Error updating unit: ${error.message}")
                    }
                })
        } else {
            showToast("Room ID is missing")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

