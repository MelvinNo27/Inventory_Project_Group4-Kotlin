package com.example.myapplication

import android.content.Intent
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
            setResult(RESULT_OK)  // Set the result to OK when navigating back
            finish()  // Close the activity and return to SelectRooms
        }

        setupRecyclerView()
        setupAddUnitButton()
        loadUnitsFromFirebase()
    }


    private fun checkIfUserIsAdmin(userId: String) {
        val database = FirebaseDatabase.getInstance().reference

        // Check if the user is an admin by querying the "admins" node
        database.child("admins").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.addBtn.visibility = android.view.View.VISIBLE
                } else {

                    binding.addBtn.visibility = android.view.View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Error checking admin status: ${error.message}")
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

        // Hide fields that have null or zero values initially
        toggleUnitFieldsVisibility(dialogBinding)


        dialogBinding.btnAddUnit.setOnClickListener {
            if (validateInputs(dialogBinding)) {
                val unit = createUnitFromInputs(dialogBinding)
                saveUnitToFirebase(unit)
                dialog.dismiss()
            } else {
                showToast("Please fill in all fields")
            }
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun toggleUnitFieldsVisibility(dialogBinding: ActivityAddUnitBinding) {
        // Check if the unitID and unitQuantity are empty or 0
        val isUnitIDValid = dialogBinding.unitID.text.toString().isNotEmpty() && dialogBinding.unitID.text.toString().toInt() != 0
        val isUnitQuantityValid = dialogBinding.unitQuantity.text.toString().isNotEmpty() && dialogBinding.unitQuantity.text.toString().toInt() != 0

        // Show or hide fields based on validity
        dialogBinding.unit1Container.visibility = if (isUnitIDValid) View.VISIBLE else View.GONE
        dialogBinding.unit2Container.visibility = if (isUnitQuantityValid) View.VISIBLE else View.GONE
    }


    private fun validateInputs(dialogBinding: ActivityAddUnitBinding): Boolean {
        return dialogBinding.run {
            monitorID.text.isNotEmpty() && mouseID.text.isNotEmpty() &&
                    keyboardID.text.isNotEmpty() && mousePadID.text.isNotEmpty() &&
                    unitID.text.isNotEmpty() && MonitorQuantity.text.isNotEmpty() &&
                    MouseQuantity.text.isNotEmpty() && KeyboardQuantity.text.isNotEmpty() &&
                    mousePadQuantity.text.isNotEmpty() && unitQuantity.text.isNotEmpty()
        }
    }

    private fun createUnitFromInputs(dialogBinding: ActivityAddUnitBinding, existingUnit: UnitClass? = null): UnitClass {
        return dialogBinding.run {
            UnitClass(
                monitorID.text.toString().toInt(),
                mouseID.text.toString().toInt(),
                keyboardID.text.toString().toInt(),
                mousePadID.text.toString().toInt(),
                existingUnit?.unitID ?: unitID.text.toString().toInt(), // Use existing unitID if editing
                MonitorQuantity.text.toString().toInt(),
                MouseQuantity.text.toString().toInt(),
                KeyboardQuantity.text.toString().toInt(),
                mousePadQuantity.text.toString().toInt(),
                unitQuantity.text.toString().toInt(),
            )
        }
    }

    private fun saveUnitToFirebase(unit: UnitClass) {
        val roomId = intent.getStringExtra("ROOM_ID") ?: ""

        if (roomId.isNotEmpty()) {
            val unitsRefForRoom = database.reference.child("units").child(roomId)
            val newUnitRef = unitsRefForRoom.push()
            newUnitRef.setValue(unit).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Unit added successfully")
                    loadUnitsFromFirebase()
                    unitList.add(unit)
                    unitAdapter.notifyDataSetChanged()
                } else {
                    showToast("Failed to add unit")
                }
            }
        } else {
            showToast("Room ID is missing")
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
                    showToast("Error loading units: ${error.message}")
                }
            })
        } else {
            showToast("Room ID is missing")
        }
    }

    fun showUnitDetailsDialog(unit: UnitClass) {
        val dialogBinding = ActivityUnitDetailBinding.inflate(LayoutInflater.from(this))

        dialogBinding.ListMonitorID.text = unit.monitorID.toString()
        dialogBinding.ListMouseID.text = unit.mouseID.toString()
        dialogBinding.ListKeyboardID.text = unit.keyboardID.toString()
        dialogBinding.ListMousePadID.text = unit.mousePadID.toString()
        dialogBinding.ListUnitID.text = unit.unitID.toString()
        dialogBinding.MonitorQuantity.text = unit.monitorQuantity.toString()
        dialogBinding.MouseQuantity.text = unit.mouseQuantity.toString()
        dialogBinding.KeyboardQuantity.text = unit.keyboardQuantity.toString()
        dialogBinding.mousePadQuantity.text = unit.mousePadQuantity.toString()
        dialogBinding.unitQuantity.text = unit.unitQuantity.toString()

        dialogBinding.ListMonitorID.isEnabled = false
        dialogBinding.ListMouseID.isEnabled = false
        dialogBinding.ListKeyboardID.isEnabled = false
        dialogBinding.ListMousePadID.isEnabled = false
        dialogBinding.ListUnitID.isEnabled = false
        dialogBinding.MonitorQuantity.isEnabled = false
        dialogBinding.MouseQuantity.isEnabled = false
        dialogBinding.KeyboardQuantity.isEnabled = false
        dialogBinding.mousePadQuantity.isEnabled = false
        dialogBinding.unitQuantity.isEnabled = false

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnEdit.setOnClickListener {
            showEditUnitDialog(unit)
            dialog.dismiss()
        }

        dialogBinding.RoomBack.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }



    fun showEditUnitDialog(unit: UnitClass) {
        val dialogBinding = ActivityEditUnitBinding.inflate(LayoutInflater.from(this))

        // Pre-fill the fields with the existing unit data
        dialogBinding.editMonitorID.setText(unit.monitorID.toString())
        dialogBinding.editMouseID.setText(unit.mouseID.toString())
        dialogBinding.editKeyboardID.setText(unit.keyboardID.toString())
        dialogBinding.editMousePadID.setText(unit.mousePadID.toString())
        dialogBinding.editUnitID.setText(unit.unitID.toString())
        dialogBinding.editMonitorQuantity.setText(unit.monitorQuantity.toString())
        dialogBinding.editMouseQuantity.setText(unit.mouseQuantity.toString())
        dialogBinding.editKeyboardQuantity.setText(unit.keyboardQuantity.toString())
        dialogBinding.editMousePadQuantity.setText(unit.mousePadQuantity.toString())
        dialogBinding.editUnitQuantity.setText(unit.unitQuantity?.toString() ?: "")

        // Toggle visibility based on unit quantity being null or 0
        toggleSaveOrReportButton(dialogBinding, unit)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Check the user's role in the Firebase database
            checkIfUserIsAdmin(currentUser.uid) { isAdmin ->
                if (isAdmin) {
                    // Admin can save changes
                    dialogBinding.btnSave.setOnClickListener {
                        if (validateInputs(dialogBinding)) {
                            val updatedUnit = createUnitFromInputs(dialogBinding, unit)
                            updateUnitInFirebase(unit, updatedUnit)
                            dialog.dismiss()
                        } else {
                            showToast("Please fill in all fields")
                        }
                    }
                } else {
                    // Non-admin user will see the "Report" button
                    dialogBinding.btnSave.setOnClickListener {
                        // Check if any quantity is 0 or null
                        if (unit.monitorQuantity == null || unit.monitorQuantity == 0 ||
                            unit.mouseQuantity == null || unit.mouseQuantity == 0 ||
                            unit.keyboardQuantity == null || unit.keyboardQuantity == 0 ||
                            unit.mousePadQuantity == null || unit.mousePadQuantity == 0 ||
                            unit.unitQuantity == null || unit.unitQuantity == 0) {
                            // If quantity is null or 0, show the report dialog
                            showReportDialog(unit)
                            dialog.dismiss()  // Close the edit dialog
                        } else {
                            // Otherwise, allow save action
                            val updatedUnit = createUnitFromInputs(dialogBinding, unit)
                            updateUnitInFirebase(unit, updatedUnit)
                            dialog.dismiss()
                        }
                    }
                }
            }
        } else {
            // Handle the case where the user is not logged in
            showToast("User is not logged in")
            dialog.dismiss()
        }

        dialog.show()
    }

    // Helper function to toggle between Save and Report buttons based on quantity
    private fun toggleSaveOrReportButton(dialogBinding: ActivityEditUnitBinding, unit: UnitClass) {
        if (unit.monitorQuantity == null || unit.monitorQuantity == 0 ||
            unit.mouseQuantity == null || unit.mouseQuantity == 0 ||
            unit.keyboardQuantity == null || unit.keyboardQuantity == 0 ||
            unit.mousePadQuantity == null || unit.mousePadQuantity == 0 ||
            unit.unitQuantity == null || unit.unitQuantity == 0) {
            // If any quantity is null or 0, show the Report button
            dialogBinding.btnSave.text = "Report"
        } else {
            // If all quantities are valid, show the Save button
            dialogBinding.btnSave.text = "Save"
        }
    }




    private fun showReportDialog(unit: UnitClass) {
        val reportDialogBinding = DialogReportBinding.inflate(LayoutInflater.from(this))
        val reportDialog = AlertDialog.Builder(this)
            .setView(reportDialogBinding.root)
            .setTitle("Report Unit")
            .create()

        // Get the room number from the Intent and the unit name from the UnitClass object
        val roomNumber = intent.getStringExtra("ROOM_NAME") ?: "Unknown Room"

        // Display room and unit name in the report dialog
        reportDialogBinding.textViewRoomNumber.text = roomNumber

        // Optionally, you can display other unit details or handle visibility of fields
        setFieldVisibility(reportDialogBinding, unit)

        // Handle submit report button
        reportDialogBinding.btnSubmitReport.setOnClickListener {
            val reason = reportDialogBinding.editTextReportReason.text.toString()
            if (reason.isNotEmpty()) {
                // Save the report to Firebase or handle it as necessary
                saveReportToFirebase(unit, reason)
                showToast("Report submitted successfully")
                reportDialog.dismiss()
            } else {
                showToast("Please enter a reason for the report")
            }
        }

        // Handle cancel report button
        reportDialogBinding.btnCancelReport.setOnClickListener {
            reportDialog.dismiss()
        }

        reportDialog.show()
    }


    // Helper function to set visibility of fields based on their values
    private fun setFieldVisibility(reportDialogBinding: DialogReportBinding, unit: UnitClass) {
        // Monitor ID
        if (unit.monitorQuantity == null || unit.monitorQuantity == 0) {
            reportDialogBinding.textViewMonitorID.text = "${unit.monitorID}"
            reportDialogBinding.textViewMonitorID.visibility = View.VISIBLE
        } else {
            reportDialogBinding.textViewMonitorID.visibility = View.GONE
        }

        // Mouse ID
        if (unit.mouseQuantity == null || unit.mouseQuantity == 0) {
            reportDialogBinding.textViewMouseID.text = "${unit.mouseID}"
            reportDialogBinding.textViewMouseID.visibility = View.VISIBLE
        } else {

            reportDialogBinding.textViewMouseID.visibility = View.GONE
        }

        // Keyboard ID
        if (unit.keyboardQuantity == null || unit.keyboardQuantity == 0) {
            reportDialogBinding.textViewKeyboardID.text = "${unit.keyboardID}"
            reportDialogBinding.textViewKeyboardID.visibility = View.VISIBLE
        } else {

            reportDialogBinding.textViewKeyboardID.visibility = View.GONE
        }

        // MousePad ID
        if (unit.mousePadQuantity == null || unit.mousePadQuantity == 0) {

            reportDialogBinding.textViewMousePadID.text = "${unit.mousePadID}"
            reportDialogBinding.textViewMousePadID.visibility = View.VISIBLE
        } else {
            reportDialogBinding.textViewMousePadID.visibility = View.GONE
        }

        // Unit Quantity
        if (unit.unitQuantity == null || unit.unitQuantity == 0) {
            reportDialogBinding.textViewUnitID.text = "${unit.unitID}"
            reportDialogBinding.textViewUnitID.visibility = View.VISIBLE
        } else {
            reportDialogBinding.textViewUnitID.visibility = View.GONE
        }
    }



    // Function to save the report to Firebase
    private fun saveReportToFirebase(unit: UnitClass, reason: String) {
        val reportsRef = FirebaseDatabase.getInstance().reference.child("reportedUnits")

        val reportId = reportsRef.push().key ?: return
        val report = Report(
            unit.unitID,
            unit.monitorID,
            unit.mouseID,
            unit.keyboardID,
            unit.mousePadID,
            unit.unitQuantity,
            reason
        )

        reportsRef.child(reportId).setValue(report).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showToast("Report submitted successfully")
            } else {
                showToast("Failed to submit report")
            }
        }
    }





    // Function to check if the user is an admin and call the callback with the result
    private fun checkIfUserIsAdmin(userId: String, callback: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("admins").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Error checking admin status: ${error.message}")
                callback(false)
            }
        })
    }


    private fun createUnitFromInputs(dialogBinding: ActivityEditUnitBinding, existingUnit: UnitClass): UnitClass {
        return dialogBinding.run {
            UnitClass(
                monitorID = editMonitorID.text.toString().toIntOrNull() ?: 0,  // Default to 0 if input is invalid
                mouseID = editMouseID.text.toString().toIntOrNull() ?: 0,
                keyboardID = editKeyboardID.text.toString().toIntOrNull() ?: 0,
                mousePadID = editMousePadID.text.toString().toIntOrNull() ?: 0,
                unitID = existingUnit.unitID,
                monitorQuantity = editMonitorQuantity.text.toString().toIntOrNull() ?: 0,
                mouseQuantity = editMouseQuantity.text.toString().toIntOrNull() ?: 0,
                keyboardQuantity = editKeyboardQuantity.text.toString().toIntOrNull() ?: 0,
                mousePadQuantity = editMousePadQuantity.text.toString().toIntOrNull() ?: 0,
                unitQuantity = editUnitQuantity.text.toString().toIntOrNull() ?: 0
            )
        }
    }




    private fun validateInputs(dialogBinding: ActivityEditUnitBinding): Boolean {
        return dialogBinding.run {
            // Check if all fields are filled
            editMonitorID.text.isNotEmpty() &&
                    editMouseID.text.isNotEmpty() &&
                    editKeyboardID.text.isNotEmpty() &&
                    editMousePadID.text.isNotEmpty() &&
                    editUnitID.text.isNotEmpty() &&
                    editMonitorQuantity.text.isNotEmpty() &&
                    editMouseQuantity.text.isNotEmpty() &&
                    editKeyboardQuantity.text.isNotEmpty() &&
                    editMousePadQuantity.text.isNotEmpty() &&
                    editUnitQuantity.text.isNotEmpty()
        }
    }


    private fun updateUnitInFirebase(oldUnit: UnitClass, updatedUnit: UnitClass) {
        val roomId = intent.getStringExtra("ROOM_ID") ?: ""

        if (roomId.isNotEmpty()) {
            val unitsRefForRoom = database.reference.child("units").child(roomId)

            // Locate the unit in Firebase using the unitID
            unitsRefForRoom.orderByChild("unitID").equalTo(oldUnit.unitID.toDouble()).limitToFirst(1)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val unitKey = snapshot.children.firstOrNull()?.key
                        if (unitKey != null) {
                            unitsRefForRoom.child(unitKey).setValue(updatedUnit)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        showToast("Unit updated successfully")
                                        loadUnitsFromFirebase()  // Reload the list after update
                                    } else {
                                        showToast("Failed to update unit")
                                    }
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        showToast("Error updating unit: ${error.message}")
                    }
                })
        } else {
            showToast("Room ID is missing")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

