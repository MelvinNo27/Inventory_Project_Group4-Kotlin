package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RoomAdapter(
    private val roomList: List<Rooms>,
    private val onRoomClick: (Rooms) -> Unit
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomImage: ImageView = itemView.findViewById(R.id.roomImageView)
        val roomName: TextView = itemView.findViewById(R.id.roomNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_rooms, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = roomList[position]
        holder.roomName.text = room.name
        holder.roomImage.setImageResource(R.drawable.door14) // Default door image
        holder.itemView.setOnClickListener {
            onRoomClick(room)
        }
    }

    override fun getItemCount(): Int = roomList.size
}
