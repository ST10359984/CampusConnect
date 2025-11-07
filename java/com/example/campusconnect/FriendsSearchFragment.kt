package com.example.campusconnect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.util.Locale

class FriendsSearchFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchField: EditText
    private lateinit var btnSearch: Button

    private lateinit var searchAdapter: UserSearchAdapter
    private val searchResults = mutableListOf<User>()
    private val foundUserIds = mutableSetOf<String>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friends, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewFriends)
        searchField = view.findViewById(R.id.searchFriendField)
        btnSearch = view.findViewById(R.id.btnAddFriend)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        searchAdapter = UserSearchAdapter(searchResults) { user ->
            sendFriendRequest(user)
        }
        recyclerView.adapter = searchAdapter

        btnSearch.text = "Search"
        searchField.hint = "Search by name or email"

        btnSearch.setOnClickListener {
            val query = searchField.text.toString().trim()
            if (query.isNotEmpty()) {
                performSearch(query)
            }
        }

        return view
    }

    private fun performSearch(query: String) {
        if (currentUserId == null) return

        // Convert the search query to lowercase
        val searchQuery = query.toLowerCase(Locale.getDefault())

        searchResults.clear()
        foundUserIds.clear()

        // Query for lowercase email
        val emailQuery = db.collection("users")
            .whereEqualTo("email", searchQuery)
            .get()

        // Query for lowercase name
        val nameQuery = db.collection("users")
            .orderBy("name_lowercase")
            .startAt(searchQuery)
            .endAt(searchQuery + '\uf8ff')
            .get()

        Tasks.whenAllSuccess<QuerySnapshot>(emailQuery, nameQuery)
            .addOnSuccessListener { resultsList ->

                for (doc in resultsList[0].documents) {
                    val user = doc.toObject(User::class.java)
                    if (user != null && user.uid != currentUserId && !foundUserIds.contains(user.uid)) {
                        searchResults.add(user)
                        foundUserIds.add(user.uid)
                    }
                }

                for (doc in resultsList[1].documents) {
                    val user = doc.toObject(User::class.java)
                    if (user != null && user.uid != currentUserId && !foundUserIds.contains(user.uid)) {
                        searchResults.add(user)
                        foundUserIds.add(user.uid)
                    }
                }

                searchAdapter.notifyDataSetChanged()
                if (searchResults.isEmpty()) {
                    Toast.makeText(context, "No users found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Search failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendFriendRequest(user: User) {
        if (currentUserId == null) return
        val targetUid = user.uid

        db.collection("users").document(targetUid)
            .update("friendRequests", com.google.firebase.firestore.FieldValue.arrayUnion(currentUserId))
            .addOnSuccessListener {
                Toast.makeText(context, "Friend request sent to ${user.name}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to send request", Toast.LENGTH_SHORT).show()
            }
    }
}