package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityUnitDetailBinding

class UnitDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUnitDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the binding object
        binding = ActivityUnitDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)  // Set the root view of the binding

        // Get the data from the Intent
        val monitorID = intent.getIntExtra("monitorID", 0)
        val mouseID = intent.getIntExtra("mouseID", 0)
        val keyboardID = intent.getIntExtra("keyboardID", 0)
        val mousePadID = intent.getIntExtra("mousePadID", 0)
        val unitID = intent.getIntExtra("unitID", 0)
        val monitorQuantity = intent.getIntExtra("monitor_quantity", 0)
        val mouseQuantity = intent.getIntExtra("mouse_quantity", 0)
        val keyboardQuantity = intent.getIntExtra("keyboard_quantity", 0)
        val mousePadQuantity = intent.getIntExtra("mousePad_quantity", 0)
        val unitQuantity = intent.getIntExtra("unit_quantity", 0)

        // Use the binding object to display the details in the TextViews
        binding.ListMonitorID.text = monitorID.toString()
        binding.ListMouseID.text = mouseID.toString()
        binding.ListKeyboardID.text = keyboardID.toString()
        binding.ListMousePadID.text = mousePadID.toString()
        binding.ListUnitID.text = unitID.toString()
        binding.MonitorQuantity.text = monitorQuantity.toString()
        binding.MouseQuantity.text = mouseQuantity.toString()
        binding.KeyboardQuantity.text = keyboardQuantity.toString()
        binding.mousePadQuantity.text = mousePadQuantity.toString()
        binding.unitQuantity.text = unitQuantity.toString()
    }
}
