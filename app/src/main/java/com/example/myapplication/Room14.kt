package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityRoom14Binding

class Room14 : AppCompatActivity() {

    private lateinit var binding: ActivityRoom14Binding
    private lateinit var unitAdapter: UnitAdapter
    private val unitList = mutableListOf<UnitClass>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRoom14Binding.inflate(layoutInflater)
        setContentView(binding.root)

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
    }

    private fun showAddUnitDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_add_unit, null)

        // Get references to input fields
        val unitNameEditText = dialogView.findViewById<EditText>(R.id.monitorID)
        val unitDescriptionEditText = dialogView.findViewById<EditText>(R.id.mouseID)
        val keyboardEditText = dialogView.findViewById<EditText>(R.id.keyboardID)
        val mousePadEditText = dialogView.findViewById<EditText>(R.id.mousePadID)
        val unitIDEditText = dialogView.findViewById<EditText>(R.id.unitID)
        val monitorQuantityEditText = dialogView.findViewById<EditText>(R.id.MonitorQuantity)
        val mouseQuantityEditText = dialogView.findViewById<EditText>(R.id.MouseQuantity)
        val keyboardQuantityEditText = dialogView.findViewById<EditText>(R.id.KeyboardQuantity)
        val mousePadQuantityEditText = dialogView.findViewById<EditText>(R.id.mousePadQuantity)
        val unitQuantityEditText = dialogView.findViewById<EditText>(R.id.unitQuantity)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val monitorID = unitNameEditText.text.toString().toIntOrNull() ?: 0
                val mouseID = unitDescriptionEditText.text.toString().toIntOrNull() ?: 0
                val keyboardID = unitIDEditText.text.toString().toIntOrNull() ?: 0
                val mousePadID = keyboardEditText.text.toString().toIntOrNull() ?: 0
                val unitID = mousePadEditText.text.toString().toIntOrNull() ?: 0
                val monitorQuantity = monitorQuantityEditText.text.toString().toIntOrNull() ?: 0
                val mouseQuantity = mouseQuantityEditText.text.toString().toIntOrNull() ?: 0
                val keyboardQuantity = keyboardQuantityEditText.text.toString().toIntOrNull() ?: 0
                val mousePadQuantity = mousePadQuantityEditText.text.toString().toIntOrNull() ?: 0
                val unitQuantity = unitQuantityEditText.text.toString().toIntOrNull() ?: 0

                // Create a Unit object
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

                // Add the unit to the list and update the RecyclerView
                unitAdapter.addUnit(unit)

            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }


}


