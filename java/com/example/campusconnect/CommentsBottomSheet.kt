package com.example.campusconnect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CommentsBottomSheet : BottomSheetDialogFragment() {

    private lateinit var recyclerComments: RecyclerView
    private lateinit var editAddComment: EditText
    private lateinit var btnPostComment: Button
    private lateinit var commentAdapter: CommentAdapter
    private val commentList = mutableListOf<Comment>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var postId: String? = null

    companion object {
        private const val ARG_POST_ID = "post_id"
        fun newInstance(postId: String): CommentsBottomSheet {
            val fragment = CommentsBottomSheet()
            val args = Bundle()
            args.putString(ARG_POST_ID, postId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString(ARG_POST_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_comments, container, false)

        recyclerComments = view.findViewById(R.id.recyclerComments)
        editAddComment = view.findViewById(R.id.editAddComment)
        btnPostComment = view.findViewById(R.id.btnPostComment)

        setupRecyclerView()
        loadComments()

        btnPostComment.setOnClickListener {
            postComment()
        }

        return view
    }

    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter(commentList)
        recyclerComments.layoutManager = LinearLayoutManager(context)
        recyclerComments.adapter = commentAdapter
    }

    private fun loadComments() {
        if (postId == null) return

        db.collection("posts").document(postId!!)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(context, "Failed to load comments", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                commentList.clear()
                snapshots?.forEach { doc ->
                    val comment = doc.toObject(Comment::class.java)
                    commentList.add(comment)
                }
                commentAdapter.notifyDataSetChanged()
            }
    }

    private fun postComment() {
        val commentText = editAddComment.text.toString().trim()
        val user = auth.currentUser
        if (commentText.isEmpty() || postId == null || user == null) {
            return
        }

        val postRef = db.collection("posts").document(postId!!)
        val commentCollectionRef = postRef.collection("comments")

        val comment = Comment(
            commentId = commentCollectionRef.document().id,
            postId = postId!!,
            userId = user.uid,
            userName = user.displayName ?: "Unknown",
            userProfile = user.photoUrl?.toString(),
            text = commentText,
            timestamp = null
        )

        db.runBatch { batch ->
            batch.set(commentCollectionRef.document(comment.commentId), comment)
            batch.update(postRef, "commentCount", FieldValue.increment(1))
        }
            .addOnSuccessListener {
                editAddComment.text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to post comment", Toast.LENGTH_SHORT).show()
            }
    }
}