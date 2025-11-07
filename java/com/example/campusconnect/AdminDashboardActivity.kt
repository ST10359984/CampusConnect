package com.example.campusconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AdminDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val btnViewAsUser: Button = findViewById(R.id.btnViewAsUser)
        val btnModeratePosts: Button = findViewById(R.id.btnModeratePosts)

        btnViewAsUser.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        btnModeratePosts.setOnClickListener {
            val intent = Intent(this, AdminModerationActivity::class.java)
            startActivity(intent)
        }
    }
}