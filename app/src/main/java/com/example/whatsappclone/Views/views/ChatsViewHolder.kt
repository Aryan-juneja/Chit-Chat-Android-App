package com.example.whatsappclone.Views.views

import Models.SharedViewModel
import Models.chats
import android.graphics.Typeface
import android.util.Log
import android.view.View
import android.widget.TextView
import com.example.whatsappclone.R // Ensure this is the correct import for your project
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import utils.formatAsListItem

class ChatsViewHolder(private val views: View, private val viewModel: SharedViewModel): RecyclerView.ViewHolder(views) {
    val auth by lazy {
        FirebaseAuth.getInstance()
    }


    fun bind(chat: chats, click: (name: String, photo: String, UID: String) -> Unit) = with(views) {

        Log.d("TAG", "bind: $chat")
        val date: TextView =views.findViewById((com.example.whatsappclone.R.id.date))
        val profileImage: CircleImageView = views.findViewById(com.example.whatsappclone.R.id.profile_image)
        val name: TextView = views.findViewById(com.example.whatsappclone.R.id.name)
        val status: TextView = views.findViewById(com.example.whatsappclone.R.id.status)
        val mssgCount: TextView =views.findViewById(com.example.whatsappclone.R.id.count)
        viewModel.darkMode.observe(itemView.context as LifecycleOwner){
            if(it){
                name.setTypeface(name.typeface, Typeface.BOLD)
                name.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                status.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                date.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
            }
            else{
                name.setTypeface(name.typeface, Typeface.BOLD)
                name.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                status.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                date.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
            }
        }
        date.text =chat.time.formatAsListItem(context)
        mssgCount.visibility =View.GONE
        if(chat.count>0){
            mssgCount.isVisible=true
            mssgCount.text=chat.count.toString()
        }
        com.bumptech.glide.Glide.with(itemView.context)
            .load(chat.image)
            .placeholder(com.example.whatsappclone.R.drawable.man303792640)
            .into(profileImage)
        name.text = chat.name
        status.text = chat.mssg
        setOnClickListener {
            click(chat.name,chat.image,chat.from)
        }
    }
}