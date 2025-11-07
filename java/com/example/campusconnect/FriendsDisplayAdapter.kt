package com.example.campusconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class FriendsDisplayAdapter(
    private val userList: List<User>,
    private val onRemoveClicked: (User) -> Unit
) : RecyclerView.Adapter<FriendsDisplayAdapter.FriendViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        // We can reuse your existing item_friend.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = userList.size

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProfile: ImageView = itemView.findViewById(R.id.imgFriendProfile)
        private val txtName: TextView = itemView.findViewById(R.id.txtFriendName)
        private val btnAction: Button = itemView.findViewById(R.id.btnRemoveFriend)

        fun bind(user: User) {
            txtName.text = user.name

            Glide.with(itemView.context)
                .load(user.profileImage)
                .placeholder(R.drawable.ic_profile)
                .circleCrop()
                .into(imgProfile)

            // Set button text and click listener
            btnAction.text = "Remove"
            btnAction.setOnClickListener {
                onRemoveClicked(user)
            }
        }
    }
}