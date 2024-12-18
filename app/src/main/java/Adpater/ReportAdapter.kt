package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemReportBinding
import java.text.SimpleDateFormat
import java.util.*

class ReportAdapter(
    private val reportList: List<Report>,
    private val context: Context
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reportList[position]

        // Format timestamp into date and time
        val formattedDate = formatTimestampToDate(report.timestamp)
        val formattedTime = formatTimestampToTime(report.timestamp)

        // Bind report data to UI
        holder.binding.apply {
            reportUnitId.text = "Unit ID: ${report.unitID}"
            reportSummary.text = "Reason: ${report.reason}"
            reportDate.text = "Date: $formattedDate"
            reportTime.text = "Time: $formattedTime"
        }
    }

    override fun getItemCount(): Int = reportList.size

    class ReportViewHolder(val binding: ItemReportBinding) : RecyclerView.ViewHolder(binding.root)

    // Helper function to format the timestamp into a date string
    private fun formatTimestampToDate(timestamp: Any?): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = timestampToDate(timestamp)
            sdf.format(date)
        } catch (e: Exception) {
            "Unknown Date"
        }
    }

    // Helper function to format the timestamp into a time string
    private fun formatTimestampToTime(timestamp: Any?): String {
        return try {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val date = timestampToDate(timestamp)
            sdf.format(date)
        } catch (e: Exception) {
            "Unknown Time"
        }
    }

    // Helper function to convert the timestamp into a Date object
    private fun timestampToDate(timestamp: Any?): Date {
        return when (timestamp) {
            is Long -> Date(timestamp)
            is Map<*, *> -> {
                // If using Firebase ServerValue.TIMESTAMP, it may return a Map
                val timeLong = (timestamp["timestamp"] as? Long) ?: 0L
                Date(timeLong)
            }
            else -> Date() // Default to the current date if unknown
        }
    }
}
