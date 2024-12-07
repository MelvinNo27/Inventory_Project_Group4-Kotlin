package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityAddUnitBinding
import com.example.myapplication.databinding.ActivityRoom14Binding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.selects.select

class Room14 : AppCompatActivity() {

    private lateinit var binding: ActivityRoom14Binding
    private lateinit var unitAdapter: UnitAdapter
    private val unitList = mutableListOf<UnitClass>()
    private lateinit var database: FirebaseDatabase
    private lateinit var unitsRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance()
        unitsRef = database.reference.child("units") // Save units under "units" node in Firebase


        binding = ActivityRoom14Binding.inflate(layoutInflater)
        setContentView(binding.root)


        val type = "Room #"+ intent.getStringExtra("roomNo");

        binding.textViewRoom.setText(type)

                // Initialize RecyclerView and Adapter
        unitAdapter = UnitAdapter(unitList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = unitAdapter

        // Set up back button
        binding.Room14back.setOnClickListener {
            startActivity(Intent(this, SelectRooms::class.java))
            finish()
        }

        // Add button listener
        binding.addBtn.setOnClickListener {
            showAddUnitDialog()
        }

        // Load items from Firebase
        loadItemsFromFirebase()
    }
    private fun showAddUnitDialog() {
        // Inflate the dialog layout using ViewBinding
        val dialogBinding = ActivityAddUnitBinding.inflate(LayoutInflater.from(this))

        // Get references to input fields and buttons using ViewBinding
        val monitorId = dialogBinding.monitorID
        val mouseId = dialogBinding.mouseID
        val keyboardId = dialogBinding.keyboardID
        val mousePadId = dialogBinding.mousePadID
        val unitId = dialogBinding.unitID
        val monitorQuan = dialogBinding.MonitorQuantity
        val mouseQuan = dialogBinding.MouseQuantity
        val keyboardQuan = dialogBinding.KeyboardQuantity
        val mousePadQuan = dialogBinding.mousePadQuantity
        val unitQuan = dialogBinding.unitQuantity
        val btnAddUnit = dialogBinding.btnAddUnit
        val btnCancelUnit = dialogBinding.btnCancel

        // Create the dialog using the ViewBinding layout
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        // Set up the "Add" button click listener
        btnAddUnit.setOnClickListener {
            // Validate fields before adding the unit
            if (monitorId.text.isNullOrEmpty() || mouseId.text.isNullOrEmpty() ||
                keyboardId.text.isNullOrEmpty() || mousePadId.text.isNullOrEmpty() ||
                unitId.text.isNullOrEmpty() || monitorQuan.text.isNullOrEmpty() ||
                mouseQuan.text.isNullOrEmpty() || keyboardQuan.text.isNullOrEmpty() ||
                mousePadQuan.text.isNullOrEmpty() || unitQuan.text.isNullOrEmpty()) {

                // Show toast if any field is empty
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()

            } else {
                // Get user input and create Unit object
                val monitorID = monitorId.text.toString().toIntOrNull() ?: 0
                val mouseID = mouseId.text.toString().toIntOrNull() ?: 0
                val keyboardID = keyboardId.text.toString().toIntOrNull() ?: 0
                val mousePadID = mousePadId.text.toString().toIntOrNull() ?: 0
                val unitID = unitId.text.toString().toIntOrNull() ?: 0
                val monitorQuantity = monitorQuan.text.toString().toIntOrNull() ?: 0
                val mouseQuantity = mouseQuan.text.toString().toIntOrNull() ?: 0
                val keyboardQuantity = keyboardQuan.text.toString().toIntOrNull() ?: 0
                val mousePadQuantity = mousePadQuan.text.toString().toIntOrNull() ?: 0
                val unitQuantity = unitQuan.text.toString().toIntOrNull() ?: 0

                // Create UnitClass object
                val unit = UnitClass(
                    monitorID,
                    mouseID,
                    keyboardID,
                    mousePadID,
                    unitID,
                    monitorQuantity,
                    mouseQuantity,
                    keyboardQuantity,
                    mousePadQuantity,
                    unitQuantity
                )

                unitAdapter.addUnit(unit)
                saveUnitToFirebase(unit)
                dialog.dismiss()
            }
        }


        btnCancelUnit.setOnClickListener {

            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
    }



    // Save unit to Firebase
    private fun saveUnitToFirebase(unit: UnitClass) {
        // Get a unique ID for the unit (e.g., using push() for automatic ID generation)
        val newUnitRef = unitsRef.push()

        // Set the unit data at the new reference
        newUnitRef.setValue(unit).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Successfully added to Firebase
                Toast.makeText(this, "Unit added successfully", Toast.LENGTH_SHORT).show()
            } else {
                // Failed to add to Firebase
                Toast.makeText(this, "Failed to add unit", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Load items from Firebase Realtime Database
    private fun loadItemsFromFirebase() {
        // Fetch data from Firebase
        unitsRef.get().addOnSuccessListener { snapshot ->
            unitList.clear() // Clear the current list
            snapshot.children.forEach { dataSnapshot ->
                val unit = dataSnapshot.getValue(UnitClass::class.java)
                unit?.let { unitList.add(it) } // Add each unit to the list
            }
            unitAdapter.notifyDataSetChanged() // Notify the adapter to update the RecyclerView
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load units", Toast.LENGTH_SHORT).show()
        }
    }
}


