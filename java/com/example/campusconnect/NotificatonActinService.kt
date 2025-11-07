package com.example.campusconnect

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class NotificationActionService : IntentService("NotificationActionService") {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onHandleIntent(intent: Intent?) {
        intent?.let {
            val action = it.getStringExtra("action")
            val senderId = it.getStringExtra("senderId")
            val type = it.getStringExtra("type")

            Log.d("NotificationAction", "Handling action: $action for type: $type")

            when (type) {
                "friend_request" -> handleFriendRequestAction(action, senderId)
            }
        }
    }

    private fun handleFriendRequestAction(action: String?, senderId: String?) {
        val currentUserId = auth.currentUser?.uid ?: return
        senderId ?: return

        val myDocRef = db.collection("users").document(currentUserId)
        val senderDocRef = db.collection("users").document(senderId)

        when (action) {
            "accept" -> {
                db.runBatch { batch ->
                    batch.update(myDocRef, "friendRequests", FieldValue.arrayRemove(senderId))
                    batch.update(myDocRef, "friends", FieldValue.arrayUnion(senderId))
                    batch.update(senderDocRef, "friends", FieldValue.arrayUnion(currentUserId))
                }
                    .addOnSuccessListener {
                        Log.d("NotificationAction", "Friend request accepted from $senderId")
                        sendAcceptanceNotification(senderId)
                    }
                    .addOnFailureListener {
                        Log.e("NotificationAction", "Failed to accept friend request")
                    }
            }
            "decline" -> {
                myDocRef.update("friendRequests", FieldValue.arrayRemove(senderId))
                    .addOnSuccessListener {
                        Log.d("NotificationAction", "Friend request declined from $senderId")
                    }
            }
        }
    }

    private fun sendAcceptanceNotification(friendId: String) {
    }
}