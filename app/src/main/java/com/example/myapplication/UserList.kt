package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityUserListBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UserList : AppCompatActivity() {

    data class Item(val id: String = "", val name: String = "", val quantity: String = "", val status: String = "")


    private lateinit var binding: ActivityUserListBinding
    private val itemList = mutableListOf<Item>()
    private val ADD_ITEM_REQUEST_CODE = 1  // Request code for adding/editing items
    private lateinit var rootDatabaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rootDatabaseRef =
            FirebaseDatabase.getInstance().getReference("MyData") // Correct database reference

        // Set up button to add a new item
        binding.addButton.setOnClickListener {
            showAddItemForm()
        }

        // Initialize the table headers
        setupTableHeaders()

        // Load existing items from Firebase
        loadItemsFromFirebase()
    }

    // Set up table headers
    private fun setupTableHeaders() {
        val headerRow = TableRow(this)

        // Define headers for each column
        val headers = arrayOf("ID", "Name", "Quantity", "Status", "Actions")
        headers.forEach { text ->
            val header = TextView(this).apply {
                this.text = text
                setPadding(8, 8, 8, 8)  // Reduced padding for header
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                textSize = 12f  // Adjusted text size for header
            }
            headerRow.addView(header)
        }

        // Add header row to the table layout
        binding.tableLayout.addView(headerRow)
    }

    // Add item to the table layout
    private fun addItemToTable(item: Item) {
        val tableRow = TableRow(this)

        // Set layout parameters for columns to adjust the width
        var layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )

        // Create TextViews for each field (ID, Name, Quantity, Status)
        val idTextView = TextView(this).apply {
            text = item.id
            layoutParams = layoutParams
            setPadding(8, 8, 8, 8)  // Padding around the text
            textSize = 14f
        }

        val nameTextView = TextView(this).apply {
            text = item.name
            layoutParams = layoutParams
            setPadding(8, 8, 8, 8)
            textSize = 14f
        }

        val quantityTextView = TextView(this).apply {
            text = item.quantity
            layoutParams = layoutParams
            setPadding(8, 8, 8, 8)
            textSize = 14f
        }

        val statusTextView = TextView(this).apply {
            text = item.status
            layoutParams = layoutParams
            setPadding(8, 8, 8, 8)
            textSize = 14f
        }

        val actionsTextView = TextView(this).apply {
            text = "Edit"
            layoutParams = layoutParams
            setPadding(8, 8, 8, 8)
            textSize = 14f
            setOnClickListener {
                editItem(item)
            }
        }

        // Add TextViews to the TableRow
        tableRow.addView(idTextView)
        tableRow.addView(nameTextView)
        tableRow.addView(quantityTextView)
        tableRow.addView(statusTextView)
        tableRow.addView(actionsTextView)

        // Add the TableRow to the TableLayout
        binding.tableLayout.addView(tableRow)
    }


    // Show popover form to add a new item
    private fun showAddItemForm() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_add_item, null)

        val nameEditText: EditText = dialogView.findViewById(R.id.nameEditText)
        val emailEditText: EditText = dialogView.findViewById(R.id.emailEditText)
        val statusEditText: EditText = dialogView.findViewById(R.id.statusEditText)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Item")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val nameText = nameEditText.text.toString()
                val emailText = emailEditText.text.toString()
                val statusText = statusEditText.text.toString()

                if (nameText.isNotEmpty() && emailText.isNotEmpty() && statusText.isNotEmpty()) {
                    // Generate a unique ID using Firebase push()
                    val newItemId = rootDatabaseRef.push().key ?: return@setPositiveButton

                    val item = Item(newItemId, nameText, emailText, statusText)
                    Log.d("AddItem", "Item to add: $item")
                    itemList.add(item)
                    addItemToTable(item)
                    addItemToFirebase(item)
                } else {
                    Toast.makeText(this@UserList, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)

        dialog.show()
    }



    // Edit item logic
    private fun editItem(item: Item) {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.activity_add_item, null)

        val idEditText: EditText = dialogView.findViewById(R.id.idEditText)
        val nameEditText: EditText = dialogView.findViewById(R.id.nameEditText)
        val emailEditText: EditText = dialogView.findViewById(R.id.emailEditText)
        val statusEditText: EditText = dialogView.findViewById(R.id.statusEditText)

        idEditText.setText(item.id)
        nameEditText.setText(item.name)
        emailEditText.setText(item.quantity)
        statusEditText.setText(item.status)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Item")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val id = idEditText.text.toString()
                val name = nameEditText.text.toString()
                val email = emailEditText.text.toString()
                val status = statusEditText.text.toString()

                val updatedItem = Item(id, name, email, status)
                val existingItemIndex = itemList.indexOfFirst { it.id == item.id }
                if (existingItemIndex != -1) {
                    itemList[existingItemIndex] = updatedItem
                    updateTableRow(existingItemIndex, updatedItem)
                    updateItemInFirebase(updatedItem)
                }
            }
            .setNegativeButton("Cancel", null)

        dialog.show()
    }

    // Handle the result from AddItem activity (Add or Update)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_ITEM_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val id = data.getStringExtra("ID")
            val name = data.getStringExtra("Name")
            val quantity = data.getStringExtra("Quantity")
            val status = data.getStringExtra("Status")

            if (id != null && name != null && quantity != null && status != null) {
                val existingItemIndex = itemList.indexOfFirst { it.id == id }
                val item = Item(id, name, quantity, status)

                if (existingItemIndex != -1) {
                    // Update item in the list and in Firebase
                    itemList[existingItemIndex] = item
                    updateTableRow(existingItemIndex, item)
                    updateItemInFirebase(item)
                } else {
                    // Add new item to the list and Firebase
                    itemList.add(item)
                    addItemToTable(item)
                    addItemToFirebase(item)
                }
            }
        }
    }

    // Create new item and add to Firebase
// Create new item and add to Firebase
    private fun addItemToFirebase(item: Item) {
        rootDatabaseRef.child( item.id).setValue(item)
    }


    // Update the item in Firebase
    private fun updateItemInFirebase(item: Item) {
        rootDatabaseRef.child(item.id).setValue(item)
    }

    // Update the row in the table when an item is edited
    private fun updateTableRow(index: Int, updatedItem: Item) {
        val tableRow = binding.tableLayout.getChildAt(index + 1) as TableRow // +1 to skip header row
        tableRow.getChildAt(0).apply { (this as TextView).text = updatedItem.id }
        tableRow.getChildAt(1).apply { (this as TextView).text = updatedItem.name }
        tableRow.getChildAt(2).apply { (this as TextView).text = updatedItem.quantity }
        tableRow.getChildAt(3).apply { (this as TextView).text = updatedItem.status }
    }

    // Load existing items from Firebase and populate the table
    private fun loadItemsFromFirebase() {
        rootDatabaseRef.get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { dataSnapshot ->
                val item = dataSnapshot.getValue(Item::class.java)
                item?.let {
                    itemList.add(it)
                    addItemToTable(it)
                }
            }
        }
    }
}
