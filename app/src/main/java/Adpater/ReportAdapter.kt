package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemReportBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportAdapter(
    private val reportList: List<Report>,
    private val context: Context,
    private val clickListener: OnReportClickListener
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    interface OnReportClickListener {
        fun onReportClick(report: Report)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reportList[position]
        val formattedDate = formatTimestampToDate(report.timestamp)
        val formattedTime = formatTimestampToTime(report.timestamp)

        with(holder.binding) {
            reportRoomNumber.text = report.roomNumber
            reportDate.text = "Date: $formattedDate"
            reportTime.text = "Time: $formattedTime"
            reportUnitNumber.text = report.unitName ?: "Unknown Unit"

            // Set click listener on the item view
            root.setOnClickListener {
                clickListener.onReportClick(report)
            }
        }
    }

    override fun getItemCount(): Int = reportList.size

    private fun timestampToDate(timestamp: Any?): Date {
        return when (timestamp) {
            is Long -> Date(timestamp)
            is Map<*, *> -> Date((timestamp["timestamp"] as? Long) ?: 0L)
            else -> Date()
        }
    }

    private fun formatTimestampToDate(timestamp: Any?): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.format(timestampToDate(timestamp))
        } catch (e: Exception) {
            "Unknown Date"
        }
    }

    private fun formatTimestampToTime(timestamp: Any?): String {
        return try {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            sdf.format(timestampToDate(timestamp))
        } catch (e: Exception) {
            "Unknown Time"
        }
    }

    class ReportViewHolder(val binding: ItemReportBinding) : RecyclerView.ViewHolder(binding.root)
}