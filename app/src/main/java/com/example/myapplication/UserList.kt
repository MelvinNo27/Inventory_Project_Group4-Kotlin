package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityUserListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UserList : AppCompatActivity() {

    private lateinit var binding: ActivityUserListBinding
    private lateinit var rootDatabaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var userAdapter: UserAdapter
    private val itemList = mutableListOf<AdminUser>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rootDatabaseRef = FirebaseDatabase.getInstance().getReference("users")
        auth = FirebaseAuth.getInstance()

        // Set up RecyclerView with UserAdapter
        userAdapter = UserAdapter(itemList, this::editItem, this::deleteItem)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = userAdapter

        binding.addButton.setOnClickListener { showAddItemForm() }
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

    private fun loadItemsFromFirebase() {
        rootDatabaseRef.get().addOnSuccessListener { snapshot ->
            itemList.clear()
            snapshot.children.forEach { dataSnapshot ->
                val item = dataSnapshot.getValue(AdminUser::class.java)
                item?.let { itemList.add(it) }
            }
            userAdapter.notifyDataSetChanged()
        }.addOnFailureListener {
            showDialog("Failed to load items")
        }
    }

    private fun showAddItemForm() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_add_item, null)
        val nameEditText: EditText = dialogView.findViewById(R.id.nameEditText)
        val emailEditText: EditText = dialogView.findViewById(R.id.emailEditText)
        val passwordEditText: EditText = dialogView.findViewById(R.id.passwordEditText)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New User")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val nameText = nameEditText.text.toString()
                val emailText = emailEditText.text.toString()
                val passwordText = passwordEditText.text.toString()

                if (validateInput(nameText, emailText, passwordText)) {
                    // Check if email exists before proceeding
                    checkEmailExists(emailText) { exists ->
                        if (exists) {
                            // If email exists, notify the user
                            showDialog("Email already exists. Please use a different email.")
                        } else {
                            // If email does not exist, add the user to the database
                            val newItemId = rootDatabaseRef.push().key ?: return@checkEmailExists
                            val item = AdminUser(newItemId, nameText, emailText, passwordText)
                            addItemToFirebase(item)
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
        dialog.show()
    }

    private fun editItem(item: AdminUser) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_edit_item, null)
        val nameEditText: EditText = dialogView.findViewById(R.id.nameEdit)
        val emailEditText: EditText = dialogView.findViewById(R.id.emailEdit)

        // Populate fields with existing data
        nameEditText.setText(item.name)
        emailEditText.setText(item.email)

        // Disable email field
        emailEditText.isEnabled = false

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit User")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val newName = nameEditText.text.toString()

                if (newName.isNotEmpty()) {
                    val updatedItem = AdminUser(item.uid, newName, item.email, item.password) // Email remains unchanged
                    val index = itemList.indexOfFirst { it.uid == item.uid }
                    if (index != -1) {
                        itemList[index] = updatedItem
                        userAdapter.notifyItemChanged(index)
                        updateItemInFirebase(updatedItem)
                        showDialog("User updated successfully")
                    }
                } else {
                    showDialog("Name cannot be empty")
                }
            }
            .setNegativeButton("Cancel", null)
        dialog.show()
    }

    private fun checkEmailExists(email: String, callback: (Boolean) -> Unit) {
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    callback(!signInMethods.isNullOrEmpty()) // Email exists if signInMethods is not empty
                } else {
                    showDialog("Failed to check email: ${task.exception?.message}")
                    callback(false)
                }
            }
            .addOnFailureListener {
                showDialog("Error checking email")
                callback(false)
            }
    }

    private fun deleteItem(item: AdminUser) {
        val index = itemList.indexOf(item)
        if (index != -1) {
            itemList.removeAt(index)
            userAdapter.notifyItemRemoved(index)
            rootDatabaseRef.child(item.uid).removeValue()
        }
    }

    private fun addItemToFirebase(item: AdminUser) {
        rootDatabaseRef.orderByChild("email").equalTo(item.email)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    showDialog("Email already exists in the database.")
                } else {
                    // If no duplicate email found, add the item
                    rootDatabaseRef.child(item.uid).setValue(item)
                        .addOnSuccessListener {
                            showDialog("User added successfully.")
                        }
                        .addOnFailureListener {
                            showDialog("Failed to add item to database.")
                        }
                }
            }
            .addOnFailureListener {
                showDialog("Email already exists. Please use a different email.")
            }
    }

    private fun updateItemInFirebase(item: AdminUser) {
        rootDatabaseRef.child(item.uid).setValue(item)
            .addOnFailureListener {
                showDialog("Failed to update item")
            }
    }

    private fun addUserToFirebaseAuth(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnFailureListener {
                showDialog("Failed to create user")
            }
    }

    private fun validateInput(name: String, email: String, password: String): Boolean {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showDialog("All fields are required")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showDialog("Invalid email format")
            return false
        }
        if (password.length < 6) {
            showDialog("Password must be at least 6 characters long")
            return false
        }
        return true
    }

    // Helper function to show dialog messages
    private fun showDialog(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
