package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityUserListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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


        binding.searchEditText.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    filterItemsFromFirebase(newText)
                } else {
                    loadItemsFromFirebase()
                }
                return true
            }
        })


        loadItemsFromFirebase()
    }

    private fun filterItemsFromFirebase(query: String) {
        val formattedQuery = query.lowercase() // Convert the query to lowercase

        rootDatabaseRef.orderByChild("name") // Assuming "name" is the child you're filtering
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    itemList.clear() // Clear the previous list

                    snapshot.children.forEach { dataSnapshot ->
                        val item = dataSnapshot.getValue(AdminUser::class.java)
                        item?.let {
                            if (it.name.lowercase().contains(formattedQuery)) {
                                // Check if the query is contained in the name (case-insensitive)
                                itemList.add(it)
                            }
                        }
                    }

                    userAdapter.notifyDataSetChanged() // Notify adapter about the updated data
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    showDialog("Failed to load filtered items: ${databaseError.message}")
                }
            })
    }

    private fun loadItemsFromFirebase() {
        // Reference to the Firebase database node
        rootDatabaseRef.orderByChild("name").limitToFirst(10)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    itemList.clear() // Clear the previous list
                    snapshot.children.forEach { dataSnapshot ->
                        val item = dataSnapshot.getValue(AdminUser::class.java)
                        item?.let { itemList.add(it) }
                    }
                    userAdapter.notifyDataSetChanged() // Notify adapter about the updated data
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle the error, e.g., show a dialog
                    showDialog("Failed to load items: ${databaseError.message}")
                }
            })
    }



    private fun showAddItemForm() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_add_item, null)
        val nameEditText: EditText = dialogView.findViewById(R.id.nameEditText)
        val emailEditText: EditText = dialogView.findViewById(R.id.emailEditText)
        val passwordEditText: EditText = dialogView.findViewById(R.id.passwordEditText)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val nameText = nameEditText.text.toString()
                val emailText = emailEditText.text.toString()
                val passwordText = passwordEditText.text.toString()

                if (validateInput(nameText, emailText, passwordText)) {
                    // Directly add the user to the database without checking email existence
                    val newItemId = rootDatabaseRef.push().key ?: return@setPositiveButton
                    val item = AdminUser(newItemId, nameText, emailText, passwordText)
                    addItemToFirebase(item)
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
            .setTitle("Only name can be edited")
            .setView(dialogView)
            .setPositiveButton("Done") { _, _ ->
                val newName = nameEditText.text.toString().trim()

                if (newName.isNotEmpty()) {
                    // Check if the name already exists
                    rootDatabaseRef.orderByChild("name").equalTo(newName).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists() && snapshot.children.any { it.key != item.uid }) {
                                // Name exists in the database for a different user
                                showDialog("Name already exists. Please use a different name.")
                            } else {
                                // Proceed to update the item
                                val updatedItem = AdminUser(item.uid, newName, item.email, item.password) // Email remains unchanged
                                val index = itemList.indexOfFirst { it.uid == item.uid }
                                if (index != -1) {
                                    itemList[index] = updatedItem
                                    userAdapter.notifyItemChanged(index)
                                    updateItemInFirebase(updatedItem)
                                    showDialog("User updated successfully")
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            showDialog("Error checking name: ${error.message}")
                        }
                    })
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
        // Show a confirmation dialog before deleting
        AlertDialog.Builder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete this user?")
            .setPositiveButton("Yes") { _, _ ->
                val index = itemList.indexOf(item)
                if (index != -1) {
                    itemList.removeAt(index) // Remove from the list
                    userAdapter.notifyItemRemoved(index) // Notify adapter of item removal
                    rootDatabaseRef.child(item.uid).removeValue() // Remove from Firebase Database
                }
            }
            .setNegativeButton("No", null) // Do nothing on "No"
            .show()
    }


    private fun addItemToFirebase(item: AdminUser) {
        rootDatabaseRef.orderByChild("email").equalTo(item.email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    showDialog("Email already exists in Firebase Database. Please use a different email.")
                } else {
                    // Check if the name exists
                    rootDatabaseRef.orderByChild("name").equalTo(item.name).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(nameSnapshot: DataSnapshot) {
                            if (nameSnapshot.exists()) {
                                showDialog("Name already exists. Please use a different name.")
                            } else {
                                // Proceed to add the user if email and name are unique
                                proceedToAddUser(item)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            showDialog("Error checking name: ${error.message}")
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showDialog("Error checking email: ${error.message}")
            }
        })
    }

    private fun proceedToAddUser(item: AdminUser) {
        auth.createUserWithEmailAndPassword(item.email, item.password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user // Firebase Auth user
                val newUser = user?.let {
                    AdminUser(
                        it.uid,  // Firebase user UID
                        item.name,
                        item.email,
                        item.password,
                        role = "user"
                    )
                }

                if (user != null) {
                    // Add the new user to Firebase Database
                    rootDatabaseRef.child(user.uid).setValue(newUser)
                        .addOnSuccessListener {
                            showDialog("User added successfully.")
                            auth.signOut()
                            loadItemsFromFirebase()
                            if (newUser != null) {
                                itemList.add(0, newUser) // Add the new item to the top of the list
                                userAdapter.notifyItemInserted(0) // Notify the adapter that a new item is inserted at the top
                            }
                        }
                        .addOnFailureListener {
                            showDialog("Failed to add user to database.")
                        }
                }
            }
            .addOnFailureListener { exception ->
                showDialog("Failed to create user in Firebase Authentication: ${exception.message}")
            }
    }


    private fun updateItemInFirebase(item: AdminUser) {
        rootDatabaseRef.child(item.uid).setValue(item)
            .addOnFailureListener {
                showDialog("Failed to update item")
            }
    }
    private fun validateInput(name: String, email: String, password: String): Boolean {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showDialog("All fields are required")
            return false
        }else if (!email.endsWith("@gmail.com")) {
            showDialog("Invalid email format")
            return false
        }else if (password.length < 6) {
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
