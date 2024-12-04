package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityTableListBinding
import kotlin.reflect.KFunction1

class UserAdapter(
    private val userList: MutableList<AdminUser>,
    private val onEditClick: KFunction1<AdminUser, Unit>,
    private val onDeleteClick: KFunction1<AdminUser, Unit>
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    // Define a ViewHolder class using view binding
    class UserViewHolder(val binding: ActivityTableListBinding) : RecyclerView.ViewHolder(binding.root)

    // This method is called when a new view is created. It inflates the item layout and returns a ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ActivityTableListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    // This method is called to bind the data to the views in each item.
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        with(holder.binding) {
            // Set the data for the current item
            idNumberAddItem.text = (position + 1).toString()
            nameAddItem.text = user.name
            emailAddItem.text = user.email

            // Set listeners for Edit and Delete icons
            editImageView.setOnClickListener {
                onEditClick(user)
            }

            deleteImageView.setOnClickListener {
                onDeleteClick(user)
            }
        }
    }

    // Return the total number of items in the data set
    override fun getItemCount(): Int = userList.size
}
