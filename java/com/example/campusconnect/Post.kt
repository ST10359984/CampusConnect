package com.example.campusconnect

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Post(
    var postId: String = "",
    val userId: String? = null,
    val userName: String? = null,
    val userProfile: String? = null,
    val text: String? = null,
    val imageUrl: String? = null,

    @ServerTimestamp
    val timestamp: Date? = null,

    val likes: List<String> = emptyList(),

    val commentCount: Int = 0
)