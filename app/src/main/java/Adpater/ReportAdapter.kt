package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemReportBinding
import com.example.myapplication.databinding.ReportAdminBinding
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

            // Format and display the timestamp as a readable date
            report.timestamp?.let {
                reportDate.text = "Date: ${formatTimestamp(it)}"
            }

            // Other report fields
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
                    btnClose.setOnClickListener { dialog.dismiss() }
                }
                dialog.show()
            }
        }
    }

    override fun getItemCount(): Int = reportList.size

    class ReportViewHolder(val binding: ItemReportBinding) : RecyclerView.ViewHolder(binding.root)
}


