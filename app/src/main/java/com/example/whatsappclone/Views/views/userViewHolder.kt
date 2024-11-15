package com.example.whatsappclone.Views.views

import Models.SharedViewModel
import Models.User
import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whatsappclone.R
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView

class userViewHolder(private val view: View, private val viewModel: SharedViewModel) : RecyclerView.ViewHolder(view) {

    val auth by lazy {
        FirebaseAuth.getInstance()
    }
    fun bind(user: User, click: (name: String, photo: String, UID: String) -> Unit) = with(view) {
        val date:TextView =view.findViewById((R.id.date))
        val profileImage: CircleImageView = view.findViewById(R.id.profile_image)
        val name: TextView = view.findViewById(R.id.name)
        val status: TextView = view.findViewById(R.id.status)
        val mssgCount:TextView=view.findViewById(R.id.count)
        date.isVisible=false
        mssgCount.isVisible=false
        viewModel.darkMode.observe(itemView.context as LifecycleOwner){
                if(it){
                    name.setTypeface(name.typeface, Typeface.BOLD)
                    name.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                    status.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                }
                else{
                    name.setTypeface(name.typeface, Typeface.BOLD)
                    name.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                    status.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                }
        }
        Glide.with(itemView.context)
            .load(user.imageUrl)
            .placeholder(R.drawable.man303792640)
            .into(profileImage)
            name.text = user.name
        status.text = user.status
        setOnClickListener {
            click(user.name,user.imageUrl,user.uid)
        }
    }
}
