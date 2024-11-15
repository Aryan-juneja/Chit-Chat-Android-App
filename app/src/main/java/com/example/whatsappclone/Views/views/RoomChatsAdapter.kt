package com.example.whatsappclone.Views.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whatsappclone.R
import de.hdodenhof.circleimageview.CircleImageView
import localService.chatsEntityClass
import utils.formatAsListItem

class RoomChatsAdapter(
    private val chatList: List<chatsEntityClass>,
    private val onItemClick: (String, String, String) -> Unit
) : RecyclerView.Adapter<RoomChatsAdapter.RoomChatsViewHolder>() {

    class RoomChatsViewHolder(views: View) : RecyclerView.ViewHolder(views) {
        val date: TextView = views.findViewById(R.id.date)
        val profileImage: CircleImageView = views.findViewById(R.id.profile_image)
        val name: TextView = views.findViewById(R.id.name)
        val status: TextView = views.findViewById(R.id.status)
        val mssgCount: TextView = views.findViewById(R.id.count)

        // Binding data to the UI elements
        fun bind(chat: chatsEntityClass, onItemClick: (String, String, String) -> Unit) {
            name.text = chat.name
            status.text = chat.message  // Assuming `status` is a field in chatsEntityClass
            date.text = chat.time.formatAsListItem(itemView.context) // Assuming you have a date field in chatsEntityClass

            // Load the profile image using Glide
            Glide.with(itemView.context)
                .load(chat.image) // Assuming `image` is the field containing the image URL
                .placeholder(R.drawable.man303792640) // Placeholder while loading
                .into(profileImage)

            // Set message count visibility and text
            if (chat.count > 0) { // Assuming mssgCount is an integer
                mssgCount.isVisible = true
                mssgCount.text = chat.count.toString()
            } else {
                mssgCount.isVisible = false
            }

            // Set click listener
            itemView.setOnClickListener {
                onItemClick(chat.name, chat.image, chat.from) // Assuming `from` is also a field
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomChatsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.usermodal, parent, false)
        return RoomChatsViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomChatsViewHolder, position: Int) {
        holder.bind(chatList[position], onItemClick)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }
}
