package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ReportAdminItemsBinding

class Reports : AppCompatActivity() {
    private lateinit var binding: ReportAdminItemsBinding
    private lateinit var reportList: MutableList<Report> // List of reports
    private lateinit var reportAdapter: ReportAdapter // Adapter for the RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ReportAdminItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the list and adapter
        reportList = mutableListOf()
        reportAdapter = ReportAdapter(reportList) { report ->
            // Handle item click if needed
            // Example: Show a Toast with the clicked report's reason
            showToast("Clicked on: ${report.reason}")
        }

        // Set up RecyclerView
        binding.recyclerViewReports.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewReports.adapter = reportAdapter

        // Load reports (dummy or fetched data)
        loadReports()
    }

    private fun loadReports() {
        // Simulate loading reports (replace this with Firebase or database fetch logic)
        reportList.add(Report(1, 101, 201, 301, 401, 10, "Broken monitor", "Room 101"))
        reportList.add(Report(2, 102, 202, 302, 402, 5, "Missing keyboard", "Room 202"))

        // Notify the adapter of the data change
        reportAdapter.notifyDataSetChanged()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
