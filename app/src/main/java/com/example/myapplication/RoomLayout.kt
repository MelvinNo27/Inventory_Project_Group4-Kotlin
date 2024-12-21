package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityAddUnitBinding
import com.example.myapplication.databinding.ActivityEditUnitBinding
import com.example.myapplication.databinding.ActivityRoomLayoutBinding
import com.example.myapplication.databinding.ActivityUnitDetailBinding
import com.example.myapplication.databinding.DialogReportBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RoomLayout : AppCompatActivity() {

    private lateinit var binding: ActivityRoomLayoutBinding
    private lateinit var unitAdapter: UnitAdapter
    private val unitList = mutableListOf<UnitClass>()
    private lateinit var database: FirebaseDatabase
    private lateinit var unitsRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the room ID and name from the Intent
        val roomId = intent.getStringExtra("ROOM_ID") ?: ""
        val roomName = intent.getStringExtra("ROOM_NAME") ?: "Unknown Room"

        // Display the room name
        binding.textViewRoom.text = roomName

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            checkIfUserIsAdmin(currentUser.uid)
        } else {
            binding.addBtn.visibility = android.view.View.VISIBLE
        }

        database = FirebaseDatabase.getInstance()
        unitsRef = database.reference.child("units").child(roomId)

        binding.RoomBack.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        setupRecyclerView()
        setupAddUnitButton()
        loadUnitsFromFirebase()
    }


    private fun checkIfUserIsAdmin(userId: String) {
        val database = FirebaseDatabase.getInstance().reference

        database.child("admins").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.addBtn.visibility =View.VISIBLE
                } else {
                    binding.addBtn.visibility =View.GONE
                }
            }
            override fun onCancelled(error: DatabaseError) {
                showDialogMessage("Error checking admin status: ${error.message}")
            }
        })
    }

    private fun setupRecyclerView() {
        unitAdapter = UnitAdapter(unitList, this) // Pass 'this' (RoomLayout) as context
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = unitAdapter
    }


    private fun setupAddUnitButton() {
        binding.addBtn.setOnClickListener {
            showAddUnitDialog()
        }
    }

    private fun showAddUnitDialog() {
        val dialogBinding = ActivityAddUnitBinding.inflate(LayoutInflater.from(this))

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnAddUnit.setOnClickListener {
            if (validateInputs(dialogBinding)) {
                val unit = createUnitFromInputs(dialogBinding)
                saveUnitToFirebase(unit)
                dialog.dismiss()
            }else if (!validateInputs(dialogBinding)) {
                showDialogMessage("There doesn't seem to be such a large quantity")
        }else {
                showDialogMessage("Please fill in all fields")
            }

        }
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun validateInputs(dialogBinding: ActivityAddUnitBinding): Boolean {
        return dialogBinding.run {
            monitorID.text.isNotEmpty() &&
                    mouseID.text.isNotEmpty() &&
                    keyboardID.text.isNotEmpty() &&
                    mousePadID.text.isNotEmpty() &&
                    unitID.text.isNotEmpty() &&
                    AVRID.text.isNotEmpty() &&
                    MonitorQuantity.text.toString().toIntOrNull()?.let { it in 1..2 } == true &&
                    MouseQuantity.text.toString().toIntOrNull()?.let { it in 1..2 } == true &&
                    KeyboardQuantity.text.toString().toIntOrNull()?.let { it in 1..2 } == true &&
                    mousePadQuantity.text.toString().toIntOrNull()?.let { it in 1..2 } == true &&
                    AVRQuantity.text.toString().toIntOrNull()?.let { it in 1..2 } == true &&
                    unitQuantity.text.toString().toIntOrNull()?.let { it in 1..2 } == true
        }
    }

    private fun createUnitFromInputs(dialogBinding: ActivityAddUnitBinding, existingUnit: UnitClass? = null): UnitClass {
        return dialogBinding.run {
            UnitClass(
                monitorID = monitorID.text.toString(),
                mouseID = mouseID.text.toString(),
                keyboardID = keyboardID.text.toString(),
                mousePadID = mousePadID.text.toString(),
                AVRID = mousePadID.text.toString(),
                unitID = existingUnit?.unitID ?: unitID.text.toString(),
                monitorQuantity = MonitorQuantity.text.toString().toIntOrNull() ?: 0,
                mouseQuantity = MouseQuantity.text.toString().toIntOrNull() ?: 0,
                keyboardQuantity = KeyboardQuantity.text.toString().toIntOrNull() ?: 0,
                mousePadQuantity = mousePadQuantity.text.toString().toIntOrNull() ?: 0,
                AVRQuantity = mousePadQuantity.text.toString().toIntOrNull() ?: 0,
                unitQuantity = unitQuantity.text.toString().toIntOrNull() ?: 0
            )
        }
    }
    private fun saveUnitToFirebase(unit: UnitClass) {
        val roomId = intent.getStringExtra("ROOM_ID") ?: ""

        if (roomId.isNotEmpty()) {
            val unitsRefForRoom = database.reference.child("units").child(roomId)
            val newUnitRef = unitsRefForRoom.push()

            val currentTimestamp = System.currentTimeMillis()
            val unitWithTimestamp = unit.copy(timestamp = currentTimestamp)

            newUnitRef.setValue(unitWithTimestamp).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showDialogMessage("Unit added successfully")
                    loadUnitsFromFirebase()
                    unitList.add(unit)
                    unitAdapter.notifyDataSetChanged()
                } else {
                    showDialogMessage("Failed to add unit")
                }
            }
        } else {
            showDialogMessage("Room ID is missing")
        }
    }


    private fun loadUnitsFromFirebase() {
        val roomId = intent.getStringExtra("ROOM_ID") ?: ""
        if (roomId.isNotEmpty()) {
            val unitsRefForRoom = database.reference.child("units").child(roomId)

            unitsRefForRoom.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    unitList.clear()
                    snapshot.children.forEach { unitSnapshot ->
                        val unit = unitSnapshot.getValue(UnitClass::class.java)
                        unit?.let { unitList.add(it) }
                    }
                    unitAdapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {
                    showDialogMessage("Error loading units: ${error.message}")
                }
            })
        } else {
            showDialogMessage("Room ID is missing")
        }
    }

    fun showUnitDetailsDialog(unit: UnitClass, position: Int) {
        val dialogBinding = ActivityUnitDetailBinding.inflate(LayoutInflater.from(this))

        dialogBinding.ListMonitorID.text = unit.monitorID
        dialogBinding.ListMouseID.text = unit.mouseID
        dialogBinding.ListKeyboardID.text = unit.keyboardID
        dialogBinding.ListMousePadID.text = unit.mousePadID
        dialogBinding.ListAVRID.text = unit.AVRID
        dialogBinding.ListUnitID.text = unit.unitID
        dialogBinding.MonitorQuantity.text = unit.monitorQuantity.toString()
        dialogBinding.MouseQuantity.text = unit.mouseQuantity.toString()
        dialogBinding.KeyboardQuantity.text = unit.keyboardQuantity.toString()
        dialogBinding.mousePadQuantity.text = unit.mousePadQuantity.toString()
        dialogBinding.ListAVRQuantity.text = unit.AVRQuantity.toString()
        dialogBinding.unitQuantity.text = unit.unitQuantity.toString()

        dialogBinding.ListMonitorID.isEnabled = false
        dialogBinding.ListMouseID.isEnabled = false
        dialogBinding.ListKeyboardID.isEnabled = false
        dialogBinding.ListMousePadID.isEnabled = false
        dialogBinding.ListAVRID.isEnabled = false
        dialogBinding.ListUnitID.isEnabled = false
        dialogBinding.MonitorQuantity.isEnabled = false
        dialogBinding.MouseQuantity.isEnabled = false
        dialogBinding.KeyboardQuantity.isEnabled = false
        dialogBinding.mousePadQuantity.isEnabled = false
        dialogBinding.ListAVRQuantity.isEnabled = false
        dialogBinding.unitQuantity.isEnabled = false

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnEdit.setOnClickListener {
            showEditUnitDialog(unit, position )
            dialog.dismiss()
        }

        dialogBinding.RoomBack.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showEditUnitDialog(unit: UnitClass, position: Int) {
        val dialogBinding = ActivityEditUnitBinding.inflate(LayoutInflater.from(this))

        // Pre-fill the fields with the existing unit data
        dialogBinding.editMonitorID.setText(unit.monitorID)
        dialogBinding.editMouseID.setText(unit.mouseID)
        dialogBinding.editKeyboardID.setText(unit.keyboardID)
        dialogBinding.editMousePadID.setText(unit.mousePadID)
        dialogBinding.editUnitID.setText(unit.unitID)
        dialogBinding.editAVRID.setText(unit.mousePadID)
        dialogBinding.editMonitorQuantity.setText(unit.monitorQuantity.toString())
        dialogBinding.editMouseQuantity.setText(unit.mouseQuantity.toString())
        dialogBinding.editKeyboardQuantity.setText(unit.keyboardQuantity.toString())
        dialogBinding.editMousePadQuantity.setText(unit.mousePadQuantity.toString())
        dialogBinding.editAVRQuantity.setText(unit.AVRQuantity.toString())
        dialogBinding.editUnitQuantity.setText(unit.unitQuantity?.toString())

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Check if user is admin
            val database = FirebaseDatabase.getInstance().reference
            database.child("admins").child(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val isAdmin = snapshot.exists()

                        // Handle quantity fields based on role and current values
                        with(dialogBinding) {
                            if (!isAdmin) {
                                // For regular users, disable fields if quantity is 0
                                if (unit.monitorQuantity == 0) {
                                    editMonitorQuantity.isEnabled = false
                                    editMonitorQuantity.setTextColor(resources.getColor(android.R.color.darker_gray))
                                }
                                if (unit.mouseQuantity == 0) {
                                    editMouseQuantity.isEnabled = false
                                    editMouseQuantity.setTextColor(resources.getColor(android.R.color.darker_gray))
                                }
                                if (unit.keyboardQuantity == 0) {
                                    editKeyboardQuantity.isEnabled = false
                                    editKeyboardQuantity.setTextColor(resources.getColor(android.R.color.darker_gray))
                                }
                                if (unit.mousePadQuantity == 0) {
                                    editMousePadQuantity.isEnabled = false
                                    editMousePadQuantity.setTextColor(resources.getColor(android.R.color.darker_gray))
                                }
                                if (unit.unitQuantity == 0) {
                                    editUnitQuantity.isEnabled = false
                                    editUnitQuantity.setTextColor(resources.getColor(android.R.color.darker_gray))
                                }
                                if (unit.AVRQuantity == 0) {
                                    editAVRQuantity.isEnabled = false
                                    editAVRQuantity.setTextColor(resources.getColor(android.R.color.darker_gray))
                                }
                            }
                        }

                        dialogBinding.btnSave.setOnClickListener {
                            // Create updated unit with current values
                            val updatedUnit = UnitClass(
                                monitorID = dialogBinding.editMonitorID.text.toString(),
                                mouseID = dialogBinding.editMouseID.text.toString(),
                                keyboardID = dialogBinding.editKeyboardID.text.toString(),
                                mousePadID = dialogBinding.editMousePadID.text.toString(),
                                AVRID = dialogBinding.editAVRID.text.toString(),
                                unitID = unit.unitID,
                                monitorQuantity = dialogBinding.editMonitorQuantity.text.toString().toIntOrNull() ?: 0,
                                mouseQuantity = dialogBinding.editMouseQuantity.text.toString().toIntOrNull() ?: 0,
                                keyboardQuantity = dialogBinding.editKeyboardQuantity.text.toString().toIntOrNull() ?: 0,
                                mousePadQuantity = dialogBinding.editMousePadQuantity.text.toString().toIntOrNull() ?: 0,
                                AVRQuantity = dialogBinding.editAVRQuantity.text.toString().toIntOrNull() ?: 0,
                                unitQuantity = dialogBinding.editUnitQuantity.text.toString().toIntOrNull() ?: 0
                            )

                            updateUnitInFirebase(unit, updatedUnit)
                            dialog.dismiss()

                            // Check if any quantity is 0 and show report dialog if needed
                            if (updatedUnit.monitorQuantity == 0 || updatedUnit.mouseQuantity == 0 ||
                                updatedUnit.keyboardQuantity == 0 || updatedUnit.mousePadQuantity == 0 ||
                                updatedUnit.AVRQuantity == 0 ||
                                updatedUnit.unitQuantity == 0) {
                                val unitLabel = "Unit ${position + 1}"
                                showReportDialog(updatedUnit, unitLabel)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        showDialogMessage("Error checking admin status: ${error.message}")
                        dialog.dismiss()
                    }
                })
        } else {
            showDialogMessage("User is not logged in")
            dialog.dismiss()
        }

        dialog.show()
    }

    // In your RoomLayout activity/fragment:
    private fun showReportDialog(unit: UnitClass, unitLabel: String) {  // Add unitLabel parameter
        val reportDialogBinding = DialogReportBinding.inflate(LayoutInflater.from(this))
        val reportDialog = AlertDialog.Builder(this)
            .setView(reportDialogBinding.root)
            .setTitle("Report Unit")
            .create()

        // Get the room number from the Intent
        val roomNumber = intent.getStringExtra("ROOM_NAME") ?: "Unknown Room"

        // Display room and unit name in the report dialog
        reportDialogBinding.textViewRoomNumber.text = roomNumber
        reportDialogBinding.textViewUnitName.text = unitLabel  // Use the passed unitLabel

        // Rest of your dialog code remains the same
        setFieldVisibility(reportDialogBinding, unit)

        reportDialogBinding.btnSubmitReport.setOnClickListener {
            val reason = reportDialogBinding.editTextReportReason.text.toString()
            if (reason.isNotEmpty()) {
                saveReportToFirebase(unit, reason)
                reportDialog.dismiss()
            } else {
                showDialogMessage("Please enter a reason for the report")
            }
        }

        reportDialogBinding.btnCancelReport.setOnClickListener {
            reportDialog.dismiss()
        }
        reportDialog.show()
    }
    private fun setFieldVisibility(reportDialogBinding: DialogReportBinding, unit: UnitClass) {
        if (unit.monitorQuantity == null || unit.monitorQuantity == 0) {
            reportDialogBinding.textViewMonitorID.text = "${unit.monitorID}"
            reportDialogBinding.textViewMonitorID.visibility = View.VISIBLE
        } else {
            reportDialogBinding.textViewMonitorID.visibility = View.GONE
            reportDialogBinding.textViewMonitor.visibility = View.GONE
        }
        if (unit.mouseQuantity == null || unit.mouseQuantity == 0) {
            reportDialogBinding.textViewMouseID.text = "${unit.mouseID}"
            reportDialogBinding.textViewMouseID.visibility = View.VISIBLE
        } else {
            reportDialogBinding.textViewMouseID.visibility = View.GONE
            reportDialogBinding.textViewMouse.visibility = View.GONE
        }
        if (unit.keyboardQuantity == null || unit.keyboardQuantity == 0) {
            reportDialogBinding.textViewKeyboardID.text = "${unit.keyboardID}"
            reportDialogBinding.textViewKeyboardID.visibility = View.VISIBLE
        } else {
            reportDialogBinding.textViewKeyboardID.visibility = View.GONE
            reportDialogBinding.textViewKeyboard.visibility = View.GONE
        }
        if (unit.mousePadQuantity == null || unit.mousePadQuantity == 0) {
            reportDialogBinding.textViewMousePadID.text = "${unit.mousePadID}"
            reportDialogBinding.textViewMousePadID.visibility = View.VISIBLE
        } else {
            reportDialogBinding.textViewMousePadID.visibility = View.GONE
            reportDialogBinding.textViewMousePad.visibility = View.GONE
        }
        if (unit.unitQuantity == null || unit.unitQuantity == 0) {
            reportDialogBinding.textViewUnitID.text = "${unit.unitID}"
            reportDialogBinding.textViewUnitID.visibility = View.VISIBLE
        } else {
            reportDialogBinding.textViewUnitID.visibility = View.GONE
            reportDialogBinding.textViewUnit.visibility = View.GONE
        }
        if (unit.AVRQuantity == null || unit.AVRQuantity == 0) {
            reportDialogBinding.textViewAVRID.text = "${unit.AVRID}"
            reportDialogBinding.textViewAVRID.visibility = View.VISIBLE
        } else {
            reportDialogBinding.textViewAVRID.visibility = View.GONE
            reportDialogBinding.textViewAVR.visibility = View.GONE
        }
    }

    private fun saveReportToFirebase(unit: UnitClass, reason: String) {
        val reportsRef = FirebaseDatabase.getInstance().reference.child("reports")
        val reportId = reportsRef.push().key ?: return
        val roomNumber = intent.getStringExtra("ROOM_NAME") ?: "Unknown Room"

        // Get the position of the unit to create the proper unit name
        val unitPosition = unitList.indexOfFirst { it.unitID == unit.unitID }
        val unitName = "Unit ${unitPosition + 1}"  // This creates the format "Unit X"

        val report = Report(
            unitID = unit.unitID,
            monitorID = unit.monitorID,
            mouseID = unit.mouseID,
            keyboardID = unit.keyboardID,
            mousePadID = unit.mousePadID,
            AVRQuantity = unit.AVRQuantity,
            unitQuantity = unit.unitQuantity,
            monitorQuantity = unit.monitorQuantity,
            mouseQuantity = unit.mouseQuantity,
            keyboardQuantity = unit.keyboardQuantity,
            mousePadQuantity = unit.mousePadQuantity,
            AVRID = unit.AVRID,
            roomNumber = roomNumber,
            reason = reason,
            timestamp = ServerValue.TIMESTAMP,
            unitName = unitName
        )
        reportsRef.child(reportId).setValue(report).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Report submitted successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to submit report", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun updateUnitInFirebase(oldUnit: UnitClass, updatedUnit: UnitClass) {
        val roomId = intent.getStringExtra("ROOM_ID") ?: ""

        if (roomId.isNotEmpty()) {
            val unitsRefForRoom = database.reference.child("units").child(roomId)

            unitsRefForRoom.orderByChild("unitID").equalTo(oldUnit.unitID)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            val unitKey = snapshot.children.firstOrNull()?.key
                            if (unitKey != null) {
                                // Preserve the timestamp of the original unit if it exists
                                val updatedUnitWithTimestamp = updatedUnit.copy(
                                    timestamp = snapshot.children.firstOrNull()
                                        ?.getValue(UnitClass::class.java)
                                        ?.timestamp ?: System.currentTimeMillis()
                                )

                                unitsRefForRoom.child(unitKey).setValue(updatedUnitWithTimestamp)
                                    .addOnSuccessListener {
                                        // Only show success message if no quantities are zero
                                        val hasZeroQuantity = updatedUnit.run {
                                            monitorQuantity == 0 || mouseQuantity == 0 ||
                                                    keyboardQuantity == 0 || mousePadQuantity == 0
                                                    || AVRQuantity == 0 ||
                                                    unitQuantity == 0
                                        }
                                        if (!hasZeroQuantity) {
                                            showDialogMessage("Unit updated successfully")
                                        }
                                        loadUnitsFromFirebase()
                                    }
                                    .addOnFailureListener { e ->
                                        showDialogMessage("Failed to update unit: ${e.message}")
                                    }
                            } else {
                                showDialogMessage("Unit not found")
                            }
                        } catch (e: Exception) {
                            showDialogMessage("Error updating unit: ${e.message}")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        showDialogMessage("Error updating unit: ${error.message}")
                    }
                })
        } else {
            showDialogMessage("Room ID is missing")
        }
    }
    private fun showDialogMessage(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

}

