package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ReportAdminBinding

class ReportAdapter(
    private val reports: List<Report>,
    private val onClick: (Report) -> Unit
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    class ReportViewHolder(val binding: ReportAdminBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ReportAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.binding.textViewUnitID.text = "Unit ID: ${report.unitID}"
        holder.binding.editTextReportReason.text = "Reason: ${report.reason}"
        holder.binding.textViewRoomNumber.text = "Room: ${report.roomName}"

        holder.itemView.setOnClickListener {
            onClick(report)
        }
    }

    override fun getItemCount(): Int = reports.size
}

