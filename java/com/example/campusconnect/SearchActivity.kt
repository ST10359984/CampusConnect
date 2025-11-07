package com.example.campusconnect

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SearchActivity : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchAdapter: PostAdapter
    private lateinit var postList: MutableList<Post>
    private lateinit var searchField: EditText

    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private var currentUserId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchField = view.findViewById(R.id.editSearch)
        recyclerView = view.findViewById(R.id.recyclerViewSearch)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        postList = mutableListOf()

        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""

        searchAdapter = PostAdapter(postList, currentUserId)
        recyclerView.adapter = searchAdapter

        searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            postList.clear()
            searchAdapter.notifyDataSetChanged()
            return
        }

        db.collection("posts")
            .whereGreaterThanOrEqualTo("text", query)
            .whereLessThanOrEqualTo("text", query + "\uf8ff")
            .get()
            .addOnSuccessListener { docs ->
                postList.clear()
                for (doc in docs) {
                    val post = doc.toObject(Post::class.java)
                    post.postId = doc.id
                    postList.add(post)
                }
                searchAdapter.notifyDataSetChanged()
            }
    }
}