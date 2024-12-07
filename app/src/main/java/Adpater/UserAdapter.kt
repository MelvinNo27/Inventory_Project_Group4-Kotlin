package com.example.myapplication

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityTableListBinding
import com.example.myapplication.databinding.ActivityViewItemBinding

class UserAdapter(
    private val itemList: List<AdminUser>,
    private val editItem: (AdminUser) -> Unit,
    private val deleteItem: (AdminUser) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ActivityTableListBinding.inflate(LayoutInflater.from(parent.context), parent, false) // Ensure this is the item layout
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = itemList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = itemList.size

    inner class UserViewHolder(private val binding: ActivityTableListBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: AdminUser) {
            binding.idNumberAddItem.text = "ID: ${user.uid}"
            binding.nameAddItem.text = "Name: ${user.name}"
            binding.emailAddItem.text = "Email: ${user.email}"

            binding.root.setOnClickListener {
                showDetailsDialog(user)
            }
        }

        private fun showDetailsDialog(user: AdminUser) {
            val context = itemView.context
            // Inflate the layout using ViewBinding
            val binding = ActivityViewItemBinding.inflate(LayoutInflater.from(context))

            // Set the user details in the TextViews using ViewBinding
            binding.nameView.text = user.name
            binding.emailView.text = user.email

            // Show the AlertDialog with the ViewBinding root view
            AlertDialog.Builder(context)
                .setTitle("User Details")
                .setView(binding.root)  // Use the root view directly from ViewBinding
                .setPositiveButton("Update") { _, _ -> showUpdateDialog(user) }
                .setNegativeButton("Delete") { _, _ -> deleteItem(user) }
                .setNeutralButton("Close", null)
                .show()
        }




        private fun showUpdateDialog(user: AdminUser) {
            val context = itemView.context
            val dialogView = LayoutInflater.from(context).inflate(R.layout.activity_edit_item, null)
            val nameEditText = dialogView.findViewById<EditText>(R.id.nameEdit)
            val emailEditText = dialogView.findViewById<EditText>(R.id.emailEdit)

            nameEditText.setText(user.name)
            emailEditText.setText(user.email)

            emailEditText.isEnabled = false

            AlertDialog.Builder(context)
                .setTitle("Update User")
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->
                    val updatedName = nameEditText.text.toString().trim()
                    val updatedEmail = emailEditText.text.toString().trim()

                    if (updatedName.isNotEmpty()) {
                        val updatedUser = user.copy(name = updatedName, email = updatedEmail)
                        editItem(updatedUser)
                    } else {
                        AlertDialog.Builder(context)
                            .setMessage("Name cannot be empty.")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}

