package com.example.campusconnect

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private lateinit var editName: EditText
    private lateinit var editBio: EditText
    private lateinit var txtEmail: TextView
    private lateinit var txtPostCount: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnLogout: Button
    private lateinit var btnSaveProfile: Button
    private lateinit var btnSettings: ImageButton

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val userPosts = ArrayList<Post>()
    private lateinit var postAdapter: PostAdapter
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_profile)

        imgProfile = findViewById(R.id.imgProfile)
        editName = findViewById(R.id.editName)
        editBio = findViewById(R.id.editBio)
        txtEmail = findViewById(R.id.txtEmail)
        txtPostCount = findViewById(R.id.txtPostCount)
        recyclerView = findViewById(R.id.recyclerProfile)
        btnLogout = findViewById(R.id.btnLogout)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)
        btnSettings = findViewById(R.id.btnSettings)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""

        postAdapter = PostAdapter(userPosts, currentUserId)

        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = postAdapter

        loadUserData()
        loadUserPosts()

        btnSaveProfile.setOnClickListener { saveProfile() }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, AuthActivity::class.java))
            finishAffinity() // Use finishAffinity to clear all activities
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun loadUserData() {
        if (currentUserId.isEmpty()) return

        db.collection("users").document(currentUserId).get()
            .addOnSuccessListener { doc ->
                if (doc != null) {
                    val fullName = doc.getString("name") ?: ""
                    val bio = doc.getString("bio") ?: ""
                    val email = doc.getString("email") ?: auth.currentUser?.email ?: "No Email"
                    val profileUrl = doc.getString("profileImage")

                    editName.setText(fullName)
                    editBio.setText(bio)
                    txtEmail.text = email

                    val glideApp = Glide.with(this)
                    if (!profileUrl.isNullOrEmpty()) {
                        glideApp.load(profileUrl)
                            .placeholder(R.drawable.ic_profile)
                            .circleCrop()
                            .into(imgProfile)
                    } else {
                        glideApp.load(R.drawable.ic_profile)
                            .circleCrop()
                            .into(imgProfile)
                    }
                }
            }
    }

    private fun saveProfile() {
        if (currentUserId.isEmpty()) return
        val name = editName.text.toString().trim()
        val bio = editBio.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        // --- THIS IS THE FIX ---
        // Create the map with the new lowercase field
        val updates = hashMapOf(
            "name" to name,
            "name_lowercase" to name.toLowerCase(Locale.getDefault()),
            "bio" to bio
        )

        db.collection("users").document(currentUserId)
            .update(updates as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserPosts() {
        if (currentUserId.isEmpty()) return

        db.collection("posts")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { docs ->
                userPosts.clear()
                for (doc in docs) {
                    val post = doc.toObject(Post::class.java)
                    post.postId = doc.id
                    userPosts.add(post)
                }
                txtPostCount.text = "${userPosts.size} posts"
                postAdapter.notifyDataSetChanged()
            }
    }
}