package com.example.campusconnect

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AddPostActivity : AppCompatActivity() {

    private lateinit var imgPreview: ImageView
    private lateinit var editPost: EditText
    private lateinit var captionField: EditText
    private lateinit var btnUpload: Button
    private lateinit var btnSelectImage: Button
    private var imageUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            imgPreview.setImageURI(uri)
            Log.d("AddPostActivity", "Selected image URI: $uri")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_create_post)

        imgPreview = findViewById(R.id.imgPreview)
        editPost = findViewById(R.id.editPost)
        captionField = findViewById(R.id.captionField)
        btnUpload = findViewById(R.id.btnUpload)
        btnSelectImage = findViewById(R.id.btnSelectImage)

        btnSelectImage.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        btnUpload.setOnClickListener {
            uploadPost()
        }
    }

    private fun uploadPost() {
        val caption = captionField.text.toString().trim()
        val postText = editPost.text.toString().trim()
        val userId = auth.currentUser?.uid ?: return

        if (imageUri == null && postText.isEmpty() && caption.isEmpty()) {
            Toast.makeText(this, "Please add an image or some text", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            val filename = "posts/${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference.child(filename)

            storageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        savePostToFirestore(userId, caption, postText, uri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            savePostToFirestore(userId, caption, postText, null)
        }
    }

    private fun savePostToFirestore(userId: String, caption: String, postText: String, imageUrl: String?) {

        val post = Post(
            userId = userId,
            userName = auth.currentUser?.displayName ?: "Unknown",
            userProfile = auth.currentUser?.photoUrl?.toString(),
            imageUrl = imageUrl,
            text = "$caption\n$postText",
            timestamp = null,
            likes = emptyList(),
            commentCount = 0
        )

        db.collection("posts").add(post)
            .addOnSuccessListener {
                Toast.makeText(this, "Post uploaded!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save post", Toast.LENGTH_SHORT).show()
            }
    }
}