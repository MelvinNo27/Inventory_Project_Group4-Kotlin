package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ReportAdminItemsBinding
import com.google.firebase.database.*

class Reports : AppCompatActivity() {

    private lateinit var binding: ReportAdminItemsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ReportAdminItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val reportList = mutableListOf<Report>() // Create a list to hold report data
        val adapter = ReportAdapter(reportList, this) // Adapter for RecyclerView

        binding.recyclerViewReports.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewReports.adapter = adapter

        // Load data from Firebase
        loadReportsFromFirebase(reportList, adapter)
    }

    private fun loadReportsFromFirebase(reportList: MutableList<Report>, adapter: ReportAdapter) {
        val reportsRef = FirebaseDatabase.getInstance().reference.child("reportedUnits")

        reportsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reportList.clear() // Clear the list to avoid duplication
                for (child in snapshot.children) {
                    val report = child.getValue(Report::class.java)
                    report?.let { reportList.add(it) }
                }
                adapter.notifyDataSetChanged()
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
