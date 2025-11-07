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

class FriendsListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var friendsAdapter: FriendsDisplayAdapter
    private val friendsList = mutableListOf<User>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friends_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewFriends)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        friendsAdapter = FriendsDisplayAdapter(friendsList) { friend ->
            removeFriend(friend)
        }
        recyclerView.adapter = friendsAdapter

        loadFriends()

        return view
    }

    private fun loadFriends() {
        if (currentUserId == null) return

        db.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Failed to load friends", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    val friendUids = user?.friends ?: emptyList()

                    if (friendUids.isEmpty()) {
                        friendsList.clear()
                        friendsAdapter.notifyDataSetChanged()
                    } else {
                        fetchFriendProfiles(friendUids)
                    }
                }
            }
    }

    private fun fetchFriendProfiles(uids: List<String>) {
        db.collection("users").whereIn("uid", uids)
            .get()
            .addOnSuccessListener { querySnapshot ->
                friendsList.clear()
                for (doc in querySnapshot.documents) {
                    val user = doc.toObject(User::class.java)
                    if (user != null) {
                        friendsList.add(user)
                    }
                }
                friendsAdapter.notifyDataSetChanged()
            }
    }

    private fun removeFriend(friend: User) {
        if (currentUserId == null) return
        val friendId = friend.uid

        val myDocRef = db.collection("users").document(currentUserId)
        val friendDocRef = db.collection("users").document(friendId)

        db.runBatch { batch ->
            batch.update(myDocRef, "friends", FieldValue.arrayRemove(friendId))
            batch.update(friendDocRef, "friends", FieldValue.arrayRemove(currentUserId))
        }
            .addOnSuccessListener {
                Toast.makeText(context, "Friend removed", Toast.LENGTH_SHORT).show()
                // The snapshot listener will auto-update the list
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to remove friend", Toast.LENGTH_SHORT).show()
            }
    }
}