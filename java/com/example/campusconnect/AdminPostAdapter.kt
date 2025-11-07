package com.example.campusconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class AdminPostAdapter(
    private val postList: MutableList<Post>
) : RecyclerView.Adapter<AdminPostAdapter.AdminPostViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminPostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post_admin, parent, false)
        return AdminPostViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminPostViewHolder, position: Int) {
        val post = postList[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int = postList.size

    inner class AdminPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtUser: TextView = itemView.findViewById(R.id.txtAdminPostUser)
        private val txtPost: TextView = itemView.findViewById(R.id.txtAdminPostText)
        private val imgPost: ImageView = itemView.findViewById(R.id.imgAdminPostImage)
        private val btnDelete: Button = itemView.findViewById(R.id.btnAdminDeletePost)

        fun bind(post: Post) {
            txtUser.text = "Posted by: ${post.userName} (ID: ${post.userId})"
            txtPost.text = post.text

            if (!post.imageUrl.isNullOrEmpty()) {
                imgPost.visibility = View.VISIBLE
                Glide.with(itemView.context).load(post.imageUrl).into(imgPost)
            } else {
                imgPost.visibility = View.GONE
            }

            btnDelete.setOnClickListener {
                db.collection("posts").document(post.postId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(itemView.context, "Post deleted", Toast.LENGTH_SHORT).show()
                        postList.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition)
                    }
                    .addOnFailureListener {
                        Toast.makeText(itemView.context, "Failed to delete", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}