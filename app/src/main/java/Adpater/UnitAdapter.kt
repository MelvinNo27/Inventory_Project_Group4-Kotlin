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

    inner class UnitViewHolder(private val binding: ListItemUnitBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(unit: UnitClass, position: Int) {
            val unitLabel = "Unit ${position + 1}"
            binding.unitName.text = unitLabel

            // Check if any quantity is zero or null
            val hasEmptyQuantity = unit.monitorQuantity == 0 ||
                    unit.mouseQuantity == 0 ||
                    unit.keyboardQuantity == 0 ||
                    unit.mousePadQuantity == 0 ||
                    unit.unitQuantity == 0

            // Set the unit name with error sign if needed
            binding.unitName.text = if (hasEmptyQuantity) {
                "Unit ${position + 1} ⚠️"  // Add warning emoji for error
                // Or alternatively: "Unit ${position + 1} ❌" // Red X mark
                // Or: "⚠️ Unit ${position + 1}"  // Warning at start
            } else {
                "Unit ${position + 1}"
            }

            binding.root.setOnClickListener {
                if (context is RoomLayout) {
                    context.showUnitDetailsDialog(unit, position)
                }
            }
        }
    }
    }


