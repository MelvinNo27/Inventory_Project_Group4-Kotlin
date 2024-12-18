package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ReportAdminItemsBinding
import com.google.firebase.database.*

class RoomLayouts : AppCompatActivity() {
    private lateinit var reportList: MutableList<Report> // Mutable list of reports
    private lateinit var reportAdapter: ReportAdapter // Correct adapter name
    private lateinit var reportsRef: DatabaseReference
    private lateinit var binding: ReportAdminItemsBinding // Correct binding for main layout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding for activity
        binding = ReportAdminItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase reference
        reportsRef = FirebaseDatabase.getInstance().reference.child("reportedUnits")

        // Initialize the report list
        reportList = mutableListOf()

        // Setup RecyclerView Adapter
        reportAdapter = ReportAdapter(reportList) { report ->
            // Handle item click - Navigate to Reports activity
            val intent = Intent(this, Reports::class.java)
            intent.putExtra("unitID", report.unitID)
            intent.putExtra("monitorID", report.monitorID)
            intent.putExtra("mouseID", report.mouseID)
            intent.putExtra("keyboardID", report.keyboardID)
            intent.putExtra("mousePadID", report.mousePadID)
            intent.putExtra("unitQuantity", report.unitQuantity)
            intent.putExtra("reason", report.reason)
            startActivity(intent)
        }

        // Set up RecyclerView
        binding.recyclerViewReports.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewReports.adapter = reportAdapter

        // Load reports from Firebase
        loadReportsFromFirebase()
    }

    private fun loadReportsFromFirebase() {
        reportsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reportList.clear() // Clear existing data
                snapshot.children.forEach { reportSnapshot ->
                    val report = reportSnapshot.getValue(Report::class.java) // Deserialize into Report object
                    if (report != null) {
                        reportList.add(report) // Add report to the list
                    }
                }
                reportAdapter.notifyDataSetChanged() // Refresh RecyclerView
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
