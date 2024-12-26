package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        adapter = ReportAdapter(reportList, this, this)

        binding.recyclerViewReports.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewReports.adapter = adapter

        setupSwipeToDelete()

        binding.ItemReportBack.setOnClickListener {
            startActivity(Intent(this, AdminDashboard::class.java))
            finish()
        }

        // Firebase Database Reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("reports")

        // Fetch and display reports
        fetchReportedUnits()
    }
    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0, // No drag directions
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT // Swipe directions
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val report = reportList[position]

                // Yes/No confirmation dialog
                AlertDialog.Builder(this@Reports)
                    .setTitle("Confirm Deletion")
                    .setMessage("Do you want to delete this report?")
                    .setPositiveButton("Yes") { _, _ ->
                        // Delete from Firebase
                        report.reportId?.let { reportId ->
                            databaseReference.child(reportId).removeValue()
                                .addOnSuccessListener {
                                    // Remove from local list
                                    reportList.removeAt(position)
                                    adapter.notifyItemRemoved(position)
                                    Toast.makeText(this@Reports, "Report deleted successfully", Toast.LENGTH_SHORT).show()

                                    // Reload reports
                                    fetchReportedUnits()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this@Reports, "Failed to delete report: ${e.message}", Toast.LENGTH_SHORT).show()
                                    adapter.notifyItemChanged(position)
                                }
                        } ?: run {
                            Toast.makeText(this@Reports, "Report ID not found", Toast.LENGTH_SHORT).show()
                            adapter.notifyItemChanged(position)
                        }
                    }
                    .setNegativeButton("No") { _, _ ->
                        // Reset the item position if deletion is canceled
                        adapter.notifyItemChanged(position)
                    }
                    .show()
            }
        }

        // Attach the swipe handler to the RecyclerView
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.recyclerViewReports)
    }

    private fun fetchReportedUnits() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reportList.clear()
                for (data in snapshot.children) {
                    val report = data.getValue(Report::class.java)
                    report?.let {
                        it.reportId = data.key
                        reportList.add(it)
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
            if (report.mouseQuantity != null && report.mouseQuantity == 0) {
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
            if (report.avrQuantity != null && report.avrQuantity == 0) {
                textViewAVRID.text = "${report.unitID} (Quantity: ${report.unitQuantity})"
                textViewAVRID.visibility = android.view.View.VISIBLE
                textViewUnit.visibility = android.view.View.VISIBLE
            } else {
                textViewUnitID.visibility = android.view.View.GONE
                textViewAVR.visibility = android.view.View.GONE
            }

            textViewReason.text = "Reason: ${report.reason}"

            // Set the current status if it exists
            when (report.status) {
                "Issue Noted" -> dialogIssueNoted.isChecked = true
                "Repair in Process" -> dialogRepairInProcess.isChecked = true
                "Complete" -> dialogComplete.isChecked = true
            }

            btnUpdate.setOnClickListener {
                // Get the selected status
                val selectedStatus = when {
                    dialogIssueNoted.isChecked -> "Issue Noted"
                    dialogRepairInProcess.isChecked -> "Repair in Process"
                    dialogComplete.isChecked -> "Complete"
                    else -> null
                }

                if (selectedStatus != null) {
                    // Update the status in Firebase
                    report.reportId?.let { reportId ->
                        databaseReference.child(reportId).child("status").setValue(selectedStatus)
                            .addOnSuccessListener {
                                Toast.makeText(this@Reports, "Status updated successfully", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this@Reports, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } ?: run {
                        Toast.makeText(this@Reports, "Report ID not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@Reports, "Please select a status", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }
}