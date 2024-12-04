package com.example.myapplication

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ListItemUnitBinding

class UnitAdapter(private val unitList: MutableList<UnitClass>) : RecyclerView.Adapter<UnitAdapter.UnitViewHolder>() {

    // Inflate the item layout and return the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitViewHolder {
        val binding = ListItemUnitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UnitViewHolder(binding)
    }

    // Bind the unit data to the view elements
    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        val unit = unitList[position]
        holder.bind(unit, position) // Pass position to calculate the "Unit X" label
    }

    // Return the size of the list
    override fun getItemCount(): Int = unitList.size

    // Function to add a unit to the list and update the RecyclerView
    fun addUnit(unit: UnitClass) {
        unitList.add(unit)
        notifyItemInserted(unitList.size - 1) // Notify that a new item was inserted
    }

    // ViewHolder class to bind the data to each item in the RecyclerView
    inner class UnitViewHolder(private val binding: ListItemUnitBinding) : RecyclerView.ViewHolder(binding.root) {

        // Bind the unit data to the view elements
        fun bind(unit: UnitClass, position: Int) {
            // Dynamically generate the unit label like "Unit 1", "Unit 2", etc.
            binding.unitName.text = "Unit ${position + 1}" // Position + 1 to make it 1-based index

            // Set the "View" button's click listener
            binding.unitName.setOnClickListener {
                // When clicked, pass the unit details to the UnitDetailActivity
                val context = binding.root.context
                val intent = Intent(context, UnitDetailActivity::class.java).apply {
                    putExtra("monitorID", unit.monitorID)
                    putExtra("mouseID", unit.mouseID)
                    putExtra("keyboardID", unit.keyboardID)
                    putExtra("mousePadID", unit.mousePadID)
                    putExtra("unitID", unit.unitID)
                    putExtra("monitor_quantity", unit.monitorQuantity)
                    putExtra("mouse_quantity", unit.mouseQuantity)
                    putExtra("keyboard_quantity", unit.keyboardQuantity)
                    putExtra("mousePad_quantity", unit.mousePadQuantity)
                    putExtra("unit_quantity", unit.unitQuantity)
                }
                context.startActivity(intent)
            }
        }
    }
}

