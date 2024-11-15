package Adapters

import Models.User
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.R
import de.hdodenhof.circleimageview.CircleImageView
import com.bumptech.glide.Glide // Add this line to import Glide

class RecyclerViewAdapter(private val users: Array<User>) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date:TextView =view.findViewById((R.id.date))
        val profileImage: CircleImageView = view.findViewById(R.id.profile_image)
        val name: TextView = view.findViewById(R.id.name)
        val status: TextView = view.findViewById(R.id.status)
        val mssgCount:TextView=view.findViewById(R.id.count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.usermodal, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.date.isVisible=false
        holder.mssgCount.isVisible=false
        val user = users[position]
        // Load image using Glide (or any other image loading library)
        Glide.with(holder.itemView.context)
            .load(Uri.parse(user.imageUrl))
            .placeholder(R.drawable.man303792640)
            .into(holder.profileImage)
             holder.name.text = user.name
            holder.status.text = user.status
    }
}
