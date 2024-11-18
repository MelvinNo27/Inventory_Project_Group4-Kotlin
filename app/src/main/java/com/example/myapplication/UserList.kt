package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityUserListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UserList : AppCompatActivity() {

    private lateinit var binding: ActivityUserListBinding
    private val itemList = mutableListOf<AdminUser>()
    private val ADD_ITEM_REQUEST_CODE = 1
    private lateinit var rootDatabaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rootDatabaseRef = FirebaseDatabase.getInstance().getReference("Users")
        auth = FirebaseAuth.getInstance()

        binding.addButton.setOnClickListener {
            showAddItemForm()
        }

        binding.backBtn.setOnClickListener {
            startActivity(Intent(this, AdminDashboard::class.java))
            finish()
        }
        binding.viewUsers.setOnClickListener {
            startActivity(Intent(this, AdminPendingUsers::class.java))
            finish()
        }

        loadItemsFromFirebase()
    }


    private fun addItemToTable(item: AdminUser) {
        val tableRow = TableRow(this).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
        }

        val idNumberTextView = TextView(this).apply {
            text = (binding.tableLayout.childCount).toString()
            gravity = Gravity.CENTER
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        }

        val nameTextView = TextView(this).apply {
            text = truncateString(item.name, 7)
            gravity = Gravity.CENTER
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.5f)
        }

        val emailTextView = TextView(this).apply {
            text = truncateString(item.email, 7)
            gravity = Gravity.CENTER
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.5f)
        }

        val editTextView = TextView(this).apply {
            text = "Edit"
            gravity = Gravity.CENTER
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener {
                editItem(item)
            }
        }

        val deleteTextView = TextView(this).apply {
            text = "Delete"
            gravity = Gravity.CENTER
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener {
                deleteItem(item)
                binding.tableLayout.removeView(tableRow)
                updateRowIds()
            }
        }

        tableRow.addView(idNumberTextView)
        tableRow.addView(nameTextView)
        tableRow.addView(emailTextView)
        tableRow.addView(editTextView)
        tableRow.addView(deleteTextView)
        binding.tableLayout.addView(tableRow)
    }

    private fun updateRowIds() {
        for (i in 1 until binding.tableLayout.childCount) {
            val row = binding.tableLayout.getChildAt(i) as TableRow
            val idTextView = row.getChildAt(0) as TextView
            idTextView.text = i.toString()
        }
    }

    private fun showAddItemForm() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_add_item, null)
        val nameEditText: EditText = dialogView.findViewById(R.id.nameEditText)
        val emailEditText: EditText = dialogView.findViewById(R.id.emailEditText)
        val passwordEditText: EditText = dialogView.findViewById(R.id.passwordEditText)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Item")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val nameText = nameEditText.text.toString()
                val emailText = emailEditText.text.toString()
                val passwordText = passwordEditText.text.toString()

                if (validateInput(nameText, emailText, passwordText)) {
                    val newItemId = rootDatabaseRef.push().key ?: return@setPositiveButton

                    val item = AdminUser(newItemId, nameText, emailText, passwordText)
                    itemList.add(item)
                    addItemToTable(item)
                    addItemToFirebase(item)
                    addUserToFirebaseAuth(emailText, passwordText)
                }
            }
            .setNegativeButton("Cancel", null)
        dialog.show()
    }

    private fun addUserToFirebaseAuth(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "User created successfully", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMessage = task.exception?.localizedMessage ?: "Error occurred"
                    Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
    }



    private fun editItem(item: AdminUser) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_edit_item, null)
        val nameEditText: EditText = dialogView.findViewById(R.id.nameEdit)
        val emailEditText: EditText = dialogView.findViewById(R.id.emailEdit)

        nameEditText.setText(item.name)
        emailEditText.setText(item.email)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit User")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val newName = nameEditText.text.toString()
                val newEmail = emailEditText.text.toString()

                if (newName.isNotEmpty() && newEmail.isNotEmpty()) {
                    val currentUser = auth.currentUser
                    if (currentUser != null && currentUser.email == item.email) {
                        // Update the email in Firebase Authentication
                        currentUser.updateEmail(newEmail)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val updatedItem = AdminUser(item.uid, newName, newEmail)
                                    val index = itemList.indexOfFirst { it.uid == item.uid }
                                    if (index != -1) {
                                        itemList[index] = updatedItem
                                        updateTableRow(index, updatedItem)
                                        updateItemInFirebase(updatedItem)
                                        Toast.makeText(this, "User updated successfully", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    val errorMessage = task.exception?.localizedMessage ?: "Error occurred"
                                    Toast.makeText(this, "Failed to update email: $errorMessage", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Unable to update email. User not authenticated.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Name and Email cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
        dialog.show()
    }

    private fun deleteItem(item: AdminUser) {
        val itemIndex = itemList.indexOf(item)
        if (itemIndex != -1) {
            itemList.removeAt(itemIndex)
            rootDatabaseRef.child(item.uid).removeValue()
            Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTableRow(index: Int, updatedItem: AdminUser) {
        val tableRow = binding.tableLayout.getChildAt(index + 1) as TableRow
        (tableRow.getChildAt(1) as TextView).text = updatedItem.name
        (tableRow.getChildAt(2) as TextView).text = updatedItem.email
    }

    private fun loadItemsFromFirebase() {
        rootDatabaseRef.get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { dataSnapshot ->
                val item = dataSnapshot.getValue(AdminUser::class.java)
                item?.let {
                    itemList.add(it)
                    addItemToTable(it)
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load items", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addItemToFirebase(item: AdminUser) {
        rootDatabaseRef.child(item.uid).setValue(item)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateItemInFirebase(item: AdminUser) {
        rootDatabaseRef.child(item.uid).setValue(item)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun validateInput(name: String, email: String, password: String): Boolean {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun truncateString(text: String, maxLength: Int): String {
        return if (text.length > maxLength) text.take(maxLength) + "..." else text
    }

}
