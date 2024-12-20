package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ReportAdminItemsBinding
import com.example.myapplication.databinding.ReportDialogBinding
import com.google.firebase.database.*

class Reports : AppCompatActivity(), ReportAdapter.OnReportClickListener {

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
        adapter = ReportAdapter(reportList, this, this) // Pass the activity as click listener

        binding.recyclerViewReports.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewReports.adapter = adapter

        binding.ItemReportBack.setOnClickListener {
            startActivity(Intent(this, AdminDashboard::class.java))
            finish()
        }

        // Firebase Database Reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("reports")

        // Fetch and display reports
        fetchReportedUnits()
    }

    private fun fetchReportedUnits() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reportList.clear()
                for (data in snapshot.children) {
                    val report = data.getValue(Report::class.java)
                    if (report != null) {
                        reportList.add(report)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Reports, "Failed to fetch reports: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("Reports", "DatabaseError: ${error.message}")
            }
        })
    }

    override fun onReportClick(report: Report) {
        showReportDetailsDialog(report)
    }

    private fun showReportDetailsDialog(report: Report) {
        val dialogBinding = ReportDialogBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        // Set report details
        with(dialogBinding) {
            textViewRoomNumber.text = "${report.roomNumber}"
            textViewUnitName.text = "${report.unitName}"

            // Monitor
            if (report.monitorQuantity != null && report.monitorQuantity == 0) {
                textViewMonitorID.text = "${report.monitorID} (Quantity: ${report.monitorQuantity})"
                textViewMonitorID.visibility = android.view.View.VISIBLE
                textViewMonitor.visibility = android.view.View.VISIBLE
            } else {
                textViewMonitorID.visibility = android.view.View.GONE
                textViewMonitor.visibility = android.view.View.GONE
            }

            // Mouse
            if (report.mouseQuantity != null  && report.mouseQuantity == 0) {
                textViewMouseID.text = "${report.mouseID} (Quantity: ${report.mouseQuantity})"
                textViewMouseID.visibility = android.view.View.VISIBLE
                textViewMouse.visibility = android.view.View.VISIBLE
            } else {
                textViewMouseID.visibility = android.view.View.GONE
                textViewMouse.visibility = android.view.View.GONE
            }

            // Keyboard
            if (report.keyboardQuantity != null && report.keyboardQuantity == 0) {
                textViewKeyboardID.text = "${report.keyboardID} (Quantity: ${report.keyboardQuantity})"
                textViewKeyboardID.visibility = android.view.View.VISIBLE
                textViewKeyboard.visibility = android.view.View.VISIBLE
            } else {
                textViewKeyboardID.visibility = android.view.View.GONE
                textViewKeyboard.visibility = android.view.View.GONE
            }

            // MousePad
            if (report.mousePadQuantity != null && report.mousePadQuantity == 0) {
                textViewMousePadID.text = "${report.mousePadID} (Quantity: ${report.mousePadQuantity})"
                textViewMousePadID.visibility = android.view.View.VISIBLE
                textViewMousePad.visibility = android.view.View.VISIBLE
            } else {
                textViewMousePadID.visibility = android.view.View.GONE
                textViewMousePad.visibility = android.view.View.GONE
            }

            // Unit
            if (report.unitQuantity != null && report.unitQuantity == 0) {
                textViewUnitID.text = "${report.unitID} (Quantity: ${report.unitQuantity})"
                textViewUnitID.visibility = android.view.View.VISIBLE
                textViewUnit.visibility = android.view.View.VISIBLE
            } else {
                textViewUnitID.visibility = android.view.View.GONE
                textViewUnit.visibility = android.view.View.GONE
            }

            textViewReason.text = "Reason: ${report.reason}"

            btnClose.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}