package com.example.campusconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class PostAdapter(
    private val posts: MutableList<Post>,
    private val currentUserId: String
) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val db = FirebaseFirestore.getInstance().collection("posts")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int = posts.size

    fun updateList(newList: List<Post>) {
        posts.clear()
        posts.addAll(newList)
        notifyDataSetChanged()
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProfile: ImageView = itemView.findViewById(R.id.imgUserProfile)
        private val txtUserName: TextView = itemView.findViewById(R.id.txtUserName)
        private val txtPostText: TextView = itemView.findViewById(R.id.txtPostText)
        private val imgPost: ImageView = itemView.findViewById(R.id.imgPost)
        private val btnLike: ImageButton = itemView.findViewById(R.id.btnLike)
        private val txtLikes: TextView = itemView.findViewById(R.id.txtLikes)
        private val btnComment: ImageButton = itemView.findViewById(R.id.btnComment)
        private val txtComments: TextView = itemView.findViewById(R.id.txtComments)

        fun bind(post: Post) {
            txtUserName.text = post.userName
            txtPostText.text = post.text

            Glide.with(itemView.context)
                .load(post.userProfile)
                .placeholder(R.drawable.ic_profile)
                .circleCrop()
                .into(imgProfile)

            if (!post.imageUrl.isNullOrEmpty()) {
                imgPost.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(post.imageUrl)
                    .into(imgPost)
            } else {
                imgPost.visibility = View.GONE
            }

            txtLikes.text = "${post.likes.size} likes"

            if (post.likes.contains(currentUserId)) {
                btnLike.setImageResource(R.drawable.ic_like_filled)
            } else {
                btnLike.setImageResource(R.drawable.ic_like)
            }

            btnLike.setOnClickListener {
                toggleLike(post)
            }

            if (post.commentCount > 0) {
                txtComments.text = "View all ${post.commentCount} comments"
                txtComments.visibility = View.VISIBLE
            } else {
                txtComments.text = "No comments yet"
            }

            btnComment.setOnClickListener {
                openComments(post.postId)
            }
            txtComments.setOnClickListener {
                openComments(post.postId)
            }
        }

        private fun toggleLike(post: Post) {
            val postRef = db.document(post.postId)

            if (post.likes.contains(currentUserId)) {
                postRef.update("likes", FieldValue.arrayRemove(currentUserId))
            } else {
                postRef.update("likes", FieldValue.arrayUnion(currentUserId))
            }
        }

        private fun openComments(postId: String) {
            val context = itemView.context
            if (context is AppCompatActivity) {
                val commentsBottomSheet = CommentsBottomSheet.newInstance(postId)
                commentsBottomSheet.show(context.supportFragmentManager, "CommentsBottomSheet")
            }
        }
    }
}