package com.example.campusconnect

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Comment(
    val commentId: String = "",
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfile: String? = null,
    val text: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
)