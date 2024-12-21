package com.example.myapplication

import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.UserReportItemBinding

class UserReportAdapter(private val reportList: List<Report>) :
    RecyclerView.Adapter<UserReportAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: UserReportItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(report: Report) {
            with(binding) {
                // Set the room number and unit name
                tvReportTitle.text = "${report.roomNumber} - ${report.unitName}"

                // Set equipment details
                val equipmentDetails = StringBuilder()

                if (report.monitorQuantity != null && report.monitorQuantity == 0) {
                    equipmentDetails.append("Monitor: ${report.monitorID}\n")
                }
                if (report.mouseQuantity != null && report.mouseQuantity == 0) {
                    equipmentDetails.append("Mouse: ${report.mouseID}\n")
                }
                if (report.keyboardQuantity != null && report.keyboardQuantity == 0) {
                    equipmentDetails.append("Keyboard: ${report.keyboardID}\n")
                }
                if (report.mousePadQuantity != null && report.mousePadQuantity == 0) {
                    equipmentDetails.append("Mouse Pad: ${report.mousePadID}\n")
                }
                if (report.unitQuantity != null && report.unitQuantity == 0) {
                    equipmentDetails.append("Unit: ${report.unitID}")
                }

                tvEquipmentDetails.text = equipmentDetails.toString().trim()

                // Set reason
                tvReportReason.text = "Reason: ${report.reason}"

                // Set status with different colors
                val status = report.status ?: "Pending"
                tvReportStatus.text = "Status: $status"

                // Set status color based on the status
                tvReportStatus.setTextColor(when(status) {
                    "Issue Noted" -> android.graphics.Color.BLUE
                    "Repair in Process" -> android.graphics.Color.parseColor("#FFA500") // Orange
                    "Complete" -> android.graphics.Color.GREEN
                    else -> android.graphics.Color.GRAY // For Pending
                })
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = UserReportItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(reportList[position])
    }
    class SwipeToDeleteCallback(private val adapter: UserReportAdapter) :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val report = adapter.getItem(position)

            // Check if the status is "Complete"
            if (report.status == "Complete") {
                // Show confirmation dialog
                val context = viewHolder.itemView.context
                androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete this report?")
                    .setPositiveButton("Yes") { _, _ ->
                        adapter.removeItem(position) // Proceed with deletion
                    }
                    .setNegativeButton("No") { _, _ ->
                        adapter.notifyItemChanged(position) // Reset the swipe
                    }
                    .setCancelable(false) // Ensure the user must make a choice
                    .show()
            } else {
                // Reset the swipe if the status is not "Complete"
                adapter.notifyItemChanged(position)
            }
        }
    }

    fun getItem(position: Int): Report {
        return reportList[position]
    }

    fun removeItem(position: Int) {
        // Assuming reportList is mutable, otherwise convert it to a MutableList
        (reportList as MutableList).removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount() = reportList.size
}