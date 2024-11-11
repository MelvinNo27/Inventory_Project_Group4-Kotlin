package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityUserListBinding

data class Item(val id: String, val name: String, val quantity: String, val status: String)

class UserList : AppCompatActivity() {

    private lateinit var binding: ActivityUserListBinding
    private val itemList = mutableListOf<Item>()
    private val ADD_ITEM_REQUEST_CODE = 1  // Request code for adding/editing items

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up button to add a new item
        binding.addButton.setOnClickListener {
            val intent = Intent(this, AddItem::class.java)
            startActivityForResult(intent, ADD_ITEM_REQUEST_CODE)
        }

        // Initialize the table headers
        setupTableHeaders()
    }

    // Set up table headers
    private fun setupTableHeaders() {
        val headerRow = TableRow(this)

        // Define headers for each column
        val headers = arrayOf("ID", "Name", "Quantity", "Status", "Actions")
        headers.forEach { text ->
            val header = TextView(this).apply {
                this.text = text
                setPadding(8, 8, 8, 8)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                textSize = 14f
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
            }
            headerRow.addView(header)
        }

        // Add header row to the table layout
        binding.tableLayout.addView(headerRow)
    }

    // Add item to the table layout
    private fun addItemToTable(item: Item) {
        val tableRow = TableRow(this)

        // Create text views for each item attribute
        val idTextView = createTextView(item.id)
        val nameTextView = createTextView(item.name)
        val quantityTextView = createTextView(item.quantity)
        val statusTextView = createTextView(item.status)
        val actionsTextView = TextView(this).apply {
            text = "Edit"
            setPadding(16, 16, 16, 16)
            setOnClickListener {
                editItem(item)
            }
        }

        // Add views to the row
        tableRow.addView(idTextView)
        tableRow.addView(nameTextView)
        tableRow.addView(quantityTextView)
        tableRow.addView(statusTextView)
        tableRow.addView(actionsTextView)

        binding.tableLayout.addView(tableRow)
    }

    // Create text view helper method for creating consistent text views
    private fun createTextView(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            setPadding(16, 16, 16, 16)
        }
    }

    // Edit item logic
    private fun editItem(item: Item) {
        val intent = Intent(this, AddItem::class.java).apply {
            putExtra("ID", item.id)
            putExtra("Name", item.name)
            putExtra("Quantity", item.quantity)
            putExtra("Status", item.status)
        }
        startActivityForResult(intent, ADD_ITEM_REQUEST_CODE)
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
                // Check if the item with the same ID exists, and update it
                val existingItemIndex = itemList.indexOfFirst { it.id == id }
                if (existingItemIndex != -1) {
                    val updatedItem = Item(id, name, quantity, status)
                    itemList[existingItemIndex] = updatedItem
                    updateTableRow(existingItemIndex, updatedItem)
                } else {
                    createItem(id, name, quantity, status)
                }
            }
        }
    }

    // Create new item and add to the list and table
    private fun createItem(id: String, name: String, quantity: String, status: String) {
        val item = Item(id, name, quantity, status)
        itemList.add(item)
        addItemToTable(item)
    }

    // Update the row in the table when an item is edited
    private fun updateTableRow(index: Int, updatedItem: Item) {
        val tableRow = binding.tableLayout.getChildAt(index + 1) as TableRow // +1 to skip header row
        tableRow.getChildAt(0).apply { (this as TextView).text = updatedItem.id }
        tableRow.getChildAt(1).apply { (this as TextView).text = updatedItem.name }
        tableRow.getChildAt(2).apply { (this as TextView).text = updatedItem.quantity }
        tableRow.getChildAt(3).apply { (this as TextView).text = updatedItem.status }
    }
}
