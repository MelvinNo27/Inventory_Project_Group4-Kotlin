package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemReportBinding
import com.example.myapplication.databinding.ReportAdminBinding

class ReportAdapter(
    private val reportList: List<Report>,
    private val context: Context
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    // Inflates the layout using View Binding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(context), parent, false)
        return ReportViewHolder(binding)
    }

    // Binds data to the view and handles click events
    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reportList[position]

        // Bind the data to the views
        holder.binding.apply {
            reportUnitId.text = "Unit ID: ${report.unitID}"
            reportSummary.text = "Reason: ${report.description}"
            reportDate.text = "Date: ${report.date}" // Date binding
            reportTime.text = "Time: ${report.time}" // Time binding
        }

        // Handle item click to show dialog with detailed reason
        holder.binding.root.setOnClickListener {
            // Use binding for the dialog layout as well
            val dialogBinding = ReportAdminBinding.inflate(LayoutInflater.from(context))
            val dialog = AlertDialog.Builder(context)
                .setView(dialogBinding.root)
                .create()

            // Bind data to dialog views
            dialogBinding.apply {
                dialogUnitId.text = "Unit ID: ${report.unitID}"
                dialogReason.text = "Reason: ${report.description}"
                dialogDate.text = "Date: ${report.date}" // Dialog Date binding
                dialogTime.text = "Time: ${report.time}" // Dialog Time binding

                // Handle close button click
                btnClose.setOnClickListener {
                    dialog.dismiss()
                }
            }

            dialog.show()
        }
    }

    override fun getItemCount(): Int = reportList.size

    // ViewHolder with View Binding
    class ReportViewHolder(val binding: ItemReportBinding) : RecyclerView.ViewHolder(binding.root)
}
