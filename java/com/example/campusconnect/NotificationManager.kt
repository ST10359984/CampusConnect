package com.example.campusconnect

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object NotificationManager {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun sendFriendRequestNotification(context: Context, toUserId: String, fromUserName: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        val notificationData = mapOf(
            "title" to "New Friend Request",
            "message" to "$fromUserName sent you a friend request",
            "type" to "friend_request",
            "senderId" to currentUserId
        )

        db.collection("users").document(toUserId)
            .collection("notifications")
            .add(notificationData)
    }

    fun sendMessageNotification(context: Context, toUserId: String, fromUserName: String, message: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        val notificationData = mapOf(
            "title" to "New Message from $fromUserName",
            "message" to message,
            "type" to "new_message",
            "senderId" to currentUserId
        )

        db.collection("users").document(toUserId)
            .collection("notifications")
            .add(notificationData)
    }

    fun sendLikeNotification(context: Context, toUserId: String, fromUserName: String, postId: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        val notificationData = mapOf(
            "title" to "New Like",
            "message" to "$fromUserName liked your post",
            "type" to "like",
            "postId" to postId,
            "senderId" to currentUserId
        )

        db.collection("users").document(toUserId)
            .collection("notifications")
            .add(notificationData)
    }
}