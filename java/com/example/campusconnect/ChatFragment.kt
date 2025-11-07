package com.example.campusconnect

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatList: MutableList<ChatMessage>
    private lateinit var dbRef: DatabaseReference
    private val currentUserId get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewChat)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        chatList = mutableListOf()
        chatAdapter = ChatAdapter(chatList)
        recyclerView.adapter = chatAdapter

        dbRef = FirebaseDatabase.getInstance().reference.child("Chats")
        dbRef.keepSynced(true)

        loadMessages()

        val editMessage = view.findViewById<TextInputEditText>(R.id.editMessage)
        val messageInputLayout = view.findViewById<TextInputLayout>(R.id.messageInputLayout)

        messageInputLayout.setEndIconOnClickListener {
            val msg = editMessage.text.toString().trim()
            val senderId = currentUserId ?: return@setEndIconOnClickListener

            if (msg.isNotEmpty()) {
                val chatMsg = ChatMessage(
                    senderId = senderId,
                    receiverId = "all",
                    message = msg,
                    timestamp = System.currentTimeMillis()
                )
                dbRef.push().setValue(chatMsg)
                editMessage.text?.clear()
            }
        }

        return view
    }

    private fun loadMessages() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (snap in snapshot.children) {
                    val chat = snap.getValue(ChatMessage::class.java)
                    if (chat != null && chat.receiverId == "all") chatList.add(chat)
                }
                chatAdapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(chatList.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}