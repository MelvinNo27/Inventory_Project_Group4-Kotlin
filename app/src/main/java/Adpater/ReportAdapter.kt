package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemReportBinding
import com.example.myapplication.databinding.ReportAdminBinding
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportAdapter(
    private val reportList: List<Report>,
    private val context: Context
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(context), parent, false)
        return ReportViewHolder(binding)
    }

    private fun formatTimestamp(timestamp: Any?): String {
        if (timestamp is Map<*, *>) {
            // Firebase returns ServerValue.TIMESTAMP as a Map<String, Long>
            val timestampLong = timestamp["timestamp"] as? Long
            timestampLong?.let {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                return sdf.format(Date(it))
            }
        }
        return "Unknown Date"
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reportList[position]

        // Bind the data to the views
        holder.binding.apply {
            reportUnitId.text = "Unit ID: ${report.unitID}"
            reportSummary.text = "Reason: ${report.description}"

            report.timestamp?.let {
                reportDate.text = "Date: ${formatTimestamp(it)}"
            }

            reportTime.text = "Time: ${report.timestamp?.let { formatTimestamp(it).split(" ")[1] }}"

            // Handle clicks on the item
            root.setOnClickListener {
                val dialogBinding = ReportAdminBinding.inflate(LayoutInflater.from(context))
                val dialog = AlertDialog.Builder(context)
                    .setView(dialogBinding.root)
                    .create()

                dialogBinding.apply {
                    dialogUnitId.text = "Unit ID: ${report.unitID}"
                    dialogReason.text = "Reason: ${report.description}"
                    report.timestamp?.let {
                        dialogDate.text = "Date: ${formatTimestamp(it)}"
                        dialogTime.text = "Time: ${formatTimestamp(it).split(" ")[1]}"
                    }

                    // Set initial state of radio buttons based on report's data
                    if (report.isComplete) {
                        dialogComplete.isChecked = true
                    } else {
                        dialogRepairInProcess.isChecked = true
                    }

                    // Handle RadioButton changes
                    dialogComplete.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            // Update the report's status to "Complete"
                            report.isComplete = true
                            report.isRepairInProcess = false
                            updateReportStatus(report)
                        }
                    }

                    dialogRepairInProcess.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            // Update the report's status to "Repair in Process"
                            report.isRepairInProcess = true
                            report.isComplete = false
                            updateReportStatus(report)
                        }
                    }

                    // Update the database when the update button is clicked
                    btnUpdate.setOnClickListener {
                        dialog.dismiss()
                    }
                }

                dialog.show()
            }
        }
    }

    // Function to update the report's status in Firebase Realtime Database
    private fun updateReportStatus(report: Report) {
        val database = FirebaseDatabase.getInstance()
        val reportRef = database.getReference("reports") // Path to your reports in Firebase
        reportRef.child(report.unitID.toString()).setValue(report)
            .addOnSuccessListener {
                // Handle success (e.g., show a toast or log)
                println("Report status updated successfully.")
            }
            .addOnFailureListener { exception ->
                // Handle failure (e.g., show a toast or log the error)
                println("Failed to update report: ${exception.message}")
            }
    }

    override fun getItemCount(): Int = reportList.size

    class ReportViewHolder(val binding: ItemReportBinding) : RecyclerView.ViewHolder(binding.root)
}
