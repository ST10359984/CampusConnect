package com.example.campusconnect

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminModerationActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminPostAdapter
    private val postList = mutableListOf<Post>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_moderation)

        recyclerView = findViewById(R.id.adminRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AdminPostAdapter(postList)
        recyclerView.adapter = adapter

        loadAllPosts()
    }

    private fun loadAllPosts() {
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshots ->
                postList.clear()
                for (doc in snapshots.documents) {
                    val post = doc.toObject(Post::class.java)
                    if (post != null) {
                        post.postId = doc.id
                        postList.add(post)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load posts", Toast.LENGTH_SHORT).show()
            }
    }
}