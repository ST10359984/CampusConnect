package com.example.campusconnect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AcceptFriendRequestsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FriendRequestAdapter
    private val requestUserList = mutableListOf<User>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_accept_friend_requests, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewRequests)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = FriendRequestAdapter(
            requestUserList,
            onAccept = { user -> acceptRequest(user) },
            onDecline = { user -> declineRequest(user) }
        )
        recyclerView.adapter = adapter

        loadRequests()

        return view
    }

    private fun loadRequests() {
        if (currentUserId == null) return

        db.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Failed to load requests", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    val requestUids = user?.friendRequests ?: emptyList()

                    if (requestUids.isEmpty()) {
                        requestUserList.clear()
                        adapter.notifyDataSetChanged()
                    } else {
                        fetchUserProfiles(requestUids)
                    }
                }
            }
    }

    private fun fetchUserProfiles(uids: List<String>) {
        db.collection("users").whereIn("uid", uids)
            .get()
            .addOnSuccessListener { querySnapshot ->
                requestUserList.clear()
                for (doc in querySnapshot.documents) {
                    val user = doc.toObject(User::class.java)
                    if (user != null) {
                        requestUserList.add(user)
                    }
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun acceptRequest(sender: User) {
        if (currentUserId == null) return
        val senderId = sender.uid

        val myDocRef = db.collection("users").document(currentUserId)
        val senderDocRef = db.collection("users").document(senderId)

        db.runBatch { batch ->
            batch.update(myDocRef, "friendRequests", FieldValue.arrayRemove(senderId))
            batch.update(myDocRef, "friends", FieldValue.arrayUnion(senderId))
            batch.update(senderDocRef, "friends", FieldValue.arrayUnion(currentUserId))
        }
            .addOnSuccessListener {
                Toast.makeText(context, "Friend added!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to add friend", Toast.LENGTH_SHORT).show()
            }
    }

    private fun declineRequest(sender: User) {
        if (currentUserId == null) return

        db.collection("users").document(currentUserId)
            .update("friendRequests", FieldValue.arrayRemove(sender.uid))
            .addOnSuccessListener {
                Toast.makeText(context, "Request declined", Toast.LENGTH_SHORT).show()
            }
    }
}