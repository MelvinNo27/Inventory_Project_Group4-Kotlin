package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ReportAdminItemsBinding
import com.google.firebase.database.*

class Reports : AppCompatActivity() {

    private lateinit var binding: ReportAdminItemsBinding

    // List to store the reports fetched from Firebase
    private lateinit var reportList: MutableList<Report>
    private lateinit var reportAdapter: ReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ReportAdminItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the list and adapter
        reportList = mutableListOf()
        reportAdapter = ReportAdapter(reportList, this)

        // Setup RecyclerView
        setupRecyclerView()

        // Load data from Firebase
        loadReportsFromFirebase()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewReports.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewReports.adapter = reportAdapter
    }

    private fun loadReportsFromFirebase() {
        val reportsRef = FirebaseDatabase.getInstance().reference.child("reportedUnits")

        // Using Firebase's ValueEventListener to listen for real-time changes
        reportsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reportList.clear() // Clear the list to avoid duplication

                // Iterate through the snapshots and populate the list
                for (child in snapshot.children) {
                    val report = child.getValue(Report::class.java)
                    report?.let { reportList.add(it) }
                }

                // Notify the adapter to refresh the RecyclerView
                reportAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Failed to load reports: ${error.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
