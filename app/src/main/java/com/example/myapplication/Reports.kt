package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ReportAdminItemsBinding
import com.google.firebase.database.*

class Reports : AppCompatActivity() {

    private lateinit var binding: ReportAdminItemsBinding
    private lateinit var reportList: MutableList<Report>
    private lateinit var adapter: ReportAdapter
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ReportAdminItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView and Report List
        reportList = mutableListOf()
        adapter = ReportAdapter(reportList, this)

        binding.recyclerViewReports.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewReports.adapter = adapter

        // Firebase Database Reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("reports")

        // Fetch and display reports
        fetchReportedUnits()
    }

    private fun fetchReportedUnits() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    reportList.clear() // Clear old data
                    for (reportSnapshot in snapshot.children) {
                        val report = reportSnapshot.getValue(Report::class.java)
                        report?.let { reportList.add(it) }
                    }
                    adapter.notifyDataSetChanged() // Notify adapter of changes
                } else {
                    Toast.makeText(this@Reports, "No reports found.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Reports", "Error fetching data: ${error.message}")
                Toast.makeText(this@Reports, "Failed to fetch reports.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
