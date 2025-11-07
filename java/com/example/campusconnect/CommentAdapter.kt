package com.example.campusconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class CommentAdapter(private val commentList: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        holder.bind(comment)
    }

    override fun getItemCount(): Int = commentList.size

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProfile: ImageView = itemView.findViewById(R.id.imgCommentProfile)
        private val txtUserName: TextView = itemView.findViewById(R.id.txtCommentUserName)
        private val txtComment: TextView = itemView.findViewById(R.id.txtCommentText)
        private val txtTimestamp: TextView = itemView.findViewById(R.id.txtCommentTimestamp)

        fun bind(comment: Comment) {
            txtUserName.text = comment.userName
            txtComment.text = comment.text

            Glide.with(itemView.context)
                .load(comment.userProfile)
                .placeholder(R.drawable.ic_profile)
                .circleCrop()
                .into(imgProfile)

            comment.timestamp?.let {
                val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                txtTimestamp.text = sdf.format(it)
            }
        }
    }
}