package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
    private val ADD_ITEM_REQUEST_CODE = 1  // Request code for adding/editing items
    private lateinit var rootDatabaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rootDatabaseRef = FirebaseDatabase.getInstance().getReference("MyData") // Correct database reference
        auth = FirebaseAuth.getInstance() // Initialize FirebaseAuth

        // Set up button to add a new item
        binding.addButton.setOnClickListener {
            showAddItemForm()
        }

        binding.backBtn.setOnClickListener {
            startActivity(Intent(this, AdminDashboard::class.java))
            finish()
        }

        // Initialize the table headers
        setupTableHeaders()

        // Load existing items from Firebase
        loadItemsFromFirebase()
    }

    // Set up table headers
    private fun setupTableHeaders() {
        val headerRow = TableRow(this)
        // Add header row to the table layout (you can customize the headers if needed)
        binding.tableLayout.addView(headerRow)
    }

    // Add item to the table layout
    private fun addItemToTable(item: AdminUser) {
        val tableRow = TableRow(this)

        // Set layout parameters for columns to adjust the width
        val idNumberTextView = TextView(this).apply {
            text = (itemList.indexOf(item) + 1).toString() // Sequential ID number
            gravity = Gravity.CENTER
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f) // Distribute space equally
        }

        val nameTextView = TextView(this).apply {
            text = item.name.take(7) + "..."  // Truncate name for long text
            gravity = Gravity.CENTER
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f) // Equal width
        }

        val emailTextView = TextView(this).apply {
            text = item.email.take(7) + "..."  // Truncate email for long text
            gravity = Gravity.CENTER
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f) // Equal width
        }

        val passwordTextView = TextView(this).apply {
            text = item.password.take(7) + "..."  // Truncate password for long text
            gravity = Gravity.CENTER
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f) // Equal width
        }

        val actionsTextView = TextView(this).apply {
            text = "Edit"
            setOnClickListener {
                editItem(item)
            }
            gravity = Gravity.CENTER
        }

        val deleteTextView = TextView(this).apply {
            text = "Delete"
            setOnClickListener {
                deleteItem(item)
            }
            gravity = Gravity.CENTER
        }

        // Add TextViews to the TableRow
        tableRow.addView(idNumberTextView)
        tableRow.addView(nameTextView)
        tableRow.addView(emailTextView)
        tableRow.addView(passwordTextView)
        tableRow.addView(actionsTextView)
        tableRow.addView(deleteTextView)

        // Add the TableRow to the TableLayout
        binding.tableLayout.addView(tableRow)
    }

    // Show popover form to add a new item
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

                if (nameText.isNotEmpty() && emailText.isNotEmpty() && passwordText.isNotEmpty()) {
                    // Generate a unique ID using Firebase push()
                    val newItemId = rootDatabaseRef.push().key ?: return@setPositiveButton

                    val item = AdminUser(newItemId, nameText, emailText, passwordText)
                    itemList.add(item)
                    addItemToTable(item)
                    addItemToFirebase(item)
                    addUserToFirebaseAuth(emailText, passwordText) // Add user to Firebase Authentication
                } else {
                    Toast.makeText(this@UserList, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)

        dialog.show()
    }

    // Add the user to Firebase Authentication
    private fun addUserToFirebaseAuth(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("UserList", "User created successfully in Firebase Auth")
                    Toast.makeText(this, "User created successfully in Firebase Authentication", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("UserList", "Failed to create user in Firebase Auth: ${task.exception?.message}")
                    Toast.makeText(this, "Failed to create user in Firebase Authentication", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Edit item logic
    private fun editItem(item: AdminUser) {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.activity_add_item, null)

        val nameEditText: EditText = dialogView.findViewById(R.id.nameEditText)
        val emailEditText: EditText = dialogView.findViewById(R.id.emailEditText)
        val passwordEditText: EditText = dialogView.findViewById(R.id.passwordEditText)

        nameEditText.setText(item.name)
        emailEditText.setText(item.email)
        passwordEditText.setText(item.password)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Item")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val name = nameEditText.text.toString()
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()

                if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                    val updatedItem = AdminUser(item.id, name, email, password)
                    val existingItemIndex = itemList.indexOfFirst { it.id == item.id }
                    if (existingItemIndex != -1) {
                        itemList[existingItemIndex] = updatedItem
                        updateTableRow(existingItemIndex, updatedItem)
                        updateItemInFirebase(updatedItem)
                    }
                } else {
                    Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)

        dialog.show()
    }

    // Delete item logic
    private fun deleteItem(item: AdminUser) {
        val itemIndex = itemList.indexOf(item)
        if (itemIndex != -1) {
            // Remove item from the list
            itemList.removeAt(itemIndex)

            // Remove item from Firebase
            rootDatabaseRef.child(item.id).removeValue()

            // Remove item from the table
            binding.tableLayout.removeViewAt(itemIndex + 1) // +1 to skip the header row
            Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show()
        }
    }

    // Update the row in the table when an item is edited
    private fun updateTableRow(index: Int, updatedItem: AdminUser) {
        if (index + 1 < binding.tableLayout.childCount) {
            val tableRow = binding.tableLayout.getChildAt(index + 1) as TableRow // +1 to skip header row
            tableRow.getChildAt(1).apply { (this as TextView).text = updatedItem.name }
            tableRow.getChildAt(2).apply { (this as TextView).text = updatedItem.email }
            tableRow.getChildAt(3).apply { (this as TextView).text = updatedItem.password }
        } else {
            Log.e("UserList", "Invalid index for update")
        }
    }

    // Load existing items from Firebase and populate the table
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
            Log.e("UserList", "Error loading items from Firebase: ${it.message}")
            Toast.makeText(this, "Failed to load items", Toast.LENGTH_SHORT).show()
        }
    }

    // Add item to Firebase
    private fun addItemToFirebase(item: AdminUser) {
        rootDatabaseRef.child(item.id).setValue(item)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Update the item in Firebase
    private fun updateItemInFirebase(item: AdminUser) {
        rootDatabaseRef.child(item.id).setValue(item)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("UserList", "Item updated successfully: ${item.id}")
                    Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("UserList", "Failed to update item: ${task.exception?.message}")
                    Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
