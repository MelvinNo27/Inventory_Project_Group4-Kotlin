package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ListItemUnitBinding

class UnitAdapter(
    private val unitList: MutableList<UnitClass>,
    private val context: Context // Pass the context to show the dialog
) : RecyclerView.Adapter<UnitAdapter.UnitViewHolder>() {

    // Inflate the item layout and return the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitViewHolder {
        val binding = ListItemUnitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UnitViewHolder(binding)
    }

    // Bind the unit data to the view elements
    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        val unit = unitList[position]
        holder.bind(unit, position)
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

            // Set the "View" button's click listener to show unit details
            binding.unitName.setOnClickListener {
                // Show unit details in a dialog instead of navigating to a new activity
                if (context is RoomLayout) {
                    context.showUnitDetailsDialog(unit) // Calling the dialog method from RoomLayout
                }
            }


            }
        }
    }


