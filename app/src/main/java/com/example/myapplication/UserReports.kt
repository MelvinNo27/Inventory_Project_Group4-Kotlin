package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.UserReportBinding
import com.google.firebase.database.*

class UserReports : AppCompatActivity() {
    private lateinit var binding: UserReportBinding
    private lateinit var reportList: MutableList<Report>
    private lateinit var adapter: UserReportAdapter
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView
        reportList = mutableListOf()
        adapter = UserReportAdapter(reportList)

        binding.recyclerViewReports.apply {
            layoutManager = LinearLayoutManager(this@UserReports)
            adapter = this@UserReports.adapter
        }

        binding.ItemUserReportBack.setOnClickListener {
            startActivity(Intent(this, Userdashboard::class.java))
        }

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().reference.child("reports")


        val itemTouchHelper = ItemTouchHelper(UserReportAdapter.SwipeToDeleteCallback(adapter))
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewReports)

        // Fetch reports
        fetchReportedUnits()
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
                Toast.makeText(
                    this@UserReports,
                    "Failed to fetch reports: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("UserReports", "DatabaseError: ${error.message}")
            }
        })
    }
}