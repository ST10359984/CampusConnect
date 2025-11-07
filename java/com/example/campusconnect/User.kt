package com.example.campusconnect

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val name_lowercase: String = "",
    val profileImage: String? = null,
    val friends: List<String> = emptyList(),
    val friendRequests: List<String> = emptyList()
)